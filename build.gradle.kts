import org.gradle.api.file.DuplicatesStrategy
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    mergeServiceFiles()

    manifest {
        // PHẢI có tên package ở phía trước
        attributes["Main-Class"] = "org.example.MainKt"
    }

    // Sửa lỗi "Could not add META-INF" bằng cách bỏ qua các file trùng từ thư viện
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Loại bỏ các file chữ ký số gây lỗi ZIP (SF, DSA, RSA)
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("META-INF/LICENSE*", "META-INF/NOTICE*")
}

tasks.test {
    useJUnitPlatform()
}