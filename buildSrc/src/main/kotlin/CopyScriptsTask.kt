import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class CopyScriptsTask : DefaultTask() {
    @TaskAction
    fun copyScripts() {
        if (project != project.rootProject) return

        val scriptsDir = File(System.getProperty("user.home"), "DreamBot/Scripts")
        scriptsDir.mkdirs()

        project.subprojects.forEach { sub ->
            val jarDir = sub.layout.buildDirectory.dir("libs").get().asFile
            if (jarDir.exists()) {
                jarDir.listFiles()?.filter { it.extension == "jar" }?.forEach { jarFile ->
                    val dest = File(scriptsDir, jarFile.name)
                    jarFile.copyTo(dest, overwrite = true)
                    println("Copied ${jarFile.name} -> ${dest.absolutePath}")
                }
            }
        }
    }
}
