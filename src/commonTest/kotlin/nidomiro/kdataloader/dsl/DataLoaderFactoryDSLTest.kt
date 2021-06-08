package nidomiro.kdataloader.dsl

import assertk.assertThat
import assertk.assertions.*
import nidomiro.kdataloader.BatchMode
import nidomiro.kdataloader.CoroutineMapCache
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.runBlockingWithTimeout
import kotlin.test.Test

class DataLoaderFactoryDSLTest {

    @Test
    fun assure_Factory_creates_new_instances() = runBlockingWithTimeout {
        val dataLoaderFactory = dataLoaderFactory(
            { keys: List<Int> -> keys.map { ExecutionResult.Success(it.toString()) } }
        )

        assertThat { dataLoaderFactory.constructNew() }
            .isNotEqualTo(dataLoaderFactory.constructNew())
    }

    @Test
    fun assure_Factory_honors_options() = runBlockingWithTimeout {
        val dataLoaderFactory =
            dataLoaderFactory({ keys: List<Int> ->
                keys.map {
                    ExecutionResult.Success(
                        it.toString()
                    )
                }
            }) {
                configure {
                    cache {
                        disabled
                        cacheExceptions = false
                    }
                    batchMode = BatchMode.LoadInBatch(1)
                }
            }
        val dataLoader = dataLoaderFactory.constructNew()

        assertThat(dataLoader.options.batchMode).isInstanceOf(BatchMode.LoadInBatch::class)
        assertThat(dataLoader.options.batchMode)
            .transform { (it as? BatchMode.LoadInBatch)?.batchSize }
            .isEqualTo(1)
        assertThat(dataLoader.options.cache).isNull()
        assertThat(dataLoader.options.cacheExceptions).isEqualTo(false)
    }

    @Test
    fun assure_Factory_honors_nonDefault_Cache() = runBlockingWithTimeout {

        val myCacheInstance = CoroutineMapCache<Int, String>()

        val dataLoaderFactory =
            dataLoaderFactory({ keys: List<Int> ->
                keys.map {
                    ExecutionResult.Success(
                        it.toString()
                    )
                }
            }) {
                configure {
                    cache {
                        enabled with myCacheInstance
                        cacheExceptions = false
                    }

                    batchMode = BatchMode.LoadInBatch(1)
                }
            }
        val dataLoader = dataLoaderFactory.constructNew()

        assertThat(dataLoader.options.batchMode).isInstanceOf(BatchMode.LoadInBatch::class)
        assertThat(dataLoader.options.batchMode)
            .transform { (it as? BatchMode.LoadInBatch)?.batchSize }
            .isEqualTo(1)
        assertThat(dataLoader.options.cache).isEqualTo(myCacheInstance)
        assertThat(dataLoader.options.cacheExceptions).isEqualTo(false)
    }

    @Test
    fun assure_Factory_honors_primes() = runBlockingWithTimeout {
        val dataLoaderFactory =
            dataLoaderFactory({ keys: List<Int> ->
                keys.map {
                    ExecutionResult.Success(
                        it.toString()
                    )
                }
            }) {
                prime(
                    1 to IllegalArgumentException("1"),
                    2 to IllegalArgumentException("2")
                )
            }
        val dataLoader = dataLoaderFactory.constructNew()

        val deferred1 = dataLoader.loadAsync(1)
        val deferred2 = dataLoader.loadAsync(2)

        assertThat { deferred1.await() }
            .isFailure()
            .isInstanceOf(IllegalArgumentException::class)
            .transform { it.message }
            .isEqualTo("1")

        assertThat { deferred2.await() }
            .isFailure()
            .isInstanceOf(IllegalArgumentException::class)
            .transform { it.message }
            .isEqualTo("2")
    }

    @Test
    fun assure_factory_doesnt_pass_cache() = runBlockingWithTimeout {
        val dataLoaderFactory = dataLoaderFactory(
            { keys: List<Int> -> keys.map { ExecutionResult.Success(it.toString()) } }
        )

        val loader1 = dataLoaderFactory.constructNew()
        val loader2 = dataLoaderFactory.constructNew()

        val res1 = loader1.loadAsync(2)
        val res2 = loader2.loadAsync(2)

        assertThat(loader1.createStatisticsSnapshot().objectsRequested).isEqualTo(1)
        assertThat(loader2.createStatisticsSnapshot().objectsRequested).isEqualTo(1)

        assertThat(loader1.createStatisticsSnapshot().cacheHitCount).isEqualTo(0)
        assertThat(loader2.createStatisticsSnapshot().cacheHitCount).isEqualTo(0)

        val res3 = loader2.loadAsync(2)
        assertThat(loader2.createStatisticsSnapshot().cacheHitCount).isEqualTo(1)

        loader1.dispatch()
        loader2.dispatch()
        assertThat(listOf(res1.await(), res2.await(), res3.await()))
            .isEqualTo(listOf("2", "2", "2"))
    }

}