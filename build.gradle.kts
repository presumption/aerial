import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
}

group = "org"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("info.picocli:picocli:4.6.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "15"
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/scanner/main", "src/cli/main"))
        }
    }

    test {
        java {
            setSrcDirs(listOf("src/scanner/test"))
        }
    }
}

tasks.withType<Jar>() {
    manifest {
        attributes(
            "Main-Class" to "org.aerial.AerialKt"
        )
    }
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
