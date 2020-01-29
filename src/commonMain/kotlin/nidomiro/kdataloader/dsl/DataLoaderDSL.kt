package nidomiro.kdataloader.dsl

import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoader
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.factories.DataLoaderFactory
import nidomiro.kdataloader.factories.SimpleDataLoaderFactory
import nidomiro.kdataloader.prime
import kotlin.jvm.JvmName

class DataLoaderDSL<K, R>(
    private val batchLoader: BatchLoader<K, R>
) {

    private var optionsBlock: DataLoaderOptionsDSL<K, R>.() -> Unit = { }
    private var cachePrimes = mutableMapOf<K, ExecutionResult<R>>()


    /**
     * Lets you configure the [DataLoader]
     */
    fun configure(block: DataLoaderOptionsDSL<K, R>.() -> Unit) {
        optionsBlock = block
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
        return SimpleDataLoaderFactory(
            { DataLoaderOptionsDSL<K, R>().apply(optionsBlock).toDataLoaderOptions() },
            cachePrimes,
            batchLoader
        )
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
