buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${Constants.BuildLibVersions.bintray}")
    }

}


apply(plugin = "maven")
apply(plugin = "maven-publish")
apply(plugin = "com.jfrog.bintray")

configure<com.jfrog.bintray.gradle.BintrayExtension> {
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
        vcsUrl = Constants.ProjectInfo.vcsUrl
        websiteUrl = Constants.ProjectInfo.websiteUrl
        issueTrackerUrl = "${Constants.ProjectInfo.websiteUrl}/issues"

        version.apply {
            name = project.version.toString()
            released = java.util.Date().toString()
        }
    }

}

tasks.withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
    doFirst {
        this@withType.publications
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