[ ![Download](https://img.shields.io/badge/License-MIT-yellow.svg) ](https://opensource.org/licenses/MIT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.nidomiro/KDataLoader/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.nidomiro/KDataLoader)


# KDataLoader

A pure, multiplatform (js and jvm) Kotlin implementation of [DataLoader](https://github.com/graphql/dataloader).
This library is using [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html).


## What is a DataLoader?

A DataLoader is a mechanism to optimize data-fetching.
Instead of issuing multiple calls one after the other, the DataLoader will collect all calls and execute them together as one.
This alone has the huge benefit of fewer calls to your db/api, therefore reducing load.
The second optimization is to eliminate duplicate calls.

### Example

Assumption: You want to fetch a list of Projects with their members.
The relation between Project and Person is m:n, therefore a Person can work in multiple projects.

The data:
```json
{
   "projects":[
      {
         "name":"Project A",
         "members":[
            {
               "id":1,
               "name":"John"
            },
            {
               "id":2,
               "name":"Anne"
            }
         ]
      },
      {
         "name":"Project B",
         "members":[
            {
               "id":2,
               "name":"Anne"
            }
         ]
      },
      {
         "name":"Project C",
         "members":[
            {
               "id":1,
               "name":"John"
            },
            {
               "id":3,
               "name":"Peter"
            }
         ]
      }
   ]
}
```

In a **naive** approach you will need **6 requests** to get all your data:

1. fetch all Projects
2. fetch John
3. fetch Anne
4. fetch Anne
5. fetch John
6. fetch Peter

The same scenario can be optimized to **2 requests** with a **DataLoader**:

1. fetch all Projects
2. fetch John, Anne and Peter in batch


How does it work?

If you want to load the Person with id 1, you simply call `dataLoader.loadAsync(1)`.
You won't get the result immediately, but you'll get a `Deferred`, which is an equivalent of a `Future` or `Promise` in other languages.

In the example above the calls will be:

1. `dataLoader.loadAsync(1)`
2. `dataLoader.loadAsync(2)`
3. `dataLoader.loadAsync(2)`
4. `dataLoader.loadAsync(1)`
5. `dataLoader.loadAsync(3)`

By now no call to actually fetch the data has been made.
The actual fetch happens when you call `dataLoader.dispatch()`.
In this case the ids 1,2 and 3 will be fetched in one call.
Since calling `dataLoader.loadAsync()` with the same id will result in the same `Deffered` being returned, no id is fetched more than once.



## Install

### Kotlin Multiplatform Project
Currently, only `jvm` and `js` is supported.

```kotlin
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("de.nidomiro:KDataLoader:0.5.1")
            }
        }
    }
}
```

For JVM-only or js only use the following instead of `de.nidomiro:KDataLoader`:
* JVM: `de.nidomiro:KDataLoader-jvm`
* JS: `de.nidomiro:KDataLoader-js`

### Maven
 
```xml
<dependency>
    <groupId>de.nidomiro</groupId>
    <artifactId>KDataLoader-jvm</artifactId>
    <version>0.5.1</version>
</dependency>
```

## Usage

Since this library uses [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) every example assumes you are in a coroutine.

```kotlin
val batchLoader: BatchLoader<Int, Int> =
    { keys -> keys.map { ExecutionResult.Success(it) } }

val dataLoader = dataLoader(batchLoader) {

    configure {
        // all default-values
        cache = DefaultCacheImpl()
        cacheEnabled = true
        cacheExceptions = true
        batchLoadEnabled = true
        batchSize = Int.MAX_VALUE
    }

    prime(1 to 1) // prime the cache
}

val value1 = dataLoader.loadAsync(1)
val value2 = dataLoader.loadAsync(2)
dataLoader.dispatch() // actually fetch the data

println("1 -> ${value1.await()}") // from cache
println("2 -> ${value2.await()}") // loaded via BatchLoader
```


## Versioning

This Library uses [Semantic Versioning](https://semver.org/).

## License

This Library is Licensed under the [MIT License](https://opensource.org/licenses/MIT).

