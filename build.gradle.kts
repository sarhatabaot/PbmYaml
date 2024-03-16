plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.snake.yaml)
    api(libs.commons.lang)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
