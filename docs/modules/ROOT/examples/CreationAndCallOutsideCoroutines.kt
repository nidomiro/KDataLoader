package nidomiro.kdataloader.example

import kotlinx.coroutines.runBlocking
import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.dsl.dataLoader

fun main() {
    val batchLoader: BatchLoader<Int, Int> =
        { keys -> keys.map { ExecutionResult.Success(it) } }

    val dataLoader = runBlocking {
        dataLoader(batchLoader)
    }

    val value1 = runBlocking { dataLoader.loadAsync(1) }
    runBlocking { dataLoader.dispatch() }

    println(runBlocking { value1.await() })
}
