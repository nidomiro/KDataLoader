import java.util.*

plugins {
    `maven-publish`
    signing
    kotlin("multiplatform") version Constants.BuildLibVersions.kotlin
}

group = "de.nidomiro"
version = "0.3.0"

repositories {
    mavenCentral()
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

        val coroutinesVersion = "1.3.9"
        val assertkVersion = "0.22"
        val junitVersion = "5.6.2"

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
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


// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }
    publications.withType<MavenPublication> {

        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("KDataLoader")
            description.set("A Kotlin implementation of dataloader")
            url.set(Constants.ProjectInfo.websiteUrl)

            licenses {
                license {
                    name.set(Constants.ProjectInfo.License.name)
                    url.set(Constants.ProjectInfo.License.url)
                    distribution.set(Constants.ProjectInfo.License.distribution)
                }
            }
            developers {
                for (dev in Constants.ProjectInfo.developer) {
                    developer {
                        id.set(dev.id)
                        name.set(dev.name)
                        email.set(dev.email)
                    }
                }
            }
            scm {
                url.set(Constants.ProjectInfo.vcsUrl)
                connection.set(Constants.ProjectInfo.vcsConnection)
            }

        }
    }
}

signing {
    sign(publishing.publications)
}

/*
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
 */