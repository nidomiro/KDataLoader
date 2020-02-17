/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.test.Test

/**
 * Tests for [SimpleDataLoaderImpl].
 *
 * The tests are a inspired by the existing tests in
 * the <a href="https://github.com/facebook/dataloader">facebook/dataloader</a> project.
 *
 *
 * @author Niclas Roßberger
 */
class SimpleDataLoaderImplTest {

    @Test
    fun load_One_to_test_basic_functionality() = runBlockingWithTimeout {

        val dataLoader =
            SimpleDataLoaderImpl(identityBatchLoader<Int>())

        val deferredOne = dataLoader.loadAsync(1)
        assertThat(deferredOne.isCompleted).isFalse()

        dataLoader.dispatch()
        assertThat(deferredOne.await()).isEqualTo(1)
    }

    @Test
    fun accepts_nullable_types_as_result_type() = runBlockingWithTimeout {

        val dataLoader =
            SimpleDataLoaderImpl(identityBatchLoaderThatReturnsNullOnOddNumber())

        val deferredOne = dataLoader.loadAsync(1)
        val deferredTwo = dataLoader.loadAsync(2)
        assertThat(deferredOne.isCompleted).isFalse()
        assertThat(deferredTwo.isCompleted).isFalse()
        dataLoader.dispatch()

        assertThat(deferredOne.await()).isNull()
        assertThat(deferredTwo.await()).isEqualTo(2)
    }

    @Test
    fun load_many_in_one_call() = runBlockingWithTimeout {

        val dataLoader =
            SimpleDataLoaderImpl(identityBatchLoader<Int>())

        val items = listOf(1, 2, 3, 5, 4).toTypedArray()
        val deferredAll = dataLoader.loadManyAsync(*items)
        dataLoader.dispatch()
        assertThat(deferredAll.await()).isEqualTo(items.toList())
    }

    @Test
    fun load_many_with_empty_key_list() = runBlockingWithTimeout {

        val dataLoader =
            SimpleDataLoaderImpl(identityBatchLoader<Int>())

        val deferredAll = dataLoader.loadManyAsync(*listOf<Int>().toTypedArray())
        dataLoader.dispatch()
        assertThat(deferredAll.await().size).isEqualTo(0)
    }

    @Test
    fun completables_are_completed_in_batch_with_dispatch() = runBlockingWithTimeout {

        val dataLoader =
            SimpleDataLoaderImpl(identityBatchLoader<Int>())

        val deferredOne = dataLoader.loadAsync(1)
        val deferredTwo = dataLoader.loadAsync(2)
        val deferredThree = dataLoader.loadAsync(3)

        assertThat(deferredOne.isCompleted).isFalse()
        assertThat(deferredTwo.isCompleted).isFalse()
        assertThat(deferredThree.isCompleted).isFalse()

        dataLoader.dispatch()
        assertThat(deferredOne.await()).isEqualTo(1)
        assertThat(deferredTwo.await()).isEqualTo(2)
        assertThat(deferredThree.await()).isEqualTo(3)


    }

    @Test
    fun enqueue_of_same_id_results_in_same_Deferred() = runBlockingWithTimeout {

        val dataLoader =
            SimpleDataLoaderImpl(identityBatchLoader<Int>())

        val deferredOne = dataLoader.loadAsync(1)
        val deferredTwo = dataLoader.loadAsync(2)
        val deferredOneDuplicate = dataLoader.loadAsync(1)

        assertThat(deferredOne, "loading the same id must result in the same Deferred").isEqualTo(
            deferredOneDuplicate
        )

        assertThat(deferredOne.isCompleted).isFalse()
        assertThat(deferredTwo.isCompleted).isFalse()
        assertThat(deferredOneDuplicate.isCompleted).isFalse()


        dataLoader.dispatch()
        assertThat(deferredOne.await()).isEqualTo(1)
        assertThat(deferredTwo.await()).isEqualTo(2)
        assertThat(deferredOneDuplicate.await()).isEqualTo(1)


    }

