package nidomiro.kdataloader.statistics

import assertk.assertThat
import assertk.assertions.isEqualTo
import nidomiro.kdataloader.*
import kotlin.test.Test

@Suppress("DeferredResultUnused")
class DataLoaderStatisticsTest {

    @Test
    fun `test_loadAsyncMethodCalled_statistics`() = runBlockingWithTimeout {
        val dataLoader: DataLoader<Int, Int> =
            SimpleDataLoaderImpl(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.loadAsync(1)
        assertThat(dataLoader.createStatisticsSnapshot().loadAsyncMethodCalled).isEqualTo(1)

        dataLoader.loadAsync(1)
        assertThat(dataLoader.createStatisticsSnapshot().loadAsyncMethodCalled).isEqualTo(2)

        dataLoader.loadAsync(2)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadAsyncMethodCalled = 3,
                objectsRequested = 3,
                cacheHitCount = 1
            )
        )
    }

    @Test
    fun `test_loadManyAsyncMethodCalled_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.loadManyAsync(1)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(1)

        dataLoader.loadManyAsync(1)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(2)

        dataLoader.loadManyAsync(2)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadManyAsyncMethodCalled = 3,
                objectsRequested = 3,
                cacheHitCount = 1
            )
        )
    }

    @Test
    fun `test_dispatchMethodCalled_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.loadAsync(1)
        dataLoader.dispatch()
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadAsyncMethodCalled = 1,
                objectsRequested = 1,
                dispatchMethodCalled = 1,
                batchCallsExecuted = 1
            )
        )

        dataLoader.dispatch()
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadAsyncMethodCalled = 1,
                objectsRequested = 1,
                dispatchMethodCalled = 2,
                batchCallsExecuted = 1
            )
        )

    }

    @Test
    fun `test_clearMethodCalled_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.clear(1)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                clearMethodCalled = 1
            )
        )

        dataLoader.clear(1)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                clearMethodCalled = 2
            )
        )
    }

    @Test
    fun `test_clearAllMethodCalled_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.clearAll()
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                clearAllMethodCalled = 1
            )
        )

        dataLoader.clearAll()
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                clearAllMethodCalled = 2
            )
        )
    }

    @Test
    fun `test_primeMethodCalled_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.prime(1, 1)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                primeMethodCalled = 1
            )
        )

        dataLoader.prime(1, 1)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                primeMethodCalled = 2
            )
        )

        dataLoader.prime(2, 4)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                primeMethodCalled = 3
            )
        )

        dataLoader.prime(1, Exception("ABC"))
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                primeMethodCalled = 4
            )
        )

        dataLoader.prime(5, Exception("ABC"))
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                primeMethodCalled = 5
            )
        )
    }

    @Test
    fun `test_objectsRequested_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.loadManyAsync(1, 2)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(1)

        dataLoader.loadManyAsync(1, 2)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(2)

        dataLoader.loadManyAsync(3, 4)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadManyAsyncMethodCalled = 3,
                objectsRequested = 6,
                cacheHitCount = 2
            )
        )
    }


    @Test
    fun `test_batchCallsExecuted_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.loadManyAsync(1, 2, 4)
        dataLoader.dispatch()
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(1)

        dataLoader.loadManyAsync(2, 3)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(2)

        dataLoader.loadManyAsync(1, 2, 3, 4)
        dataLoader.dispatch()

        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadManyAsyncMethodCalled = 3,
                dispatchMethodCalled = 2,
                objectsRequested = 9,
                batchCallsExecuted = 2,
                cacheHitCount = 5
            )
        )
    }

    @Test
    fun `test_batchCallsExecuted_statistics_with_batchSizeOption`() =
        runBlockingWithTimeout {
            val dataLoader = SimpleDataLoaderImpl<Int, Int>(
                DataLoaderOptions(batchSize = 2),
                identityBatchLoader()
            )
            assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

            dataLoader.loadManyAsync(1, 2, 3)
            dataLoader.dispatch()
            assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(1)

            dataLoader.loadManyAsync(4, 5)
            dataLoader.dispatch()
            assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(2)

            dataLoader.loadManyAsync(1, 2, 3, 4, 5, 6, 7)
            dataLoader.dispatch()

            assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
                DataLoaderStatistics(
                    loadManyAsyncMethodCalled = 3,
                    dispatchMethodCalled = 3,
                    objectsRequested = 12,
                    batchCallsExecuted = 4,
                    cacheHitCount = 5
                )
            )
        }


    @Test
    fun `test_cacheHitCount_statistics`() = runBlockingWithTimeout {
        val dataLoader =
            SimpleDataLoaderImpl<Int, Int>(identityBatchLoader())
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(DataLoaderStatistics())

        dataLoader.loadManyAsync(1, 2, 3)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(1)

        dataLoader.loadManyAsync(1, 2)
        assertThat(dataLoader.createStatisticsSnapshot().loadManyAsyncMethodCalled).isEqualTo(2)

        dataLoader.loadManyAsync(2, 3)
        assertThat(dataLoader.createStatisticsSnapshot()).isEqualTo(
            DataLoaderStatistics(
                loadManyAsyncMethodCalled = 3,
                objectsRequested = 7,
                cacheHitCount = 4
            )
        )
    }

}