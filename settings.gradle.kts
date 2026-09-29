enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral() // Netty, SnakeYaml, json-simple, Guava, Kyori event, bStats, AuthLib, LuckPerms
        maven("https://repo.viaversion.com/") // ViaVersion
        maven("https://repo.codemc.org/repository/nms/") // CraftBukkit + NMS
        maven("https://repo.papermc.io/repository/maven-public/") // paperweight, Paper API, Adventure
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
        maven("https://repo.opencollab.dev/maven-snapshots/") // Floodgate
        maven("https://jitpack.io") // PremiumVanish, Vault, YamlAssist
        maven("https://mvn.lib.co.nz/public") // LibsDisguises
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "TAB"

include(":api")
include(":shared")
include(":bukkit")
include(":bukkit:paper_1_21")
include(":bukkit:paper_1_21_2")
include(":bukkit:paper_1_21_4")
include(":bukkit:paper_1_21_9")
include(":bukkit:paper_1_21_11")
include(":bukkit:paper_26_2")
include(":bukkit:v1_21_R1")
include(":bukkit:v1_21_R2")
include(":bukkit:v1_21_R3")
include(":bukkit:v1_21_R4")
include(":bukkit:v1_21_R5")
include(":bukkit:v1_21_R6")
include(":bukkit:v1_21_R7")
include(":bukkit:v26_1")
include(":bukkit:v26_2")
include(":jar")
