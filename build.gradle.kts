import java.util.*

plugins {
    `maven-publish`
    signing
    kotlin("multiplatform") version Constants.BuildLibVersions.kotlin
}

group = "de.nidomiro"
version = "0.5.2"

repositories {
    mavenCentral()
}

@Suppress("UNUSED_VARIABLE")
kotlin {
    //Targets
    jvm {
        val javaVersion = "1.8"

        compilations.all {
            kotlinOptions.jvmTarget = javaVersion
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(BOTH) {
        // binaries.executable()
        nodejs { }
        browser { }

        compilations.all {
            kotlinOptions.sourceMap = true
            kotlinOptions.moduleKind = "umd"
        }
    }

    val nativeTargets = arrayOf(
        "linuxX64",
        "macosX64", "macosArm64",
        "iosArm32", "iosArm64", "iosX64", "iosSimulatorArm64",
        "tvosArm64", "tvosX64", "tvosSimulatorArm64",
        "watchosArm32", "watchosArm64", "watchosX86", "watchosX64", "watchosSimulatorArm64",

        // Not supported by assertk
        // "mingwX64",

        // Not supported by coroutines
        // "wasm32", "linuxArm64", "linuxMips32", "linuxMipsel32",
    )

    for (target in nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }




    @Suppress("UNUSED_VARIABLE")
    sourceSets {

        val coroutinesVersion = "1.6.4"
        val assertkVersion = "0.25"
        val junitVersion = "5.8.2"

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                //implementation("org.jetbrains.kotlin:atomicfu:1.6.4") // Workaround for https://github.com/Kotlin/kotlinx.coroutines/issues/3305
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

        val nativeMain = create("nativeMain") {
            dependsOn(commonMain)
        }
        val nativeTest = create("nativeTest") {
            dependsOn(commonTest)
        }
        for (sourceSet in nativeTargets) {
            getByName("${sourceSet}Main") {
                dependsOn(nativeMain)
            }
            getByName("${sourceSet}Test") {
                dependsOn(nativeTest)
            }
        }


    }


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
    ext["signing.gnupg.keyName"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.gnupg.passphrase"] = System.getenv("SIGNING_PASSWORD")
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

    // create task to publish all apple (macos, ios, tvos, watchos) artifacts
    @Suppress("UNUSED_VARIABLE")
    val publishApple by tasks.registering {
        publications.all {
            if (name.contains(Regex("macos|ios|tvos|watchos"))) {
                dependsOn("publish${name.capitalize(Locale.ROOT)}PublicationToSonatypeRepository")
            }
        }
    }
}

signing {
    if(System.getenv("USE_GPG")?.toBoolean() == true) {
        useGpgCmd()
    }
    sign(publishing.publications)
}