package no.nav.helse.inntektsmelding.db

import com.zaxxer.hikari.HikariDataSource
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


// Starter postgres i en container, slik at vi har en database å teste mot.
//
// Dette forutsetter at Docker-daemon kjører på maskinen. Ask me how I know...
// Løsning: https://stackoverflow.com/questions/44084846/cannot-connect-to-the-docker-daemon-on-macos
private object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:11.2").apply {
            start()
        }
    }
}

private object DataSource {
    val instance: HikariDataSource by lazy {
        HikariDataSource().apply {
            username = PostgresContainer.instance.username
            password = PostgresContainer.instance.password
            jdbcUrl = PostgresContainer.instance.jdbcUrl
            connectionTimeout = 1000L
        }
    }
}
internal class PostgresTest {

    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = migrate(DataSource.instance)
            assertEquals(1, migrations, "Wrong number of migrations")
        }
    }

    @Test
    fun `Migration scripts run once`() {
        withCleanDb {
            migrate(DataSource.instance)

            val migrations = migrate(DataSource.instance)
            assertEquals(0, migrations, "Wrong number of migrations")
        }
    }

}

internal class PostgresInntektStoreTest {
    @Test
    fun `Successful insert and read of inntektsmelding`() {
        withMigratedDb {
            with(InntektsmeldingStore(DataSource.instance)) {

                val arbeidsgiverVirksomhetsnummer = "1"
                val brukerFnr = "2"
                val arbeidsforholdId = "3"
                val inntektsmeldingXml = "4"
                val xmlVersjon = "5"

                val storedInntektsMelding = insertInntektsmelding(arbeidsgiverVirksomhetsnummer, brukerFnr, arbeidsforholdId, inntektsmeldingXml, xmlVersjon)

                assertNotNull(storedInntektsMelding.inntektsmeldingId)
                assertEquals(arbeidsgiverVirksomhetsnummer, storedInntektsMelding.arbeidsgiverVirksomhetsnummer)
                assertEquals(brukerFnr, storedInntektsMelding.brukerFnr)
                assertEquals(arbeidsforholdId, storedInntektsMelding.arbeidsforholdId)
                assertEquals(inntektsmeldingXml, storedInntektsMelding.inntektsmeldingXml)
                assertEquals(xmlVersjon, storedInntektsMelding.xmlVersjon)

                val storedInntektsMeldingById = getInntektsmelding(storedInntektsMelding.inntektsmeldingId)
                assertEquals(storedInntektsMelding, storedInntektsMeldingById)
            }
        }
    }
}

private fun withCleanDb(test: () -> Unit) = DataSource.instance.also { clean(it) }.run { test() }

private fun withMigratedDb(test: () -> Unit) = DataSource.instance.also { clean(it) }.also { migrate(it) }.run { test() }



