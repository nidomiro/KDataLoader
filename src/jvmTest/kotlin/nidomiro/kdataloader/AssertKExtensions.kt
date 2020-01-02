package nidomiro.kdataloader

import assertk.Assert
import assertk.assertions.isEqualTo


fun <T> Assert<List<T>>.containsExactly(vararg elements: T?) {
    isEqualTo(elements.toList())
}