import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

group = "io.wollinger"
version = properties["project.version"] as String
val archiveName = "${project.name}-$version.jar"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:${properties["jda.version"]}")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.json:json:20220924")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.create("run", JavaExec::class) {
    group = "application"
    dependsOn(tasks.build)
    doFirst {
        val runDir = File(project.buildDir, "run").also { it.mkdirs() }
        workingDir = runDir
        File(project.buildDir, "libs").copyRecursively(runDir, true)
        classpath(File(runDir, archiveName).absolutePath)
        standardInput = System.`in` //This allows input in our IDE
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    archiveFileName.set(archiveName)
    manifest { attributes["Main-Class"] = "io.wollinger.rataudc.MainKt" }
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}