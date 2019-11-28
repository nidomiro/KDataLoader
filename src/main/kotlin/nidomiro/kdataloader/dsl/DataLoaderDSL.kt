package nidomiro.kdataloader.dsl

import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoader

class DataLoaderDSL<K, R : Any> {

    private var options = DataLoaderOptionsDSL<K, R>()
    private var primes = mutableMapOf<K, R>()

    var batchLoader: BatchLoader<K, R>? = null

    fun configure(block: DataLoaderOptionsDSL<K, R>.() -> Unit) {
        options.apply(block)
    }

    fun prime(vararg pairs: Pair<K, R>) {
        pairs.forEach { pair ->
            primes[pair.first] = pair.second
        }

    }


    fun toDataLoader(): DataLoader<K, R> {
        val batchLoader = this.batchLoader ?: throw IllegalStateException("You have do define a BatchLoader!")
        return DataLoader(options.toDataLoaderOptions(), batchLoader)
    }
}


fun <K, R : Any> dataLoader(block: DataLoaderDSL<K, R>.() -> Unit): DataLoader<K, R> {
    val dataLoaderDSL = DataLoaderDSL<K, R>()
    dataLoaderDSL.apply(block)
    return dataLoaderDSL.toDataLoader()
}