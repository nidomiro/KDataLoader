package nidomiro.kdataloader

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import nidomiro.kdataloader.statistics.SimpleStatisticsCollector

class TimedAutoDispatcherImpl<K, R>(
    options: TimedAutoDispatcherDataLoaderOptions<K, R>,
    batchLoader: BatchLoader<K, R>,
    parent: Job? = null,
) : SimpleDataLoaderImpl<K, R>(options, SimpleStatisticsCollector(), batchLoader), CoroutineScope {

    private val autoChannel = Channel<Unit>()
    override val coroutineContext = Job(parent)

    init {
        launch {
            var job: Job? = null
            var name = 1
            for (msg in autoChannel) {
                job?.cancel()
                job = launch(CoroutineName("name-${name++}")) {
                    delay(options.waitInterval)
                    if (isActive) {
                        println("[$job] Calling dispatch!")
                        dispatch()
                    }
                }
            }
        }
    }

    suspend fun cancel() {
        coroutineContext.cancel()
        autoChannel.close()
        dispatch()
    }

    override suspend fun loadAsync(key: K): Deferred<R> {
        return super.loadAsync(key).also { autoChannel.send(Unit) }
    }

    override suspend fun loadManyAsync(vararg keys: K): Deferred<List<R>> {
        autoChannel.send(Unit)
        return super.loadManyAsync(*keys)
    }

    override suspend fun clear(key: K) {
        super.clear(key).also { autoChannel.send(Unit) }
    }

    override suspend fun clearAll() {
        autoChannel.send(Unit)
        super.clearAll()
    }

    override suspend fun prime(key: K, value: R) {
        autoChannel.send(Unit)
        super.prime(key, value)
    }

    override suspend fun prime(key: K, value: Throwable){
        autoChannel.send(Unit)
        super.prime(key, value)
    }

}
