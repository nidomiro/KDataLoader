import java.util.*

plugins {
    maven
    `maven-publish`
    id("com.jfrog.bintray") version Constants.BuildLibVersions.bintray
    kotlin("multiplatform") version Constants.BuildLibVersions.kotlin
}

group = "de.nidomiro"
version = "0.3.0"

repositories {
    mavenCentral()
    jcenter()
}

@Suppress("UNUSED_VARIABLE")
kotlin {
    //Targets
    jvm {
        val javaVersion = "1.8"

        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = javaVersion
            }
        }
        val test by compilations.getting {
            kotlinOptions {
                jvmTarget = javaVersion
            }
        }
    }

    js {
        nodejs {

        }
        //browser { }
        val main by compilations.getting {
            kotlinOptions {
                sourceMap = true
                moduleKind = "umd"
            }
        }
        val test by compilations.getting {
            kotlinOptions {
                sourceMap = true
                moduleKind = "umd"
            }
        }

    }



    @Suppress("UNUSED_VARIABLE")
    sourceSets {

        val coroutinesVersion = "1.3.3"
        val assertkVersion = "0.21"
        val junitVersion = "5.5.2"

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation("com.willowtreeapps.assertk:assertk:$assertkVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }


}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

apply(from = rootProject.file("gradle/publish.gradle.kts"))


// Bintray stuff (doesn't work in a separate file right now)
bintray {
    user = if (project.hasProperty("bintray_user")) project.property("bintray_user") as String else ""
    key = if (project.hasProperty("bintray_key")) project.property("bintray_key") as String else ""
    //publish = true
    override = true


    pkg.apply {
        repo = "maven"
        name = project.name
        setLicenses("MIT")
        setLabels("kotlin", "Kotlin", "Multiplatform", "DataLoader", "GraphQL")
        vcsUrl = Constants.ProjectInfo.vcsUrl
        websiteUrl = Constants.ProjectInfo.websiteUrl
        issueTrackerUrl = "${Constants.ProjectInfo.websiteUrl}/issues"

        version.apply {
            name = project.version.toString()
            vcsTag = project.version.toString()
            released = Date().toString()

        }
    }

}
tasks.named("bintrayUpload") {
    dependsOn(":publishToMavenLocal")
}

tasks.withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
    doFirst {
        project.publishing.publications
            .filterIsInstance<MavenPublication>()
            .forEach { publication ->
                val moduleFile = buildDir.resolve("publications/${publication.name}/module.json")
                if (moduleFile.exists()) {
                    publication.artifact(object :
                        org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact(moduleFile) {
                        override fun getDefaultExtension() = "module"
                    })
                }
            }
    }
}

apply(from = rootProject.file("gradle/groovyTasks.gradle"))
