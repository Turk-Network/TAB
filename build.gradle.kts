plugins {
    id("tab.parent")
}

allprojects {
    group = "me.neznamy"
    version = "6.2.1-SNAPSHOT"
    description = "An all-in-one solution that works"

    ext.set("id", "tab")
    ext.set("website", "https://github.com/NEZNAMY/TAB")
    ext.set("author", "NEZNAMY")
    ext.set("credits", "Joseph T. McQuigg (JT122406)")
}

val platformPaths = setOf(
    ":bukkit",
    ":bukkit:paper_1_21",
    ":bukkit:paper_1_21_2",
    ":bukkit:paper_1_21_4",
    ":bukkit:paper_1_21_9",
    ":bukkit:paper_1_21_11",
    ":bukkit:paper_26_2",
    ":bukkit:v1_21_R1",
    ":bukkit:v1_21_R2",
    ":bukkit:v1_21_R3",
    ":bukkit:v1_21_R4",
    ":bukkit:v1_21_R5",
    ":bukkit:v1_21_R6",
    ":bukkit:v1_21_R7",
    ":bukkit:v26_1",
    ":bukkit:v26_2"
)

/**
 * Paper 1.21.x modules. They target Java 21, and the remappers used by their dev bundles cannot read
 * Java 25 class files, so paperweight must run on Java 21 as well instead of the Java 25 project toolchain.
 */
val paper121JavaVersion = 21
val paper121Paths = setOf(
    ":bukkit:paper_1_21",
    ":bukkit:paper_1_21_2",
    ":bukkit:paper_1_21_4",
    ":bukkit:paper_1_21_9",
    ":bukkit:paper_1_21_11"
)

val specialPaths = setOf(
    ":api",
    ":shared"
)

subprojects {
    when (path) {
        in platformPaths -> plugins.apply("tab.platform-conventions")
        in specialPaths -> plugins.apply("tab.standard-conventions")
        else -> plugins.apply("tab.base-conventions")
    }
    if (path in paper121Paths) {
        plugins.withId("io.papermc.paperweight.userdev") {
            tasks.named<JavaCompile>("compileJava") {
                options.release.set(paper121JavaVersion)
            }
            @Suppress("UNCHECKED_CAST")
            val paperweightLauncher = extensions.getByName("paperweight")
                .withGroovyBuilder { getProperty("javaLauncher") } as Property<JavaLauncher>
            paperweightLauncher.set(extensions.getByType<JavaToolchainService>().launcherFor {
                languageVersion.set(JavaLanguageVersion.of(paper121JavaVersion))
            })
        }
    }
}
