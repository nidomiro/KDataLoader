package nidomiro.kdataloader

/**
 * A "Threadsave" Cache
 * (Coroutine-Save)
 */
interface Cache<K, V> {

    suspend fun store(key: K, value: V): V

    suspend fun get(key: K): V?

    suspend fun getOrCreate(key: K, generator: suspend (key: K) -> V): V

    suspend fun clear(key: K): V?

    suspend fun clear()
}