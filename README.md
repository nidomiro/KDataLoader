[ ![Download](https://img.shields.io/badge/License-MIT-yellow.svg) ](https://opensource.org/licenses/MIT)
[ ![Download](https://api.bintray.com/packages/nidomiro/maven/KDataLoader/images/download.svg) ](https://bintray.com/nidomiro/maven/KDataLoader/_latestVersion)

# KDataLoader

A Kotlin implementation of [DataLoader](https://github.com/graphql/dataloader).
This library is using [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html).

You can find the full documentation and usage here: [https://nidomiro.github.io/KDataLoader](https://nidomiro.github.io/KDataLoader)

## What is a DataLoder?

In general, a DataLoader is a mechanism to batch multiple load-calls together and execute one call instead of multiple.
The `load(<id>)` call will return a `Deferred` (Future/Promise) which will return the value after calling `dispatch()`.

This way you can avoid fetching every object by it's own.
Calling the `load(<id>)`-Function with the same id will return the same `Deferred`.
Therefore no additional request will be done.

## Get

### Gradle
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'de.nidomiro:KDataLoader-jvm:0.2.0'
}
```

#### Kotlin Multiplatform Project
Currently only `jvm` is supported.

```groovy
repositories {
    jcenter()
}

kotlin {
    // [...]
    sourceSets {
        commonMain {
            dependencies {
                // [...]
                implementation 'de.nidomiro:KDataLoader-common:0.2.0'
            }
        }
        // [...]
        jvmMain {
            dependencies {
                // [...]
                implementation 'de.nidomiro:KDataLoader-jvm:0.2.0'
            }
        }
        // [...]
    }
}
```

### Maven
You need to setup the [`jcenter`-Repository](https://bintray.com/beta/#/bintray/jcenter?tab=packages) in order to get the lib via maven.
 
```xml
<dependency>
    <groupId>de.nidomiro</groupId>
    <artifactId>KDataLoader-jvm</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Basic usage

For more examples and options visit the [documentation](https://nidomiro.github.io/KDataLoader).

```kotlin
fun main(): Unit = runBlocking { 
    val batchLoader: BatchLoader<Int, Int> = { keys -> keys.map { ExecutionResult.Success(it) } }
    val dataLoader = dataLoader(batchLoader)

    val value1 = dataLoader.loadAsync(1)
    val value2 = dataLoader.loadAsync(2)
    dataLoader.dispatch()

    println("1 -> ${value1.await()}")
    println("2 -> ${value2.await()}")
}
```

## Documentation

For a more detailed explanation look at the [documentation and examples](https://nidomiro.github.io/KDataLoader).

## Versioning

This Library uses [Semantic Versioning](https://semver.org/).

## License

This Library is Licensed under the [MIT License](https://opensource.org/licenses/MIT).

