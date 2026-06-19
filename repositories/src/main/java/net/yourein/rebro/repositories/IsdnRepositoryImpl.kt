package net.yourein.rebro.repositories

import net.yourein.rebro.interfaces.IsdnRepository
import net.yourein.rebro.model.isdn.IsdnResponse
import nl.adaptivity.xmlutil.serialization.XML

class IsdnRepositoryImpl(
    private val apiService: IsdnApiService,
    private val xml: XML,
) : IsdnRepository {

    override suspend fun getBookInfo(isdn: String): IsdnResponse? {
        val response = apiService.getBookInfo(isdn)
        if (!response.isSuccessful) return null
        val body = response.body()?.string() ?: return null
        return xml.decodeFromString(IsdnResponse.serializer(), body)
    }
}
