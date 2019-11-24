package nidomiro.kdataloader

fun <K : Any> identityBatchLoader(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> = { ids ->
    loadCalls.add(ids)
    ids.map { ExecutionResult.Success(it) }
}

fun <K : Any> identityBatchLoaderThatThrows(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> =
    { ids ->
    loadCalls.add(ids)
    throw IllegalStateException("Test")
}

fun identityBatchLoaderThatThrowsOnOddNumber(loadCalls: MutableList<List<Int>> = mutableListOf()): BatchLoader<Int, Int> =
    { ids ->
        loadCalls.add(ids)
        ids.map {
            if (it % 2 == 1) {
                ExecutionResult.Failure(IllegalStateException("Test"))
            } else {
                ExecutionResult.Success(it)
            }
        }

    }