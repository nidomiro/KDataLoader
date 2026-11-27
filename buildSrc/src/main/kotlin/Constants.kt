object Constants {
    object ProjectInfo {
        const val vcsUrl = "https://github.com/nidomiro/KDataLoader"
        const val vcsConnection = "scm:git:git://github.com/nidomiro/KDataLoader"
        const val websiteUrl = "https://nidomiro.github.io/KDataLoader"

        object License {
            const val name = "MIT"
            const val url = "https://opensource.org/licenses/MIT"
            const val distribution = "repo"
        }

        val developer: Collection<Developer> = listOf(
            Developer("nidomiro", "Niclas Roßberger")
        )

    }

    object BuildLibVersions {
        const val kotlin = "2.4.21"
    }

}

data class Developer(
    val id: String,
    val name: String? = null,
    val email: String? = null
)

