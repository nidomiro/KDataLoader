/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests for [DataLoader].
 *
 * The tests are a inspired by the existing tests in
 * the <a href="https://github.com/facebook/dataloader">facebook/dataloader</a> project.
 *
 *
 * @author Niclas Roßberger
 */
class DataLoaderTest {

    @Test
    fun `load One to test basic functionality`() = runBlockingWithTimeout {

        val dataLoader = DataLoader(identityBatchLoader<Int>())

        val deferredOne = dataLoader.loadAsync(1)
        assertThat(deferredOne.isCompleted).isFalse()

        dataLoader.dispatch()
        assertThat(deferredOne.await()).isEqualTo(1)
    }

    @Test
    fun `load many in one call`() = runBlockingWithTimeout {

        val dataLoader = DataLoader(identityBatchLoader<Int>())

        val items = listOf(1, 2, 3, 5, 4).toTypedArray()
        val deferredAll = dataLoader.loadManyAsync(*items)
        dataLoader.dispatch()
        assertThat(deferredAll.await()).isEqualTo(items.toList())
    }

    @Test
    fun `load many with empty key-list`() = runBlockingWithTimeout {

        val dataLoader = DataLoader(identityBatchLoader<Int>())

        val deferredAll = dataLoader.loadManyAsync(*listOf<Int>().toTypedArray())
        dataLoader.dispatch()
        assertThat(deferredAll.await().size).isEqualTo(0)
    }

