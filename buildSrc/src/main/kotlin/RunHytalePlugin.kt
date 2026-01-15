import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.Properties

open class RunHytalePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val runTask = project.tasks.register("runServer", RunServerTask::class.java) {
            group = "hytale"
            description = "Runs the Hytale server with your plugin"
        }

        project.tasks.findByName("shadowJar")?.let {
            runTask.configure {
                dependsOn(it)
            }
        }
    }
}

open class RunServerTask : DefaultTask() {

    @TaskAction
    fun run() {
        val runDir = File(project.projectDir, "run")
        if (!runDir.exists()) runDir.mkdirs()
        
        val pluginsDir = File(runDir, "plugins")
        if (!pluginsDir.exists()) pluginsDir.mkdirs()

        // Locate Hytale Server JAR
        // 1. Check local run folder
        // 2. Check standard Hytale install location (Windows)
        var jarFile = File(runDir, "HytaleServer.jar")
        
        if (!jarFile.exists()) {
            val userHome = System.getProperty("user.home")
            // Try to find it in the standard location (assuming release channel)
            val standardPath = File("$userHome/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar")
            if (standardPath.exists()) {
                println("Found HytaleServer.jar at standard location: ${standardPath.absolutePath}")
                jarFile = standardPath
            } else {
                println("Hytale server JAR not found in ${runDir.absolutePath} or standard location.")
                throw RuntimeException("HytaleServer.jar not found! Please place it in the 'run' folder or ensure Hytale is installed.")
            }
        }

        // Copy plugin JAR
        project.tasks.findByName("shadowJar")?.outputs?.files?.firstOrNull()?.let { shadowJar ->
            val targetFile = File(pluginsDir, shadowJar.name)
            shadowJar.copyTo(targetFile, overwrite = true)
            println("Plugin copied to: ${targetFile.absolutePath}")
        }

        println("Starting Hytale server...")
        
        val javaArgs = mutableListOf<String>()
        // Enable debug if requested
        if (project.hasProperty("debug")) {
            javaArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
        }
        
        javaArgs.addAll(listOf("-jar", jarFile.absolutePath))
        // Add args required for Hytale to run
        javaArgs.add("--allow-op")
        
        // If we are using the external JAR, we might need to point assets to the right place
        if (jarFile.absolutePath.contains("AppData")) {
             val assetsFile = File(jarFile.parentFile.parentFile, "Assets.zip") // Server/../Assets.zip ? Check path.
             // Based on previous build.gradle: .../game/latest/Assets.zip is parent of Server/? No.
             // previous: .../game/latest/Server/HytaleServer.jar
             // previous asset: .../game/latest/Assets.zip
             // So assets is in jarFile.parentFile.parentFile
             if (File(jarFile.parentFile.parentFile, "Assets.zip").exists()) {
                 javaArgs.add("--assets")
                 javaArgs.add(File(jarFile.parentFile.parentFile, "Assets.zip").absolutePath)
             }
        } else {
             javaArgs.add("--assets")
             javaArgs.add("Assets.zip")
        }

        // Use the same Java as Gradle or find one
        val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        
        val process = ProcessBuilder(javaBin, *javaArgs.toTypedArray())
            .directory(runDir)
            .redirectErrorStream(true)
            .start()

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            if (process.isAlive) process.destroy()
        })

        // Pipe output
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { println(it) }
        }
        
        process.waitFor()
    }
}
