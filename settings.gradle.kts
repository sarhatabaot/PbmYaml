rootProject.name = "PbmYaml"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

            library("snake-yaml", "org.yaml:snakeyaml:2.2")
            library("commons-lang", "org.apache.commons:commons-lang3:3.14.0")

            library("junit", "org.junit.jupiter:junit-jupiter-engine:5.10.2")
        }
    }
}