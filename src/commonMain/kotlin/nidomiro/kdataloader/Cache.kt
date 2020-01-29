package nidomiro.kdataloader

/**
 * A "Threadsafe" Cache
 * (Coroutine-Save)
 */
interface Cache<K, V> {

    suspend fun store(key: K, value: V): V

    suspend fun get(key: K): V?

    suspend fun getOrCreate(key: K, generator: suspend (key: K) -> V, callOnCacheHit: suspend () -> Unit): V

    suspend fun clear(key: K): V?

    suspend fun clear()
}

suspend fun <K, V> Cache<K, V>.getOrCreate(key: K, generator: suspend (key: K) -> V): V =
    getOrCreate(key, generator, {})