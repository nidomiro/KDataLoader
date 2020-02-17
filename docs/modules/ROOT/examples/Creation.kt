package nidomiro.kdataloader.example

import kotlinx.coroutines.runBlocking
import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DefaultCacheImpl
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.dsl.dataLoader

fun main(): Unit = runBlocking {
    val batchLoader: BatchLoader<Int, Int> =
        { keys -> keys.map { ExecutionResult.Success(it) } }

    val dataLoader = dataLoader(batchLoader) {

        configure {
            cache {
                enabled
                cacheExceptions = true
            }
            batchMode = BatchMode.LoadInBatch()
        }

        prime(1 to 1)
    }

    val value1 = dataLoader.loadAsync(1) // from cache
    val value2 = dataLoader.loadAsync(2) // loaded via BatchLoader
    dataLoader.dispatch()

    println("1 -> ${value1.await()}")
    println("2 -> ${value2.await()}")
}
