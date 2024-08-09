# kotlin-flow-graph

A simple flow graph implementation in Kotlin.
It allows you to create a graph of processing nodes that can be connected together to form a processing pipeline.

## Usage

Gradle:

```kotlin
repositories {
    maven("https://repo.silenium.dev/releases")
}

dependencies {
    implementation("dev.silenium.libs.flow-graph:kotlin-flow-graph:0.1.0")
}
```

Examples:

- [Simple](./examples/src/main/kotlin/dev/silenium/libs/flows/examples/Simple.kt)
