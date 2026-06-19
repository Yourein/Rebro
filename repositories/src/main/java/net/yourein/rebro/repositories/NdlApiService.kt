package net.yourein.rebro.repositories

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NdlApiService {
    @GET("api/sru")
    suspend fun searchRetrieve(
        @Query("operation") operation: String = "searchRetrieve",
        @Query("recordSchema") recordSchema: String = "dc",
        @Query("recordPacking") recordPacking: String = "xml",
        @Query("maximumRecords") maximumRecords: Int = 1,
        @Query("query") query: String,
    ): Response<ResponseBody>
}
