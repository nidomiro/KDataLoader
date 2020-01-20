package nidomiro.kdataloader.factories

import nidomiro.kdataloader.*

typealias DataLoaderFactoryMethod<K, R> = (options: DataLoaderOptions<K, R>, batchLoader: BatchLoader<K, R>) -> DataLoader<K, R>

open class DataLoaderFactory<K, R>(
    @Suppress("MemberVisibilityCanBePrivate")
    protected val optionsFactory: () -> DataLoaderOptions<K, R>,
    @Suppress("MemberVisibilityCanBePrivate")
    protected val batchLoader: BatchLoader<K, R>,
    @Suppress("MemberVisibilityCanBePrivate")
    protected val cachePrimes: Map<K, ExecutionResult<R>>,
    protected val factoryMethod: DataLoaderFactoryMethod<K, R>
) {

    suspend fun constructNew(): DataLoader<K, R> {
        val dataLoader = factoryMethod(optionsFactory(), batchLoader)
        cachePrimes.forEach { (key, value) -> dataLoader.prime(key, value) }
        return dataLoader
    }
}
