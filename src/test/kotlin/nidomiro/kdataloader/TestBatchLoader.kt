package nidomiro.kdataloader

fun <K> identityBatchLoader(loadCalls: MutableList<List<K>> = mutableListOf()): BatchLoader<K, K> = { ids ->
    loadCalls.add(ids)
    ids
}