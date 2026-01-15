import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.Properties
import javax.inject.Inject
import org.gradle.process.ExecOperations

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

abstract class RunServerTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun run() {
        val runDir = File(project.projectDir, "run")
        if (!runDir.exists()) runDir.mkdirs()
        
        val modsDir = File(runDir, "mods")
        if (!modsDir.exists()) modsDir.mkdirs()

        var jarFile = File(runDir, "HytaleServer.jar")
        
        if (!jarFile.exists()) {
            val userHome = System.getProperty("user.home")
            val os = System.getProperty("os.name").lowercase()
            val candidates = mutableListOf<File>()

            if (os.contains("win")) {
                candidates.add(File("$userHome/AppData/Roaming/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"))
            } else if (os.contains("mac")) {
                candidates.add(File("$userHome/Application Support/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"))
                candidates.add(File("$userHome/Library/Application Support/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"))
            } else {
                val xdgDataHome = System.getenv("XDG_DATA_HOME")
                val linuxBase = if (!xdgDataHome.isNullOrEmpty()) xdgDataHome else "$userHome/.local/share"
                candidates.add(File("$linuxBase/Hytale/install/release/package/game/latest/Server/HytaleServer.jar"))
            }

            val standardPath = candidates.firstOrNull { it.exists() }
            if (standardPath != null) {
                println("Found HytaleServer.jar at standard location: ${standardPath.absolutePath}")
                jarFile = standardPath
            } else {
                println("Hytale server JAR not found in ${runDir.absolutePath} or standard locations: ${candidates.map { it.absolutePath }}")
                throw RuntimeException("HytaleServer.jar not found! Please place it in the 'run' folder or ensure Hytale is installed.")
            }
        }

        // Copy plugin JAR
        project.tasks.findByName("shadowJar")?.outputs?.files?.firstOrNull()?.let { shadowJar ->
            val targetFile = File(modsDir, shadowJar.name)
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
        javaArgs.add("--allow-op")
        
        // If we are using an external JAR, point assets to the right place relative to it
        val defaultJarInRun = File(runDir, "HytaleServer.jar")
        if (jarFile.absolutePath != defaultJarInRun.absolutePath) {
             val assetsFile = File(jarFile.parentFile.parentFile, "Assets.zip")
             if (assetsFile.exists()) {
                 javaArgs.add("--assets")
                 javaArgs.add(assetsFile.absolutePath)
             }
        } else {
             javaArgs.add("--assets")
             javaArgs.add("Assets.zip")
        }

        // Use the same Java as Gradle or find one
        val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        
        execOperations.exec {
            workingDir = runDir
            commandLine = listOf(javaBin) + javaArgs
            standardInput = System.`in`
        }
    }
}
