import java.util.*


val myVcsUrl = "https://github.com/nidomiro/KDataLoader"
val myVcsConnection = "scm:git:git://github.com/nidomiro/KDataLoader"
val myWebsiteUrl = "https://nidomiro.github.io/KDataLoader"

val coroutinesVersion = "1.3.3"

plugins {
    `build-scan`
    kotlin("multiplatform") version "1.3.61"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "de.nidomiro"
version = "0.0.2"

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    jcenter()
}

kotlin {
    //Targets
    jvm {
        @Suppress("UNUSED_VARIABLE") val main by compilations.getting {
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "1.8"
            }
        }
        @Suppress("UNUSED_VARIABLE") val test by compilations.getting {
            kotlinOptions {
                // Setup the Kotlin compiler options for the 'main' compilation:
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        @Suppress("UNUSED_VARIABLE") val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
            }
        }
        @Suppress("UNUSED_VARIABLE") val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        // JVM-specific tests and their dependencies:
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
                implementation("org.junit.jupiter:junit-jupiter:5.5.2")
            }
        }
    }


}

/*
dependencies {
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")


    // # TEST
    // Use the Kotlin test library.
    testImplementation(kotlin("test"))
    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")

    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
    //testImplementation("org.assertj:assertj-core:3.14.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
 */

/*
val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}
 */

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
}



bintray {
    user = if (project.hasProperty("bintray_user")) project.property("bintray_user") as String else ""
    key = if (project.hasProperty("bintray_key")) project.property("bintray_key") as String else ""
    publish = true
    setPublications("MyPublication")
    //setConfigurations("archives")

    pkg.apply {
        repo = "maven"
        name = project.name
        setLicenses("MIT")
        setLabels("kotlin", "Kotlin", "DataLoader", "GraphQL")
        vcsUrl = myVcsUrl
        websiteUrl = myWebsiteUrl
        issueTrackerUrl = "$myVcsUrl/issues"

        version.apply {
            name = project.version.toString()
            released = Date().toString()
        }
    }

}

publishing {
    publications {
        create<MavenPublication>("MyPublication") {
            println(components.names)
            from(components["kotlin"])
            artifactId = project.name
            groupId = project.group.toString()
            version = project.version.toString()
            //artifact(sourcesJar)

            pom.withXml {
                asNode().apply {
                    appendNode("name", rootProject.name)
                    appendNode("url", myWebsiteUrl)
                    appendNode("licenses")
                        .appendNode("license").apply {
                            appendNode("name", "MIT")
                            appendNode("url", "https://opensource.org/licenses/MIT")
                            appendNode("distribution", "repo")
                        }
                    appendNode("developers")
                        .appendNode("developer").apply {
                            appendNode("id", "nidomiro")
                            appendNode("name", "Niclas Roßberger")
                        }
                    appendNode("scm").apply {
                        appendNode("url", myVcsUrl)
                        appendNode("connection", myVcsConnection)
                    }
                }
            }

        }
    }
}
