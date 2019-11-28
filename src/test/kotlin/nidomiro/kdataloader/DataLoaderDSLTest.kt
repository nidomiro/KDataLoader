package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.isEqualTo
import nidomiro.kdataloader.dsl.dataLoader
import org.junit.jupiter.api.Test

class DataLoaderDSLTest {


    @Test
    fun `create a basic DataLoader`() {

        val dataLoader = dataLoader<Int, String> {
            batchLoader = { keys -> keys.map { ExecutionResult.Success(it.toString()) } }
        }

        runBlockingWithTimeout {
            val deferred1 = dataLoader.loadAsync(1)
            dataLoader.dispatch()
            assertThat(deferred1.await()).isEqualTo("1")
        }


    }

    @Test
    fun `create DataLoader with prime`() {

        val dataLoader = dataLoader<Int, String> {
            batchLoader = { keys -> keys.map { ExecutionResult.Success(it.toString()) } }

            prime(
                1 to "1",
                2 to "2"
            )

        }

        runBlockingWithTimeout {
            val deferred1 = dataLoader.loadAsync(1)
            val deferred2 = dataLoader.loadAsync(2)
            assertThat(deferred1.await()).isEqualTo("1")
            assertThat(deferred2.await()).isEqualTo("2")
        }


    }
}