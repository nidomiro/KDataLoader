package nidomiro.kdataloader

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
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

    @Test
    fun `create DataLoader with prime throwables`() {

        val dataLoader = dataLoader<Int, String> {
            batchLoader = { keys -> keys.map { ExecutionResult.Success(it.toString()) } }

            prime(
                1 to IllegalArgumentException("1"),
                2 to IllegalArgumentException("2")
            )

        }

        runBlockingWithTimeout {
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
    }

    @Test
    fun `create a basic DataLoader with options`() {

        val dataLoader = dataLoader<Int, String> {
            batchLoader = { keys -> keys.map { ExecutionResult.Success(it.toString()) } }
            configure {
                batchLoadEnabled = true
                batchSize = 1
                cacheEnabled = false
                cacheExceptions = false
            }
        }

        assertThat(dataLoader.options.batchLoadEnabled).isEqualTo(true)
        assertThat(dataLoader.options.batchSize).isEqualTo(1)
        assertThat(dataLoader.options.cacheEnabled).isEqualTo(false)
        assertThat(dataLoader.options.cacheExceptions).isEqualTo(false)


    }
}