plugins {
    kotlin("jvm") version "2.0.10"
    id("com.gradleup.shadow") version "9.4.1"
}

group = "space.kaity"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("dev.folia:folia-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.kyori:adventure-api:4.21.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.21.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.12-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Explicit versions to resolve conflicts between WorldGuard strict constraints and Folia requirements
    compileOnly("com.google.guava:guava:33.3.1-jre")
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.15")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveFileName.set("KatCombat-${version}.jar")
    relocate("kotlin", "com.kaity.katcombat.libs.kotlin")
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}