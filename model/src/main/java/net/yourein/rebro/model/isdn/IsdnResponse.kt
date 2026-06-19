package net.yourein.rebro.model.isdn

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

private const val NS = "https://isdn.jp/schemas/0.1"

@Serializable
@XmlSerialName("isdn", NS, "")
data class IsdnResponse(
    val item: List<IsdnItem>? = null,
)

@Serializable
@XmlSerialName("item", NS, "")
data class IsdnItem(
    @XmlSerialName("key", "", "")
    val key: String? = null,
    @XmlElement(true) @XmlSerialName("disp-isdn", NS, "")
    val dispIsdn: String? = null,
    @XmlElement(true) @XmlSerialName("region", NS, "")
    val region: String? = null,
    @XmlElement(true) @XmlSerialName("class", NS, "")
    val clazz: String? = null,
    @XmlElement(true) @XmlSerialName("type", NS, "")
    val type: String? = null,
    @XmlElement(true) @XmlSerialName("rating_gender", NS, "")
    val ratingGender: String? = null,
    @XmlElement(true) @XmlSerialName("rating_age", NS, "")
    val ratingAge: String? = null,
    @XmlElement(true) @XmlSerialName("product-name", NS, "")
    val productName: String? = null,
    @XmlElement(true) @XmlSerialName("product-yomi", NS, "")
    val productYomi: String? = null,
    @XmlElement(true) @XmlSerialName("publisher-code", NS, "")
    val publisherCode: String? = null,
    @XmlElement(true) @XmlSerialName("publisher-name", NS, "")
    val publisherName: String? = null,
    @XmlElement(true) @XmlSerialName("publisher-yomi", NS, "")
    val publisherYomi: String? = null,
    @XmlElement(true) @XmlSerialName("issue-date", NS, "")
    val issueDate: String? = null,
    @XmlElement(true) @XmlSerialName("genre-code", NS, "")
    val genreCode: String? = null,
    @XmlElement(true) @XmlSerialName("genre-name", NS, "")
    val genreName: String? = null,
    @XmlElement(true) @XmlSerialName("genre-user", NS, "")
    val genreUser: String? = null,
    @XmlElement(true) @XmlSerialName("c-code", NS, "")
    val cCode: String? = null,
    @XmlElement(true) @XmlSerialName("author", NS, "")
    val author: String? = null,
    @XmlElement(true) @XmlSerialName("shape", NS, "")
    val shape: String? = null,
    @XmlElement(true) @XmlSerialName("contents", NS, "")
    val contents: String? = null,
    @XmlElement(true) @XmlSerialName("price", NS, "")
    val price: String? = null,
    @XmlElement(true) @XmlSerialName("price-unit", NS, "")
    val priceUnit: String? = null,
    @XmlElement(true) @XmlSerialName("barcode2", NS, "")
    val barcode2: String? = null,
    @XmlElement(true) @XmlSerialName("product-comment", NS, "")
    val productComment: String? = null,
    @XmlElement(true) @XmlSerialName("product-style", NS, "")
    val productStyle: String? = null,
    @XmlElement(true) @XmlSerialName("product-size", NS, "")
    val productSize: String? = null,
    @XmlElement(true) @XmlSerialName("product-capacity", NS, "")
    val productCapacity: String? = null,
    @XmlElement(true) @XmlSerialName("product-capacity-unit", NS, "")
    val productCapacityUnit: String? = null,
    @XmlElement(true) @XmlSerialName("sample-image-uri", NS, "")
    val sampleImageUri: String? = null,
    val userOptions: List<IsdnUserOption>? = null,
    val externalLinks: List<IsdnExternalLink>? = null,
)

@Serializable
@XmlSerialName("useroption", NS, "")
data class IsdnUserOption(
    @XmlElement(true) @XmlSerialName("property", NS, "")
    val property: String? = null,
    @XmlElement(true) @XmlSerialName("value", NS, "")
    val value: String? = null,
)

@Serializable
@XmlSerialName("external-link", NS, "")
data class IsdnExternalLink(
    @XmlElement(true) @XmlSerialName("title", NS, "")
    val title: String? = null,
    @XmlElement(true) @XmlSerialName("uri", NS, "")
    val uri: String? = null,
)
