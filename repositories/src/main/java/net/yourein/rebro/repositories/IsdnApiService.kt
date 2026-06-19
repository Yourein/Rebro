package net.yourein.rebro.repositories

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface IsdnApiService {
    @GET("xml/{isdn}")
    suspend fun getBookInfo(@Path("isdn") isdn: String): Response<ResponseBody>
}
