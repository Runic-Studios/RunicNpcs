val artifactName = "npcs"
val rrGroup: String by rootProject.extra
val rrVersion: String by rootProject.extra

plugins {
    `java-library`
    `maven-publish`
}

group = rrGroup
version = rrVersion

dependencies {
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.craftbukkit)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(project(":Projects:Restart"))
    compileOnly(project(":Projects:Common"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rrGroup
            artifactId = artifactName
            version = rrVersion
            from(components["java"])
        }
    }
}