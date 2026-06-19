package net.yourein.rebro.model.ndl

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

private const val SRW_NS = "http://www.loc.gov/zing/srw/"
private const val DC_NS = "http://purl.org/dc/elements/1.1/"
private const val SRW_DC_NS = "info:srw/schema/1/dc-v1.1"

@Serializable
@XmlSerialName("searchRetrieveResponse", SRW_NS, "")
data class SruResponse(
    @XmlElement(true) @XmlSerialName("version", SRW_NS, "")
    val version: String? = null,
    @XmlElement(true) @XmlSerialName("numberOfRecords", SRW_NS, "")
    val numberOfRecords: String? = null,
    val records: SruRecords? = null,
)

@Serializable
@XmlSerialName("records", SRW_NS, "")
data class SruRecords(
    val record: List<SruRecord>? = null,
)

@Serializable
@XmlSerialName("record", SRW_NS, "")
data class SruRecord(
    @XmlElement(true) @XmlSerialName("recordSchema", SRW_NS, "")
    val recordSchema: String? = null,
    @XmlElement(true) @XmlSerialName("recordPacking", SRW_NS, "")
    val recordPacking: String? = null,
    val recordData: SruRecordData? = null,
    @XmlElement(true) @XmlSerialName("recordPosition", SRW_NS, "")
    val recordPosition: String? = null,
)

@Serializable
@XmlSerialName("recordData", SRW_NS, "")
data class SruRecordData(
    val dc: DcRecord? = null,
)

@Serializable
@XmlSerialName("dc", SRW_DC_NS, "srw_dc")
data class DcRecord(
    @XmlElement(true) @XmlSerialName("title", DC_NS, "dc")
    val title: String? = null,
    @XmlElement(true) @XmlSerialName("creator", DC_NS, "dc")
    val creator: String? = null,
    @XmlElement(true) @XmlSerialName("description", DC_NS, "dc")
    val description: String? = null,
    @XmlElement(true) @XmlSerialName("publisher", DC_NS, "dc")
    val publisher: String? = null,
    @XmlElement(true) @XmlSerialName("language", DC_NS, "dc")
    val language: String? = null,
)
