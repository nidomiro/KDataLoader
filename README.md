[ ![Download](https://img.shields.io/badge/License-MIT-yellow.svg) ](https://opensource.org/licenses/MIT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.nidomiro/KDataLoader/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.nidomiro/KDataLoader)


# KDataLoader

A Kotlin implementation of [DataLoader](https://github.com/graphql/dataloader).
This library is using [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html).

You can find the full documentation and usage here: [https://nidomiro.github.io/KDataLoader](https://nidomiro.github.io/KDataLoader)

## What is a DataLoader?

In general, a DataLoader is a mechanism to batch multiple load-calls together and execute one call instead of multiple.
The `load(<id>)` call will return a `Deferred` (Future/Promise) which will return the value after calling `dispatch()`.

This way you can avoid fetching every object by its own.
Calling the `load(<id>)`-Function with the same id will return the same `Deferred`.
Therefore no additional request will be done.

## Get

### Gradle-JVM
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'de.nidomiro:KDataLoader-jvm:0.5.0'
}
```

#### Kotlin Multiplatform Project
Currently only `jvm` and `js` is supported.

```groovy
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation 'de.nidomiro:KDataLoader:0.5.0'
            }
        }
    }
}
```

### Maven
 
```xml
<dependency>
    <groupId>de.nidomiro</groupId>
    <artifactId>KDataLoader-jvm</artifactId>
    <version>0.5.0</version>
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

