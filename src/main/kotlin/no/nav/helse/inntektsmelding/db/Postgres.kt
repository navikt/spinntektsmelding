package no.nav.helse.inntektsmelding.db

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

fun migrate(dataSource: HikariDataSource, initSql: String = ""): Int =
        Flyway.configure().dataSource(dataSource).initSql(initSql).load().migrate()

fun clean(dataSource: HikariDataSource) = Flyway.configure().dataSource(dataSource).load().clean()
