package nidomiro.kdataloader.dsl

import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoader
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.factories.DataLoaderFactory
import nidomiro.kdataloader.factories.SimpleDataLoaderFactory
import nidomiro.kdataloader.prime

class DataLoaderDSL<K, R>(
    private val batchLoader: BatchLoader<K, R>
) {

    private var options = DataLoaderOptionsDSL<K, R>()
    private var cachePrimes = mutableMapOf<K, ExecutionResult<R>>()


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
            cachePrimes[pair.first] = ExecutionResult.Success(pair.second)
        }

    }

    /**
     * Primes the cache with the given value(s), if you have duplicate keys, the last one counts (includes [prime] with [R])
     */
    @JvmName("primeError")
    fun prime(vararg pairs: Pair<K, Throwable>) {
        pairs.forEach { pair ->
            cachePrimes[pair.first] = ExecutionResult.Failure(pair.second)
        }

    }

    internal fun toDataLoaderFactory(): DataLoaderFactory<K, R> {
        return SimpleDataLoaderFactory(options.toDataLoaderOptions(), cachePrimes, batchLoader)
    }
}


suspend fun <K, R> dataLoader(
    batchLoader: BatchLoader<K, R>,
    block: (DataLoaderDSL<K, R>.() -> Unit)? = null
): DataLoader<K, R> {
    return dataLoaderFactory(batchLoader, block).constructNew()
}

fun <K, R> dataLoaderFactory(
    batchLoader: BatchLoader<K, R>,
    block: (DataLoaderDSL<K, R>.() -> Unit)? = null
): DataLoaderFactory<K, R> {
    val dataLoaderDSL = DataLoaderDSL(batchLoader)
    block?.let { dataLoaderDSL.apply(it) }
    return dataLoaderDSL.toDataLoaderFactory()
}