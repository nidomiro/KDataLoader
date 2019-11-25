plugins {
    kotlin("jvm") version "1.3.60"
}

group = "de.nidomiro"
version = "0.1-SNAPSHOT"

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    jcenter()
}

dependencies {
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")


    // # TEST
    // Use the Kotlin test library.
    testImplementation(kotlin("test"))
    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")

    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
