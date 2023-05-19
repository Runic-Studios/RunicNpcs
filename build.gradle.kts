plugins {
    `java-library`
    `maven-publish`
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.craftbukkit)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.holographicdisplays)
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

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")