plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

repositories {
    maven("https://jitpack.io") // YamlAssist
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.viaversion.com/")
    maven {
        name = "luck-repo"
        url = uri("https://repo.lucko.me/")
        content {
            includeModule("me.lucko", "spark-api")
        }
    }
}

val version = "1.21.4-R0.1-SNAPSHOT"

dependencies {
    implementation(projects.bukkit)
    paperweight.paperDevBundle(version)
    compileOnly("io.papermc.paper:paper-api:${version}")
}

tasks.compileJava {
    options.release.set(21)
}

// The remapper used by this dev bundle cannot read Java 25 class files, run paperweight with Java 21
paperweight.javaLauncher.set(javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(21))
})
