import java.util.*


val myVcsUrl = "https://github.com/nidomiro/KDataLoader"
val myVcsConnection = "scm:git:git://github.com/nidomiro/KDataLoader"
val myWebsiteUrl = "https://github.com/nidomiro/KDataLoader"

plugins {
    `build-scan`
    kotlin("jvm") version "1.3.60"
    id("com.jfrog.bintray") version "1.8.4"
    `maven-publish`
}

group = "de.nidomiro"
version = "0.0.1"

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
    jcenter()
}

dependencies {
    // Use the Kotlin JDK 8 standard library.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
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

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}




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
            from(components["java"])
            artifactId = project.name
            groupId = project.group.toString()
            version = project.version.toString()
            artifact(sourcesJar)

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
