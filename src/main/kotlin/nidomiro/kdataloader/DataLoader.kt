
package nidomiro.kdataloader

import kotlinx.coroutines.Deferred


class DataLoader<T, R>(val batchLoader: (ids: Collection<T>) -> List<R>) {

    public fun load(id: T): Deferred<T> {

    }
}
