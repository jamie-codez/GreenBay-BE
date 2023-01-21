import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("io.vertx.vertx-plugin") version "1.3.0"
    application
}

group = "com.greenbay.api"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:4.3.7")
    implementation("io.vertx:vertx-web:4.3.7")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}

java{
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application{
    mainClassName = "io.vertx.core.Launcher"
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
    mainClass.set("MainKt")
}