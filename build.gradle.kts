plugins {
    kotlin("jvm") version "1.9.24"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.24"
}

group = "com.example"
version = "0.0.1"

application {
    // EngineMain + application.conf/application.yaml
    mainClass.set("io.ktor.server.netty.EngineMain")
    val isDevelopment: Boolean = project.hasProperty("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

dependencies {
    // ---- Ktor 2.3.12 alineado por BOM ----
    implementation(platform("io.ktor:ktor-bom:2.3.12"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-config-yaml") // si usás application.yaml

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.12")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")



    // DB
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")

    // Exposed (mantengo versión que ya usabas)
    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.43.0")



    // Utils
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("at.favre.lib:bcrypt:0.10.2")

    testImplementation(kotlin("test"))
}

tasks {
    shadowJar {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }
}
