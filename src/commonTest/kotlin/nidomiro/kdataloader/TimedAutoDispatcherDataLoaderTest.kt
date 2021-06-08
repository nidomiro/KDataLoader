package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.*
import kotlin.test.Test

class TimedAutoDispatcherDataLoaderTest {

    @Test
    fun load_One_to_test_basic_functionality() = runBlockingWithTimeout {

        val dataLoader = TimedAutoDispatcherImpl(
            TimedAutoDispatcherDataLoaderOptions(),
            identityBatchLoader<Int>()
        )

        val deferredOne = dataLoader.loadAsync(1)
        delay(25)
        assertThat(deferredOne.isCompleted).isFalse()
        delay(101)
        assertThat(deferredOne.await()).isEqualTo(1)
    }

    @Test
    fun load_Multiple_to_test_with_delay() = runBlockingWithTimeout {

        val dataLoader = TimedAutoDispatcherImpl(
            TimedAutoDispatcherDataLoaderOptions(),
            identityBatchLoader<Int>()
        )

        val deferredOne = dataLoader.loadAsync(1)
        delay(50)
        val deferredTwo = dataLoader.loadAsync(2)
        delay(75)
        val deferredThree = dataLoader.loadAsync(3)
        delay(95)
        val deferredFour = dataLoader.loadAsync(4)

        assertThat(deferredOne.isCompleted).isFalse()
        assertThat(deferredTwo.isCompleted).isFalse()
        assertThat(deferredThree.isCompleted).isFalse()
        assertThat(deferredFour.isCompleted).isFalse()
        delay(150)
        assertThat(deferredOne.isCompleted).isTrue()
        assertThat(deferredTwo.isCompleted).isTrue()
        assertThat(deferredThree.isCompleted).isTrue()
        assertThat(deferredFour.isCompleted).isTrue()
        assertThat(deferredOne.await()).isEqualTo(1)
        assertThat(deferredTwo.await()).isEqualTo(2)
        assertThat(deferredThree.await()).isEqualTo(3)
        assertThat(deferredFour.await()).isEqualTo(4)
    }

    @Test
    fun validate_that_it_will_close_upon_coroutine_context() = runBlockingWithTimeout(10_000) {
        val dataLoader = TimedAutoDispatcherImpl(
            TimedAutoDispatcherDataLoaderOptions(),
            identityBatchLoader<Int>(),
            Job()
        )

        val res = dataLoader.loadAsync(25)


        assertThat(dataLoader.createStatisticsSnapshot().dispatchMethodCalled).isEqualTo(0)

        delay(125)
        assertThat(dataLoader.createStatisticsSnapshot().dispatchMethodCalled).isEqualTo(1)
        dataLoader.cancel()

        delay(1_000)
        assertThat(dataLoader.createStatisticsSnapshot().dispatchMethodCalled).isLessThanOrEqualTo(2)

        assertThat(res.await()).isEqualTo(25)
    }

}
