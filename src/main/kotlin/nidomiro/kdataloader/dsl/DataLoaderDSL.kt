package nidomiro.kdataloader.dsl

import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoader
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.SimpleDataLoaderImpl

class DataLoaderDSL<K, R>(
    private val batchLoader: BatchLoader<K, R>
) {

    private var options = DataLoaderOptionsDSL<K, R>()
    private var primes = mutableMapOf<K, ExecutionResult<R>>()


    /**
     * Lets you configure the [DataLoader]
     */
    fun configure(block: DataLoaderOptionsDSL<K, R>.() -> Unit) {
        options.apply(block)
    }

    /**
     * Primes the cache with the given value(s), if you have duplicate keys, the last one counts (includes [prime] with [Throwable])
     */
    fun prime(vararg pairs: Pair<K, R>) {
        pairs.forEach { pair ->
            primes[pair.first] = ExecutionResult.Success(pair.second)
        }

    }

    /**
     * Primes the cache with the given value(s), if you have duplicate keys, the last one counts (includes [prime] with [R])
     */
    @JvmName("primeError")
    fun prime(vararg pairs: Pair<K, Throwable>) {
        pairs.forEach { pair ->
            primes[pair.first] = ExecutionResult.Failure(pair.second)
        }

    }


    internal suspend fun toDataLoader(): DataLoader<K, R> {
        val dataLoader = SimpleDataLoaderImpl(options.toDataLoaderOptions(), batchLoader)
        primes.forEach { (key, value) -> dataLoader.prime(key, value) }
        return dataLoader
    }
}


suspend fun <K, R> dataLoader(
    batchLoader: BatchLoader<K, R>,
    block: (DataLoaderDSL<K, R>.() -> Unit)? = null
): DataLoader<K, R> {
    val dataLoaderDSL = DataLoaderDSL(batchLoader)
    block?.let { dataLoaderDSL.apply(it) }
    return dataLoaderDSL.toDataLoader()
}