@file:Suppress("UnstableApiUsage")

apply(plugin = "maven")
apply(plugin = "maven-publish")

afterEvaluate {
    configure<PublishingExtension> {
        publications
            .filterIsInstance<MavenPublication>()
            .forEach { publication ->
                publication.pom {
                    name.set(rootProject.name)
                    url.set(Constants.ProjectInfo.websiteUrl)

                    licenses {
                        license {
                            name.set(Constants.ProjectInfo.License.name)
                            url.set(Constants.ProjectInfo.License.url)
                            distribution.set(Constants.ProjectInfo.License.distribution)
                        }
                    }

                    developers {
                        Constants.ProjectInfo.developer.forEach { developer ->
                            developer {
                                id.set(developer.id)
                                developer.name?.let(name::set)
                                developer.email?.let(email::set)
                            }
                        }

                    }
                    scm {
                        url.set(Constants.ProjectInfo.vcsUrl)
                        connection.set(Constants.ProjectInfo.vcsConnection)
                    }
                }

                /*
                publication.pom.withXml {
                    asNode().apply {
                        appendNode("name", rootProject.name)
                        appendNode("url", Constants.ProjectInfo.websiteUrl)
                        appendNode("licenses")
                            .appendNode("license").apply {
                                appendNode("name", Constants.ProjectInfo.License.name)
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
                 */
            }
    }
}


