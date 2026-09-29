dependencies {
    implementation(projects.bukkit)
    compileOnly("org.spigotmc:spigot:1.21.1-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.release.set(21) // Minecraft 1.21+ requires Java 21
}
