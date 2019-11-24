package nidomiro.kdataloader

fun <K> identityBatchLoader(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> = { ids ->
    loadCalls.add(ids)
    ids
}

fun <K> identityBatchLoaderThatThrows(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> = { ids ->
    loadCalls.add(ids)
    throw IllegalStateException("Test")
}

fun identityBatchLoaderThatThrowsOnOddNumber(loadCalls: MutableList<List<Int>> = mutableListOf()): BatchLoader<Int, Int> =
    { ids ->
        loadCalls.add(ids)
        ids.map {
            if (it % 2 == 1) {
                throw IllegalStateException("Test")
            } else {
                it
            }
        }

    }