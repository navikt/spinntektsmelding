package no.nav.helse.inntektsmelding

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import no.nav.altinnkanal.avro.ExternalAttachment
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

const val altinnMottakTopicName = "aapen-altinn-dokmot-Mottatt"
const val inntektsmeldingServiceCode = "4936"

class Spinntektsmelding(env: Environment = Environment()) {

    private val logger = LoggerFactory.getLogger(appName)

    val consumerProperties = Properties().apply {
        put(GROUP_ID_CONFIG, appName)
        put(BOOTSTRAP_SERVERS_CONFIG, env.bootstrapServersUrl)
        put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java)
        put(SPECIFIC_AVRO_READER_CONFIG, true)
        put(SCHEMA_REGISTRY_URL_CONFIG, env.schemaRegistryUrl)
        put(AUTO_OFFSET_RESET_CONFIG, "earliest")

        //TODO Trenger vi både username og truststore?
        env.username.let {
            logger.info("Using user name ${it} to authenticate against Kafka brokers ")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${env.username}\" password=\"${env.password}\";")
        }

        env.navTruststorePath?.let {
            try {
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
                put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.navTruststorePath)
                put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.navTruststorePassword)
                logger.info("Configured '${SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG}' location ")
            } catch (ex: Exception) {
                logger.error("Failed to set '${SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG}' location", ex)
            }
        }
    }

    private val consumer: Consumer<String, ExternalAttachment> = KafkaConsumer<String, ExternalAttachment>(consumerProperties)

    fun start() {
        addShutdownHook()
        consumer.subscribe(listOf(altinnMottakTopicName))

        logger.info("Consuming topic $altinnMottakTopicName ")

        try {
            while (true) {
                val consumerRecords = consumer.poll(Duration.ofSeconds(1))
                logger.info("Received ${consumerRecords.count()} records")

                consumerRecords.forEach { record ->
                    logger.info("Prosesserer kafka record")
                    val externalAttachment = record.value()
                    if (externalAttachment.getServiceCode() == inntektsmeldingServiceCode && externalAttachment.getServiceEditionCode() == "1") {
                        logger.info("Fikk " + unwrapInntektsMelding(externalAttachment))
                    }
                }
            }
        } catch (exception: WakeupException) {
            // do nothing we are shutting down
        } finally {
            consumer.close()
        }
    }

    fun stop() {
        consumer.wakeup()
    }

    private fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            this.stop()
        })
    }
}

