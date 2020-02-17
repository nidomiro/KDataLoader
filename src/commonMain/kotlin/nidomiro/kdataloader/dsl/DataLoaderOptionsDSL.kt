@file:Suppress("MemberVisibilityCanBePrivate")

package nidomiro.kdataloader.dsl

import nidomiro.kdataloader.DataLoaderOptions

class DataLoaderOptionsDSL<K, R> {

    private var cacheDefinitionDSL = CacheDefinitionDSL<K, R>()

    /**
     * Load individually (and immediately) or in batch
     */
    var batchLoadEnabled: Boolean = true

    /**
     * The maximum size of one batch
     */
    var batchSize: Int = Int.MAX_VALUE

    /**
     * The cache implementation definition
     */
    fun cache(block: CacheDefinitionDSL<K, R>.() -> Unit) {
        cacheDefinitionDSL.block()
    }


    internal fun toDataLoaderOptions() = DataLoaderOptions(
        cache = cacheDefinitionDSL.getCacheInstance(),
        cacheExceptions = cacheDefinitionDSL.cacheExceptions,
        batchLoadEnabled = batchLoadEnabled,
        batchSize = batchSize
    )

}