    @Test
    fun cache_repeated_requests() = runBlockingWithTimeout {

        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")


        assertThat(loadCalls).isEmpty()
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")

        assertThat(loadCalls).containsExactly(listOf("A", "B"))


        val deferredA1 = dataLoader.loadAsync("A")
        val deferredC = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredC.await()).isEqualTo("C")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("C"))


        val deferredA2 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        val deferredC1 = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA2.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(deferredC1.await()).isEqualTo("C")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("C"))

    }

    @Test
    fun should_not_redispatch_previous_load() = runBlockingWithTimeout {

        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        dataLoader.dispatch()
        assertThat(deferredA.await()).isEqualTo("A")

        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("A"), listOf("B"))

    }

    @Test
    fun should_cache_dispatch() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")

        val deferredAB = dataLoader.loadManyAsync("A", "B")
        dataLoader.dispatch()
        assertThat(deferredAB.await()).containsExactly("A", "B")

        assertThat(loadCalls).containsExactly(listOf("A"), listOf("B"))
    }

    @Test
    fun cleared_keys_are_refetched() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("A", "B"))

        dataLoader.clear("A")

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("A"))

    }

    @Test
    fun clear_all_causes_complete_refetch() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("A", "B"))

        dataLoader.clearAll()

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("A", "B"))

    }

    @Test
    fun accepts_any_object_as_key() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Any>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val keyA = Any()
        val keyB = Any()

        val deferredA = dataLoader.loadAsync(keyA)
        val deferredB = dataLoader.loadAsync(keyB)
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo(keyA)
        assertThat(deferredB.await()).isEqualTo(keyB)
        assertThat(loadCalls).containsExactly(listOf(keyA, keyB))
    }


    // Prime tests

    @Test
    fun prime_cache_is_working() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        dataLoader.prime("A" to "A")

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("B"))


    }

    @Test
    fun prime_cache_does_not_override() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        dataLoader.prime("A" to "A")

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("B"))

        dataLoader.prime("A" to "X")
        dataLoader.prime("B" to "X")

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("B"))


    }

    @Test
    fun force_prime_cache_does_override() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        dataLoader.prime("A" to "A")

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("B"))

        dataLoader.clear("A")
        dataLoader.prime("A" to "X")
        dataLoader.clear("B")
        dataLoader.prime("B" to "X")

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")

        assertThat(deferredA1.await()).isEqualTo("X")
        assertThat(deferredB1.await()).isEqualTo("X")
        assertThat(loadCalls).containsExactly(listOf("B"))
    }

    @Test
    fun prime_cache_with_error() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        dataLoader.prime(1 to IllegalStateException("Prime"))

        val deferred1 = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        assertThat {
            deferred1.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        assertThat(loadCalls).isEqualTo(listOf<List<Int>>())
    }


    // Error Handling

    @Test
    fun failed_requests_are_not_cached_on_complete_failure() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoaderThatFailsCompletely(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        dataLoader.dispatch()

        assertThat {
            deferredA.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        val deferredA1 = dataLoader.loadAsync("A")
        dataLoader.dispatch()

        assertThat {
            deferredA1.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        assertThat(deferredA).isNotEqualTo(deferredA1)
        assertThat(loadCalls).containsExactly(listOf("A"), listOf("A"))


    }

    @Test
    fun failed_requests_are_not_permanent_if_complete_failure() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoaderThatThrowsOnOddNumber(loadCalls)
        )

        val deferred1 = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        assertThat {
            deferred1.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        val deferred2 = dataLoader.loadAsync(2)
        dataLoader.dispatch()

        assertThat(deferred2.await()).isEqualTo(2)

        assertThat(loadCalls).containsExactly(listOf(1), listOf(2))
    }

    @Test
    fun failed_and_successful_requests_are_returned() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoaderThatThrowsOnOddNumber(loadCalls)
        )

        val deferrts = (1..4).map { dataLoader.loadAsync(it) }
        dataLoader.dispatch()

        deferrts.forEachIndexed { index, deferred ->
            val num = index + 1
            if (num.isEven()) {
                assertThat(deferred.await()).isEqualTo(num)
            } else {
                assertThat {
                    deferred.await()
                }.isFailure()
                    .hasClass(IllegalStateException::class)
            }
        }

        assertThat(loadCalls).containsExactly(listOf(1, 2, 3, 4))
    }

    @Test
    fun failed_requests_are_cached() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoaderThatThrowsOnOddNumber(loadCalls)
        )

        val deferred1 = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        assertThat {
            deferred1.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        val deferred1a = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        assertThat {
            deferred1a.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        assertThat(deferred1).isEqualTo(deferred1a)
        assertThat(loadCalls).containsExactly(listOf(1))
    }

    @Test
    fun failed_requests_are_not_cached_if_told_not_to() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(cacheExceptions = false),
            identityBatchLoaderThatThrowsOnOddNumber(loadCalls)
        )

        val deferred1 = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        assertThat {
            deferred1.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        val deferred1a = dataLoader.loadAsync(1)
        dataLoader.dispatch()

        assertThat {
            deferred1a.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        assertThat(deferred1).isNotEqualTo(deferred1a)
        assertThat(loadCalls).containsExactly(listOf(1), listOf(1))
    }

    @Test
    fun complete_failures_are_propagated_to_every_load() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoaderThatFailsCompletely(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat {
            deferredA.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        assertThat {
            deferredB.await()
        }.isFailure()
            .hasClass(IllegalStateException::class)

        assertThat(loadCalls).containsExactly(listOf("A", "B"))
    }


    // CacheEnableFlag Test

    @Test
    fun caching_is_disableable_with_load() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(cache = null),
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).containsExactly(listOf("A", "B"))

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredC = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredC.await()).isEqualTo("C")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("A", "C"))

        val deferredA2 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        val deferredC1 = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA2.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(deferredC1.await()).isEqualTo("C")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("A", "C"), listOf("A", "B", "C"))
    }

    @Test
    fun caching_is_disableable_with_loadMany() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(cache = null),
            identityBatchLoader(loadCalls)
        )

        val deferreds1 = dataLoader.loadManyAsync("A", "B")
        dataLoader.dispatch()

        assertThat(deferreds1.await()).containsExactly("A", "B")
        assertThat(loadCalls).containsExactly(listOf("A", "B"))

        val deferreds2 = dataLoader.loadManyAsync("A", "C")
        dataLoader.dispatch()

        assertThat(deferreds2.await()).containsExactly("A", "C")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("A", "C"))

        val deferreds3 = dataLoader.loadManyAsync("A", "B", "C")
        dataLoader.dispatch()

        assertThat(deferreds3.await()).containsExactly("A", "B", "C")
        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("A", "C"), listOf("A", "B", "C"))
    }

    @Test
    fun caching_is_disableable_with_load_and_duplicates() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(cache = null),
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        val deferredA1 = dataLoader.loadAsync("A")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(loadCalls).containsExactly(listOf("A", "B", "A"))
    }

    @Test
    fun caching_is_enableable_with_load_and_duplicates() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(cache = CoroutineMapCache()),
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        val deferredA1 = dataLoader.loadAsync("A")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(loadCalls).containsExactly(listOf("A", "B"))
    }

    @Test
    fun batching_can_be_disabled() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(batchMode = BatchMode.LoadImmediately),
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        val deferredC = dataLoader.loadAsync("C")

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(deferredC.await()).isEqualTo("C")
        assertThat(loadCalls).containsExactly(listOf("A"), listOf("B"), listOf("C"))
    }

    @Test
    fun batching_and_caching_can_be_disabled_together() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(
                batchMode = BatchMode.LoadImmediately,
                cache = null
            ),
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        val deferredA1 = dataLoader.loadAsync("A")

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(loadCalls).containsExactly(listOf("A"), listOf("B"), listOf("A"))
    }

    @Test
    fun batching_is_honoring_batchsize() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            DataLoaderOptions(batchMode = BatchMode.LoadInBatch(2)),
            identityBatchLoader(loadCalls)
        )

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        val deferredC = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(deferredC.await()).isEqualTo("C")

        assertThat(loadCalls).containsExactly(listOf("A", "B"), listOf("C"))
    }

    @Test
    fun batching_is_occurring_in_async() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        @Suppress("RedundantAsync", "DeferredResultUnused")
        async(Dispatchers.Default) {
            async { dataLoader.loadAsync("A") }.await()
            async { dataLoader.loadAsync("B") }.await()
            async { dataLoader.loadAsync("C") }.await()

        }.await()
        dataLoader.dispatch()

        assertThat(loadCalls).containsExactly(listOf("A", "B", "C"))
    }

    @Test
    fun parallel_batching_is_possible() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Int>>()
        val dataLoader = SimpleDataLoaderImpl(
            identityBatchLoader(loadCalls)
        )

        val deferredList = (0..50).map {
            it to GlobalScope.async(Dispatchers.Default) { dataLoader.loadAsync(it) }
        }
            .map { it.first to it.second.await() }
        dataLoader.dispatch()
        deferredList.forEach { (index, deferred) -> assertThat(deferred.await()).isEqualTo(index) }

        assertThat(loadCalls).hasSize(1)
        assertThat(loadCalls[0]).hasSize(deferredList.size)
    }

}
