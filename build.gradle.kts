
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {

    mainClass = "io.ktor.server.netty.EngineMain"


    mainClass.set("io.ktor.server.netty.EngineMain") // Correcto para Ktor

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20)) // Usa Java 21 para compilar
    }
}


dependencies {
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation("io.ktor:ktor-server-netty:3.1.1")


    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")
    implementation("io.ktor:ktor-server-core:2.0.0")  // Asegúrate de usar la última versión estable
    implementation("io.ktor:ktor-server-netty:2.0.0")
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-cio:2.0.0")

    implementation("mysql:mysql-connector-java:8.0.33") // Conector de MySQL
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")// or the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.4")  // Verifica la versión más reciente
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.mindrot:jbcrypt:0.4")


}

// Configura el archivo JAR para incluir el Main-Class
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "io.ktor.server.netty.EngineMain"
        )
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Excluir archivos duplicados

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

}
