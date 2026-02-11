import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class BootstrapPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register<CopyScriptsTask>("copyScripts") {
            description = "Copies built script JARs to the DreamBot Scripts directory"
            group = "dreambot"
        }
    }
}
