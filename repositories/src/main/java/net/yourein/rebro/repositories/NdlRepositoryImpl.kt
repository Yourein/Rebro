package net.yourein.rebro.repositories

import net.yourein.rebro.interfaces.NdlRepository
import net.yourein.rebro.model.ndl.SruResponse
import nl.adaptivity.xmlutil.serialization.XML

class NdlRepositoryImpl(
    private val apiService: NdlApiService,
    private val xml: XML,
) : NdlRepository {

    override suspend fun searchByIsbn(isbn: String): SruResponse? {
        val response = apiService.searchRetrieve(query = "isbn=$isbn")
        if (!response.isSuccessful) return null
        val body = response.body()?.string() ?: return null
        return xml.decodeFromString(SruResponse.serializer(), body)
    }
}
