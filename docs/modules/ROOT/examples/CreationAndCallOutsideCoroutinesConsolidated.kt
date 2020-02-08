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

    runBlocking {
        val value1 = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        println(value1.await())
    }
}
