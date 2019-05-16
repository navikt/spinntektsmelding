package no.nav.helse.inntektsmelding

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.prometheus.client.CollectorRegistry
import no.nav.altinnkanal.avro.ExternalAttachment
import no.nav.common.JAASCredential
import no.nav.common.KafkaEnvironment
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class ComponentTest {

    companion object {
        private const val username = "srvkafkaclient"
        private const val password = "kafkaclient"

        val embeddedEnvironment = KafkaEnvironment(
                users = listOf(JAASCredential(username, password)),
                autoStart = false,
                withSchemaRegistry = true,
                withSecurity = true,
                topicNames = listOf(altinnMottakTopicName)
        )

        private val env = Environment(
                username = username,
                password = password,
                bootstrapServersUrl = embeddedEnvironment.brokersURL,
                schemaRegistryUrl = embeddedEnvironment.schemaRegistry!!.url
        )

        @BeforeAll
        @JvmStatic
        fun setup() {
            CollectorRegistry.defaultRegistry.clear()
            embeddedEnvironment.start()
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            embeddedEnvironment.tearDown()
        }
    }

    private lateinit var spinntektsmelding: Spinntektsmelding

    @BeforeEach
    fun `start appen`() {
        spinntektsmelding = Spinntektsmelding(env)
        Thread {
            spinntektsmelding.start()
        }.start()
    }

    @AfterEach
    fun `clear prometheus registry`() {
        spinntektsmelding.stop()
        CollectorRegistry.defaultRegistry.clear()
    }

    @Test
    fun `inntektsmelding is received`() {
        assertEquals(embeddedEnvironment.serverPark.status, KafkaEnvironment.ServerParkStatus.Started)

        val record = ExternalAttachment(getFileAsString("src/test/resources/inntektsskjema.xml"), "4936", "1", "anArchiveReference", "aCallId")
        produceOneMessage(record)

        Thread.sleep(5000L)
    }

    private fun producerProperties(): Properties {
        return Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedEnvironment.brokersURL)
            put("schema.registry.url", embeddedEnvironment.schemaRegistry?.url.orEmpty())
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";")
        }
    }

    private fun produceOneMessage(message: ExternalAttachment) {
        val producer = KafkaProducer<String, ExternalAttachment>(producerProperties())
        val recordMetadata = producer.send(ProducerRecord(altinnMottakTopicName, System.currentTimeMillis().toString(), message)).get()
    }

    fun getFileAsString(filePath: String) = String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
}

