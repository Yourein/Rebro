package net.yourein.rebro.model.isdn

import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import org.junit.Test

class IsdnResponseParseTest {

    private val sampleXml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <isdn xmlns="https://isdn.jp/schemas/0.1"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="https://isdn.jp/schemas/0.1 https://isdn.jp/schemas/0.1/isdn.xsd">
        <item key="2784867874018">
        <disp-isdn>ISDN278-4-867874-01-8</disp-isdn>
        <region>日本</region>
        <class>オリジナル</class>
        <type>同人誌</type>
        <rating_gender>区別なし</rating_gender>
        <rating_age>一般</rating_age>
        <product-name>INSIDE the GATE No.1</product-name>
        <product-yomi>いんさいどざげーとなんばーわん</product-yomi>
        <publisher-code>69065772</publisher-code>
        <publisher-name>Yourein's Bookshelf</publisher-name>
        <publisher-yomi>ゆれいんずぶっくしぇるふ</publisher-yomi>
        <issue-date>2025-12-31</issue-date>
        <genre-code>611</genre-code>
        <genre-name>鉄道・旅行・メカミリ</genre-name>
        <genre-user></genre-user>
        <c-code>C0426</c-code>
        <author>一般</author>
        <shape>ムック・その他</shape>
        <contents>旅行</contents>
        <price>500</price>
        <price-unit>JPY</price-unit>
        <barcode2>2920426005008</barcode2>
        <product-comment></product-comment>
        <product-style></product-style>
        <product-size></product-size>
        <product-capacity></product-capacity>
        <product-capacity-unit></product-capacity-unit>
        <sample-image-uri>https://isdn.jp/images/thumbs/2784867874018.png</sample-image-uri>
        <useroption>
        <property>執筆</property>
        <value>Yourein</value>
        </useroption><useroption>
        <property>サブタイトル</property>
        <value>追跡!市有湯川源泉</value>
        </useroption>

        </item>
        </isdn>
    """.trimIndent()

    @Test
    fun parseIsdnResponse() {
        val xml = XML {
            recommended()
            policy = DefaultXmlSerializationPolicy(
                pedantic = false,
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER,
            )
        }
        val response = xml.decodeFromString(IsdnResponse.serializer(), sampleXml)
        println("Parsed response: $response")
        println("Items: ${response.item}")
        response.item?.firstOrNull()?.let { item ->
            println("productName: ${item.productName}")
            println("userOptions: ${item.userOptions}")
        }
    }
}
