import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.JavaExec

plugins {
    kotlin("jvm") version "2.0.0"
    java
    id("com.gradleup.shadow") version "8.3.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    testImplementation(kotlin("test"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("all")

    manifest {
        attributes["Main-Class"] = "org.example.MainKt"
    }

    // Tạm thời KHÔNG merge service files để tránh lỗi META-INF
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude("META-INF/INDEX.LIST")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("META-INF/LICENSE*", "META-INF/NOTICE*")
    exclude("META-INF/versions/**/module-info.class")
    exclude("module-info.class")
}

tasks.register<JavaExec>("runApp") {
    group = "application"
    description = "Run the desktop app"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.MainKt")
    environment("DISPLAY", ":1")
    environment("XAUTHORITY", "/home/nghianv/.Xauthority")
    jvmArgs("-Djava.awt.headless=false")
}

tasks.test {
    useJUnitPlatform()
}