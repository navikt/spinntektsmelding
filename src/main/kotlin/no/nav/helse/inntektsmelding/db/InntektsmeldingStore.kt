package no.nav.helse.inntektsmelding.db

import de.huxhorn.sulky.ulid.ULID

const val INNTEKTSMELDING_TABLE = "inntektsmelding_sykepenger"
const val INNTEKTSMELDING_ID = "id"
const val ARBEIDSGIVER_VIRKSOMHETSNUMMER = "arbeidsgiver_virksomhetsnummer"
const val BRUKER_FNR = "bruker_fnr"
const val ARBEIDSFORHOLD_ID = "arbeidsforhold_id"
const val INNTEKTSMELDING_XML = "inntektsmelding_xml"
const val XML_VERSJON = "xml_versjon"

/** Represents a row in table inntektsmelding_sykepenger */
data class StoredInntektsmelding(
        val inntektsmeldingId: InntektsmeldingId,
        val arbeidsgiverVirksomhetsnummer: String,
        val brukerFnr: String,
        val arbeidsforholdId: String,
        val inntektsmeldingXml: String,
        val xmlVersjon: String
)

data class InntektsmeldingId(val id: String) {
    init {
        try {
            ULID.parseULID(id)
        } catch (e: IllegalArgumentException) {
            throw IllegalInntektsmeldingIdException("ID $id is not a valid ULID", e)
        }
    }
}

class InntektsmeldingNotFoundException(override val message: String) : RuntimeException(message)

class StoreException(override val cause: Throwable) : RuntimeException(cause)

class IllegalInntektsmeldingIdException(override val message: String, override val cause: Throwable) : RuntimeException(message, cause)
