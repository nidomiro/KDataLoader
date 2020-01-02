package nidomiro.kdataloader

fun <K> identityBatchLoader(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> = { ids ->
    loadCalls.add(ids)
    ids.map { ExecutionResult.Success(it) }
}

fun <K> identityBatchLoaderThatFailsCompletly(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> =
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

fun identityBatchLoaderThatReturnsNullOnOddNumber(loadCalls: MutableList<List<Int>> = mutableListOf()): BatchLoader<Int, Int?> =
    { ids ->
        loadCalls.add(ids)
        ids.map {
            if (it % 2 == 1) {
                ExecutionResult.Success(null)
            } else {
                ExecutionResult.Success(it)
            }
        }

    }