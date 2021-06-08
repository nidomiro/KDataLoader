package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.delay
import kotlin.test.Test

class AutoDispatcherDataLoaderTest {

    @Test
    fun load_One_to_test_basic_functionality() = runBlockingWithTimeout {

        val dataLoader = AutoDispatcherDataLoaderImpl(
            AutoDispatcherDataLoaderOptions(),
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

        val dataLoader = AutoDispatcherDataLoaderImpl(
            AutoDispatcherDataLoaderOptions(),
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

}
