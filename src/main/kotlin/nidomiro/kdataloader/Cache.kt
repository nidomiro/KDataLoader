package nidomiro.kdataloader

/**
 * A "Threadsave" Cache
 */
interface Cache<K, V> {

    suspend fun store(key: K, value: V): V

    suspend fun get(key: K): V?

    suspend fun getOrCreate(key: K, generator: (key: K) -> V): V

    suspend fun remove(key: K): V?

    suspend fun clear()
}