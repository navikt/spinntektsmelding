CREATE TABLE IF NOT EXISTS inntektsmelding_sykepenger
(
  id                                TEXT                     NOT NULL,
  arbeidsgiver_virksomhetsnummer    TEXT                     NOT NULL,
  bruker_fnr                        TEXT                     NOT NULL,
  arbeidsforhold_id                 TEXT                     NOT NULL,
  inntektsmelding_xml               TEXT                     NOT NULL,
  xml_versjon                       TEXT                     NOT NULL,
  created TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
  PRIMARY KEY (id)
);
