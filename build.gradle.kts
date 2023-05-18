plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(commonLibs.acf)
    compileOnly(commonLibs.craftbukit)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Restart"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = "npcs"
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//    build {
//        dependsOn(shadowJar)
//    }
}

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")