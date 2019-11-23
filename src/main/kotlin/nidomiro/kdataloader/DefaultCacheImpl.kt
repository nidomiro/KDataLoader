package nidomiro.kdataloader

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultCacheImpl<K, V> : Cache<K, V> {

    private val cacheMap: MutableMap<K, V> = mutableMapOf()

    private val mutex = Mutex()


    override suspend fun store(key: K, value: V): V {
        mutex.withLock {
            cacheMap[key] = value
        }
        return value
    }

    override suspend fun get(key: K): V? =
        mutex.withLock {
            cacheMap[key]
        }


    override suspend fun getOrCreate(key: K, generator: (key: K) -> V): V =
        mutex.withLock {
            cacheMap.computeIfAbsent(key, generator)
        }


    override suspend fun remove(key: K): V? =
        mutex.withLock {
            cacheMap.remove(key)
        }


    override suspend fun clear() =
        mutex.withLock {
            cacheMap.clear()
        }


}