package no.nav.helse.inntektsmelding

import org.slf4j.LoggerFactory

const val appName = "spinntektsmelding"
private val log = LoggerFactory.getLogger(appName)

fun main() {
    log.info("Starting $appName")



    Spinntektsmelding().start()
}

