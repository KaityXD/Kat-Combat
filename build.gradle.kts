plugins {
    kotlin("jvm") version "1.9.24"
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