plugins {
    kotlin("jvm") version "2.3.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(18)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    // Đóng gói tất cả thư viện vào 1 file JAR duy nhất
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}