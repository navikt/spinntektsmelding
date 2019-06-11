package no.nav.helse.inntektsmelding.db


import de.huxhorn.sulky.ulid.ULID
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.postgresql.util.PSQLException
import javax.sql.DataSource

class InntektsmeldingStore(private val dataSource: DataSource) {

    private val ulidGenerator = ULID()

    fun insertInntektsmelding(arbeidsgiverVirksomhetsnummer: String, brukerFnr: String, arbeidsforholdId: String,
                              inntektsmeldingXml: String, xmlVersjon: String): StoredInntektsmelding {
        try {
            val inntektsmeldingId = InntektsmeldingId(ulidGenerator.nextULID())
            using(sessionOf(dataSource)) { session ->
                session.run(
                        queryOf("INSERT INTO $INNTEKTSMELDING_TABLE " +
                                "(" +
                                "$INNTEKTSMELDING_ID, " +
                                "$ARBEIDSGIVER_VIRKSOMHETSNUMMER, " +
                                "$BRUKER_FNR, " +
                                "$ARBEIDSFORHOLD_ID, " +
                                "$INNTEKTSMELDING_XML, " +
                                "$XML_VERSJON " +
                                ") " +
                                "VALUES (?, ?, ?, ?, ?, ?)",
                                inntektsmeldingId.id,
                                arbeidsgiverVirksomhetsnummer,
                                brukerFnr,
                                arbeidsforholdId,
                                inntektsmeldingXml,
                                xmlVersjon

                        ).asUpdate
                )
            }
            return getInntektsmelding(inntektsmeldingId)
        } catch (p: PSQLException) {
            throw StoreException(p)
        }
    }

    fun getInntektsmelding(inntektsmeldingId: InntektsmeldingId): StoredInntektsmelding {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                    queryOf("SELECT $INNTEKTSMELDING_ID, " +
                            "$ARBEIDSGIVER_VIRKSOMHETSNUMMER, " +
                            "$BRUKER_FNR, " +
                            "$ARBEIDSFORHOLD_ID, " +
                            "$INNTEKTSMELDING_XML, " +
                            "$XML_VERSJON " +
                            "FROM $INNTEKTSMELDING_TABLE " +
                            "WHERE $INNTEKTSMELDING_ID = ?", inntektsmeldingId.id)
                            .map { row ->
                                StoredInntektsmelding(
                                        InntektsmeldingId(row.string(INNTEKTSMELDING_ID)),
                                        row.string(ARBEIDSGIVER_VIRKSOMHETSNUMMER),
                                        row.string(BRUKER_FNR),
                                        row.string(ARBEIDSFORHOLD_ID),
                                        row.string(INNTEKTSMELDING_XML),
                                        row.string(XML_VERSJON)
                                )
                            }
                            .asSingle
            )
                    ?: throw InntektsmeldingNotFoundException("Inntektsmelding with id $inntektsmeldingId not found.")
        }
    }
}
