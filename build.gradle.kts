import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.7.21"
    id("io.vertx.vertx-plugin") version "1.3.0"
    application
}

fun getVersionName(): Any {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = listOf("git","describe","--tags","--dirty")
            standardOutput = stdout
        }
        stdout.toString().trim()
    }catch (ex:Exception){
        null!!
    }
}

group = "com.greenbay.api"
version = getVersionName()

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.vertx:vertx-core:4.3.7"))
    implementation("io.vertx:vertx-web")
    implementation("io.vertx:vertx-mongo-client")
    implementation("io.vertx:vertx-mail-client")
    implementation("io.vertx:vertx-lang-kotlin-coroutines")
    implementation("com.auth0:java-jwt:4.2.2")
    implementation("org.springframework.security:spring-security-crypto:6.0.1")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testImplementation("io.vertx:vertx-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}

java{
    sourceCompatibility = JavaVersion.VERSION_1_8
}

vertx{
    mainVerticle = "com.greenbay.api.service.GreenBayService"
}

val mainVerticleName = "com.greenbay.api.service..GreenBayService"
val watchForChange = "src/**/*.kt"
val doChange = "${projectDir}/gradlew classes"

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "io.vertx.core.Launcher"
    mainClass.set("GreenBayService")
}