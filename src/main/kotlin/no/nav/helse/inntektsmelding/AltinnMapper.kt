package no.nav.helse.inntektsmelding

import no.nav.altinnkanal.avro.ExternalAttachment
import no.nav.model.databatch.DataBatch
import java.io.StringReader
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

val dataBatchJaxBContext: JAXBContext = JAXBContext.newInstance(DataBatch::class.java)
val dataBatchUnmarshaller: Unmarshaller = dataBatchJaxBContext.createUnmarshaller()

fun unwrapInntektsMelding(externalAttachment: ExternalAttachment): String? {
    val dataBatch = dataBatchUnmarshaller.unmarshal(StringReader(externalAttachment.getBatch())) as DataBatch
    return dataBatch.dataUnits.dataUnit.first().formTask.form.first().formData
}