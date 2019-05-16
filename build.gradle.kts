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

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.30.2")

    // Logging
    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("net.logstash.logback:logstash-logback-encoder:5.2")

    // Metrikker
    val prometheusVersion = "0.5.0"
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    compile( "io.prometheus:simpleclient_common:$prometheusVersion")

    // XML
    compile("javax.xml.bind:jaxb-api:2.1")
    compile("org.glassfish.jaxb:jaxb-runtime:2.3.0.1")

    // Kafka
    compile("org.apache.kafka:kafka-clients:2.0.1")
    compile("io.confluent:kafka-avro-serializer:5.0.0")
    compile("no.nav.altinnkanal:altinnkanal-schemas:1.0.1")

    // HTTP-klient
    compile("com.github.kittinunf.fuel:fuel:1.15.1")

    // Test
    val junitJupiterVersion = "5.3.1"
    testCompile("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testCompile ("no.nav:kafka-embedded-env:2.1.1")
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
