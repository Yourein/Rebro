package net.yourein.rebro.model.ndl

import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SruResponseParseTest {

    private val sampleXml = """
        <searchRetrieveResponse xmlns="http://www.loc.gov/zing/srw/">
          <version>1.2</version>
          <numberOfRecords>1</numberOfRecords>
          <extraResponseData>
            <facets>
              <lst name="REPOSITORY_NO">
                <int name="R100000002">1</int>
              </lst>
            </facets>
          </extraResponseData>
          <records>
            <record>
              <recordSchema>info:srw/schema/1/dc-v1.1</recordSchema>
              <recordPacking>xml</recordPacking>
              <recordData>
                <srw_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:srw_dc="info:srw/schema/1/dc-v1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="info:srw/schema/1/dc-v1.1 http://www.loc.gov/standards/sru/dc-schema.xsd">
                  <dc:title>アラビアの夜の種族</dc:title>
                  <dc:creator>古川日出男 [著]</dc:creator>
                  <dc:description>91刷改版</dc:description>
                  <dc:publisher>角川書店</dc:publisher>
                  <dc:language>jpn</dc:language>
                </srw_dc:dc>
              </recordData>
              <recordPosition>1</recordPosition>
            </record>
          </records>
        </searchRetrieveResponse>
    """.trimIndent()

    @Test
    fun parseSruResponse() {
        val xml = XML {
            recommended()
            policy = DefaultXmlSerializationPolicy.Builder().apply {
                pedantic = false
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }.build()
        }
        val response = xml.decodeFromString(SruResponse.serializer(), sampleXml)

        assertEquals("1.2", response.version)
        assertEquals("1", response.numberOfRecords)

        val record = response.records?.record?.firstOrNull()
        assertNotNull(record)

        val dc = record!!.recordData?.dc
        assertNotNull(dc)
        assertEquals("アラビアの夜の種族", dc!!.title)
        assertEquals("古川日出男 [著]", dc.creator)
        assertEquals("91刷改版", dc.description)
        assertEquals("角川書店", dc.publisher)
        assertEquals("jpn", dc.language)

        println("title: ${dc.title}")
        println("creator: ${dc.creator}")
        println("description: ${dc.description}")
        println("publisher: ${dc.publisher}")
        println("language: ${dc.language}")
    }
}
