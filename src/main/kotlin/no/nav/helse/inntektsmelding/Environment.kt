package no.nav.helse.inntektsmelding

data class Environment(
        val username: String = getRequiredEnvVar("SERVICEUSER_USERNAME"),
        val password: String = getRequiredEnvVar("SERVICEUSER_PASSWORD"),
        val bootstrapServersUrl: String = getRequiredEnvVar("KAFKA_BOOTSTRAP_SERVERS"),
        val schemaRegistryUrl: String = getRequiredEnvVar("KAFKA_SCHEMA_REGISTRY"),
        val httpPort: Int? = null,
        val navTruststorePath: String? = getEnvVar("NAV_TRUSTSTORE_PATH"),
        val navTruststorePassword: String? = getEnvVar("NAV_TRUSTSTORE_PASSWORD")
)

private fun getRequiredEnvVar(varName: String) = getEnvVar(varName) ?:
        throw Exception("Required env var $varName not found")

private fun getEnvVar(varName: String) =
        System.getenv(varName) ?:
        System.getProperty(varName)
