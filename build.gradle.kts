import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.11"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

buildscript {
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

application {
    mainClassName = "no.nav.helse.AppKt"
}

val prometheusVersion = "0.5.0"
val flywayVersion = "6.0.0-beta"
val hikariVersion = "3.3.1"
val postgresVersion = "42.2.5"
val kotliqueryVersion = "1.3.0"
val junitJupiterVersion = "5.3.1"
val testcontainers_version = "1.10.6"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.30.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:5.2")

    // Metrikker
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation( "io.prometheus:simpleclient_common:$prometheusVersion")

    // XML
    implementation("javax.xml.bind:jaxb-api:2.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.0.1")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:2.0.1")
    implementation("io.confluent:kafka-avro-serializer:5.0.0")
    implementation("no.nav.altinnkanal:altinnkanal-schemas:1.0.1")

    // HTTP-klient
    implementation("com.github.kittinunf.fuel:fuel:1.15.1")

    // Database
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.2.0")
    implementation("com.github.seratch:kotliquery:$kotliqueryVersion")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation ("no.nav:kafka-embedded-env:2.1.1")
    testImplementation("org.testcontainers:postgresql:$testcontainers_version")
}

repositories {
    jcenter()
    mavenCentral()
    maven("http://packages.confluent.io/maven/")
    maven("https://dl.bintray.com/kotlin/ktor")
    maven("https://repo.adeo.no/repository/maven-releases/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.named<KotlinCompile>("compileKotlin") {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar> {
    classifier = ""
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "5.0"
}
