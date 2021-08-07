import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "org"
version = "0.1"

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
            setSrcDirs(listOf("lib/main", "read/main", "collate/main", "report/main"))
        }
    }

    test {
        java {
            setSrcDirs(listOf("lib/test", "read/test", "collate/test", "report/test"))
        }
    }
}
