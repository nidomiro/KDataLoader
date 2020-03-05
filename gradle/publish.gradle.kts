@file:Suppress("UnstableApiUsage")

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
            }
    }
}


