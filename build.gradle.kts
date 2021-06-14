import io.papermc.paperweight.util.cache
import io.papermc.paperweight.util.Git

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("io.papermc.paperweight.patcher") version "1.0.0-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://wav.jfrog.io/artifactory/repo/") {
        content {
            onlyForConfigurations("paperclip")
        }
    }
    maven("https://maven.quiltmc.org/repository/release/") {
        content {
            onlyForConfigurations("remapper")
        }
    }
}

dependencies {
    remapper("org.quiltmc:tiny-remapper:0.4.1")
    paperclip("io.papermc:paperclip:2.0.0-SNAPSHOT@jar")
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(16))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(16)
    }

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://ci.emc.gs/nexus/content/groups/aikar/")
        maven("https://repo.aikar.co/content/groups/aikar")
        maven("https://repo.md-5.net/content/repositories/releases/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

val initMojangApi by tasks.registering {
    val paperMojangApi = project.layout.cache.resolve("paperweight/upstreams/paper/Paper-MojangAPI").toFile()
    doLast {
        Git(paperMojangApi)("init").executeOut()
        Git(paperMojangApi)("add", ".").executeOut()
        Git(paperMojangApi)("commit", "-m", "Initial Source", "--author=Initial <auto@mated.null>").executeOut()
    }
}

paperweight {
    serverProject.set(project(":Purpur-Server"))

    usePaperUpstream(providers.gradleProperty("paperCommit")) {
        withPaperPatcher {
            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("Purpur-API"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("Purpur-Server"))
        }
    }

    upstreams {
        register("Paper") {
            upstreamDataTask.get().finalizedBy(initMojangApi)

            patchTasks.register("mojangApi") {
                sourceDir.set(project.layout.cache.resolve("paperweight/upstreams/paper/Paper-MojangAPI").toFile())
                patchDir.set(file("patches/mojangapi"))
                outputDir.set(file("Purpur-MojangAPI"))
            }
        }
    }
}
