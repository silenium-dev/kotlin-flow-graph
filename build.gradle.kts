plugins {
    kotlin("jvm") version "2.0.0"
}

group = "dev.silenium.playground"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val coroutines = "1.8.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutines")

    val kotest = "5.9.0"
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotest")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest")
    testImplementation("io.kotest:kotest-property-jvm:$kotest")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
