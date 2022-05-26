import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "org"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.9.0")
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
            setSrcDirs(listOf("lib/main", "read/main", "report/main", "cli"))
        }
    }

    test {
        java {
            setSrcDirs(listOf("lib/test", "read/test", "report/test"))
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
