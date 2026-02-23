import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class BootstrapPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val copyScripts = project.tasks.register<CopyScriptsTask>("copyScripts") {
            description = "Copies built script JARs to the DreamBot Scripts directory"
            group = "dreambot"
        }

        project.tasks.named("build").configure {
            finalizedBy(copyScripts)
        }

        project.gradle.projectsEvaluated {
            project.subprojects.forEach { sub ->
                sub.tasks.findByName("jar")?.let { jarTask ->
                    copyScripts.configure { dependsOn(jarTask) }
                }
            }
        }
    }
}
