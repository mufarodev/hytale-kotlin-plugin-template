plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.0"
    id("run-hytale")
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

group = findProperty("pluginGroup") as String
version = findProperty("pluginVersion") as String

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    val hytaleJar = run {
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()
        val patchline = "release"
        val relativePath = "install/$patchline/package/game/latest/Server/HytaleServer.jar"

        val candidates = mutableListOf<String>()
        if (os.contains("win")) {
            candidates.add("$userHome/AppData/Roaming/Hytale/$relativePath")
        } else if (os.contains("mac")) {
            candidates.add("$userHome/Application Support/Hytale/$relativePath")
            candidates.add("$userHome/Library/Application Support/Hytale/$relativePath")
        } else {
            val xdgDataHome = System.getenv("XDG_DATA_HOME")
            val linuxBase = if (!xdgDataHome.isNullOrEmpty()) xdgDataHome else "$userHome/.local/share"
            candidates.add("$linuxBase/Hytale/$relativePath")
        }
        candidates.firstOrNull { File(it).exists() } ?: candidates.first()
    }

    // Hytale Server API
    compileOnly(files(hytaleJar))
    
    // Common dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(kotlin("test"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }
    
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
        }
    }
    
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        val props = mapOf(
            "version" to project.version
        )
        filesMatching("manifest.json") {
            expand(props)
        }
    }
    
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
    }
    
    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
