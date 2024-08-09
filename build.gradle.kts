import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "2.0.10"
    `maven-publish`
}

group = "dev.silenium.libs.flow-graph"
version = findProperty("deploy.version") as String? ?: "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val coroutines = "1.8.1"
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")

    val kotest = "5.9.1"
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotest")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest")
    testImplementation("io.kotest:kotest-property-jvm:$kotest")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutines")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        languageVersion = KotlinVersion.KOTLIN_1_7
    }
}

tasks.compileTestKotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        languageVersion = KotlinVersion.DEFAULT
    }
}

kotlin {
    jvmToolchain(8)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        val url = System.getenv("MAVEN_REPO_URL") ?: return@repositories
        maven(url) {
            name = "reposilite"
            credentials {
                username = System.getenv("MAVEN_REPO_USERNAME") ?: ""
                password = System.getenv("MAVEN_REPO_PASSWORD") ?: ""
            }
        }
    }
}
