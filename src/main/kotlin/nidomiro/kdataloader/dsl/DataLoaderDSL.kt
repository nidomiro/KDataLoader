package nidomiro.kdataloader.dsl

import kotlinx.coroutines.runBlocking
import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoader
import nidomiro.kdataloader.ExecutionResult

class DataLoaderDSL<K, R : Any> {

    private var options = DataLoaderOptionsDSL<K, R>()
    private var primes = mutableMapOf<K, ExecutionResult<R>>()

    var batchLoader: BatchLoader<K, R>? = null

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


    internal fun toDataLoader(): DataLoader<K, R> {
        val batchLoader = this.batchLoader ?: throw IllegalStateException("You have do define a BatchLoader!")
        val dataLoader = DataLoader(options.toDataLoaderOptions(), batchLoader)
        runBlocking {
            primes.forEach { (key, value) -> dataLoader.prime(key, value) }
        }
        return dataLoader
    }
}


fun <K, R : Any> dataLoader(block: DataLoaderDSL<K, R>.() -> Unit): DataLoader<K, R> {
    val dataLoaderDSL = DataLoaderDSL<K, R>()
    dataLoaderDSL.apply(block)
    return dataLoaderDSL.toDataLoader()
}