    @Test
    fun `completables are completed in batch with dispatch()`() = runBlockingWithTimeout {

        val dataLoader = DataLoader(identityBatchLoader<Int>())

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
    fun `enqueue of same id results in same Deferred`() = runBlockingWithTimeout {

        val dataLoader = DataLoader(identityBatchLoader<Int>())

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
    fun `cache repeated requests`() = runBlockingWithTimeout {

        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = DataLoader(identityBatchLoader(loadCalls))

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")


        assertThat(loadCalls).isEmpty()
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")

        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))


        val deferredA1 = dataLoader.loadAsync("A")
        val deferredC = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredC.await()).isEqualTo("C")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("C")))


        val deferredA2 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        val deferredC1 = dataLoader.loadAsync("C")
        dataLoader.dispatch()

        assertThat(deferredA2.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(deferredC1.await()).isEqualTo("C")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("C")))

    }

    @Test
    fun `should not redispatch previous load`() = runBlockingWithTimeout {

        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = DataLoader(identityBatchLoader(loadCalls))

        val deferredA = dataLoader.loadAsync("A")
        dataLoader.dispatch()
        assertThat(deferredA.await()).isEqualTo("A")

        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("B")))

    }

    @Test
    fun `should cache dispatch`() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = DataLoader(identityBatchLoader(loadCalls))

        val deferredA = dataLoader.loadAsync("A")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")

        val deferredAB = dataLoader.loadManyAsync("A", "B")
        dataLoader.dispatch()
        assertThat(deferredAB.await()).isEqualTo(listOf("A", "B"))

        assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("B")))
    }


    @Test
    fun `cleared keys are refetched`() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = DataLoader(identityBatchLoader(loadCalls))

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))

        dataLoader.clear("A")

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("A")))

    }

    @Test
    fun `clear all causes complete refetch`() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<String>>()
        val dataLoader = DataLoader(identityBatchLoader(loadCalls))

        val deferredA = dataLoader.loadAsync("A")
        val deferredB = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo("A")
        assertThat(deferredB.await()).isEqualTo("B")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))

        dataLoader.clearAll()

        val deferredA1 = dataLoader.loadAsync("A")
        val deferredB1 = dataLoader.loadAsync("B")
        dataLoader.dispatch()

        assertThat(deferredA1.await()).isEqualTo("A")
        assertThat(deferredB1.await()).isEqualTo("B")
        assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("A", "B")))

    }

    @Test
    fun `accepts any object as key`() = runBlockingWithTimeout {
        val loadCalls = mutableListOf<List<Any>>()
        val dataLoader = DataLoader(identityBatchLoader(loadCalls))

        val keyA = Any()
        val keyB = Any()

        val deferredA = dataLoader.loadAsync(keyA)
        val deferredB = dataLoader.loadAsync(keyB)
        dataLoader.dispatch()

        assertThat(deferredA.await()).isEqualTo(keyA)
        assertThat(deferredB.await()).isEqualTo(keyB)
        assertThat(loadCalls).isEqualTo(listOf(listOf(keyA, keyB)))
    }


    @Nested
    inner class PrimeTests {
        @Test
        fun `prime cache is working`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            dataLoader.prime("A" to "A")

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(loadCalls).isEqualTo(listOf(listOf("B")))


        }

        @Test
        fun `prime cache does not override`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            dataLoader.prime("A" to "A")

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(loadCalls).isEqualTo(listOf(listOf("B")))

            dataLoader.prime("A" to "X")
            dataLoader.prime("B" to "X")

            val deferredA1 = dataLoader.loadAsync("A")
            val deferredB1 = dataLoader.loadAsync("B")

            assertThat(deferredA1.await()).isEqualTo("A")
            assertThat(deferredB1.await()).isEqualTo("B")
            assertThat(loadCalls).isEqualTo(listOf(listOf("B")))


        }

        @Test
        fun `force prime cache does override`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            dataLoader.prime("A" to "A")

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(loadCalls).isEqualTo(listOf(listOf("B")))

            dataLoader.clear("A")
            dataLoader.prime("A" to "X")
            dataLoader.clear("B")
            dataLoader.prime("B" to "X")

            val deferredA1 = dataLoader.loadAsync("A")
            val deferredB1 = dataLoader.loadAsync("B")

            assertThat(deferredA1.await()).isEqualTo("X")
            assertThat(deferredB1.await()).isEqualTo("X")
            assertThat(loadCalls).isEqualTo(listOf(listOf("B")))
        }

        @Test
        fun `prime cache with error`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<Int>>()
            val dataLoader = DataLoader(identityBatchLoader(loadCalls))

            dataLoader.prime(1 to IllegalStateException("Prime"))

            val deferred1 = dataLoader.loadAsync(1)
            dataLoader.dispatch()

            assertThat {
                deferred1.await()
            }.isFailure()
                .hasClass(IllegalStateException::class)

            assertThat(loadCalls).isEqualTo(listOf<List<Int>>())
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `failed requests are not cached on complete failure`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoaderThatFailsCompletly(loadCalls))

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
            assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("A")))


        }

        @Test
        fun `failed requests are not permanent if complete failure`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<Int>>()
            val dataLoader = DataLoader(identityBatchLoaderThatThrowsOnOddNumber(loadCalls))

            val deferred1 = dataLoader.loadAsync(1)
            dataLoader.dispatch()

            assertThat {
                deferred1.await()
            }.isFailure()
                .hasClass(IllegalStateException::class)

            val deferred2 = dataLoader.loadAsync(2)
            dataLoader.dispatch()

            assertThat(deferred2.await()).isEqualTo(2)

            assertThat(loadCalls).isEqualTo(listOf(listOf(1), listOf(2)))
        }

        @Test
        fun `failed and successful requests are returned`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<Int>>()
            val dataLoader = DataLoader(identityBatchLoaderThatThrowsOnOddNumber(loadCalls))

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

            assertThat(loadCalls).isEqualTo(listOf(listOf(1, 2, 3, 4)))
        }

        @Test
        fun `failed requests are cached`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<Int>>()
            val dataLoader = DataLoader(identityBatchLoaderThatThrowsOnOddNumber(loadCalls))

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
            assertThat(loadCalls).isEqualTo(listOf(listOf(1)))
        }

        @Test
        fun `failed requests are not cached if told not to`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<Int>>()
            val dataLoader = DataLoader(
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
            assertThat(loadCalls).isEqualTo(listOf(listOf(1), listOf(1)))
        }

        @Test
        fun `complete failures are propagated to every load`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(identityBatchLoaderThatFailsCompletly(loadCalls))

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

            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))
        }

    }

    @Nested
    inner class CacheEnableFlag {

        @Test
        fun `caching is disableable with load`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(cacheEnabled = false),
                identityBatchLoader(loadCalls)
            )

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))

            val deferredA1 = dataLoader.loadAsync("A")
            val deferredC = dataLoader.loadAsync("C")
            dataLoader.dispatch()

            assertThat(deferredA1.await()).isEqualTo("A")
            assertThat(deferredC.await()).isEqualTo("C")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("A", "C")))

            val deferredA2 = dataLoader.loadAsync("A")
            val deferredB1 = dataLoader.loadAsync("B")
            val deferredC1 = dataLoader.loadAsync("C")
            dataLoader.dispatch()

            assertThat(deferredA2.await()).isEqualTo("A")
            assertThat(deferredB1.await()).isEqualTo("B")
            assertThat(deferredC1.await()).isEqualTo("C")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("A", "C"), listOf("A", "B", "C")))
        }

        @Test
        fun `caching is disableable with loadMany`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(cacheEnabled = false),
                identityBatchLoader(loadCalls)
            )

            val deferreds1 = dataLoader.loadManyAsync("A", "B")
            dataLoader.dispatch()

            assertThat(deferreds1.await()).isEqualTo(listOf("A", "B"))
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))

            val deferreds2 = dataLoader.loadManyAsync("A", "C")
            dataLoader.dispatch()

            assertThat(deferreds2.await()).isEqualTo(listOf("A", "C"))
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("A", "C")))

            val deferreds3 = dataLoader.loadManyAsync("A", "B", "C")
            dataLoader.dispatch()

            assertThat(deferreds3.await()).isEqualTo(listOf("A", "B", "C"))
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("A", "C"), listOf("A", "B", "C")))
        }

        @Test
        fun `caching is disableable with load and duplicates`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(cacheEnabled = false),
                identityBatchLoader(loadCalls)
            )

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            val deferredA1 = dataLoader.loadAsync("A")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(deferredA1.await()).isEqualTo("A")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B", "A")))
        }

        @Test
        fun `caching is enableable with load and duplicates`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(cacheEnabled = true),
                identityBatchLoader(loadCalls)
            )

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            val deferredA1 = dataLoader.loadAsync("A")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(deferredA1.await()).isEqualTo("A")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B")))
        }

        @Test
        fun `batching can be disabled`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(batchLoadEnabled = false),
                identityBatchLoader(loadCalls)
            )

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            val deferredC = dataLoader.loadAsync("C")

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(deferredC.await()).isEqualTo("C")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("B"), listOf("C")))
        }

        @Test
        fun `batching and caching can be disabled together`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(
                    batchLoadEnabled = false,
                    cacheEnabled = false
                ),
                identityBatchLoader(loadCalls)
            )

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            val deferredA1 = dataLoader.loadAsync("A")

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(deferredA1.await()).isEqualTo("A")
            assertThat(loadCalls).isEqualTo(listOf(listOf("A"), listOf("B"), listOf("A")))
        }

        @Test
        fun `batching is honoring batchsize`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                DataLoaderOptions(batchSize = 2),
                identityBatchLoader(loadCalls)
            )

            val deferredA = dataLoader.loadAsync("A")
            val deferredB = dataLoader.loadAsync("B")
            val deferredC = dataLoader.loadAsync("C")
            dataLoader.dispatch()

            assertThat(deferredA.await()).isEqualTo("A")
            assertThat(deferredB.await()).isEqualTo("B")
            assertThat(deferredC.await()).isEqualTo("C")

            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B"), listOf("C")))
        }

        @Test
        fun `batching is occurring in async`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<String>>()
            val dataLoader = DataLoader(
                identityBatchLoader(loadCalls)
            )

            @Suppress("RedundantAsync", "DeferredResultUnused")
            async(Dispatchers.IO) {
                async { dataLoader.loadAsync("A") }.await()
                async { dataLoader.loadAsync("B") }.await()
                async { dataLoader.loadAsync("C") }.await()

            }.await()
            dataLoader.dispatch()

            assertThat(loadCalls).isEqualTo(listOf(listOf("A", "B", "C")))
        }

        @Test
        fun `parallel batching is possible`() = runBlockingWithTimeout {
            val loadCalls = mutableListOf<List<Int>>()
            val dataLoader = DataLoader(
                identityBatchLoader(loadCalls)
            )

            val deferredList = (0..50).map {
                it to GlobalScope.async(Dispatchers.IO) { dataLoader.loadAsync(it) }
            }
                .map { it.first to it.second.await() }
            dataLoader.dispatch()
            deferredList.forEach { (index, deferred) -> assertThat(deferred.await()).isEqualTo(index) }

            assertThat(loadCalls).hasSize(1)
            assertThat(loadCalls[0]).hasSize(deferredList.size)
        }



    }




}
