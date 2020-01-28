@file:Suppress("UnstableApiUsage")

apply(plugin = "maven")
apply(plugin = "maven-publish")

/*afterEvaluate {
    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(kotlin.sourceSets.commonMain)
    }
}*/

afterEvaluate {
    configure<PublishingExtension> {
        publications
            .filterIsInstance<MavenPublication>()
            .forEach { publication ->
                publication.pom.withXml {
                    asNode().apply {
                        appendNode("name", rootProject.name)
                        appendNode("url", Constants.ProjectInfo.websiteUrl)
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
                            appendNode("url", Constants.ProjectInfo.vcsUrl)
                            appendNode("connection", Constants.ProjectInfo.vcsConnection)
                        }
                    }
                }
            }
    }
}


//apply(from = rootProject.file("gradle/bintray.gradle.kts"))


