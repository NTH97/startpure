plugins {
    `java-library`
}

apply<BootstrapPlugin>()

subprojects {
    apply<JavaPlugin>()
    apply(plugin = "java-library")

    group = "org.dreambot.scripts"

    project.extra["PluginProvider"] = "DreamBot"
    project.extra["PluginLicense"] = "All Rights Reserved"

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(files("C:/Users/nthju/DreamBot/BotData/repository2/dreambot-client.jar"))

        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }
    }
}
