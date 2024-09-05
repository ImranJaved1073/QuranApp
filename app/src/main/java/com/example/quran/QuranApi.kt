package com.example.quran

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApi {
    @GET("Surah")
    suspend fun getSurahs(): List<Surah>

    @GET("Para")
    suspend fun getParahs(): List<Para>

    @GET("Surah/{suraID}")
    suspend fun getVerses(@Path("suraID") surahNumber: Int): VerseResponse

    @GET("Para/{paraID}")
    suspend fun getParaVerses(@Path("paraID") paraNumber: Int): ParaResponse

    @GET("Ayat/{ayaID}")
    suspend fun getAyat(@Path("ayaID") ayaNumber: Int): Verse



}



interface UmmAlQuraApi {
    @GET("date")
    suspend fun getIslamicDate(@Query("date") date: String): IslamicDateResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://quranapiservice-ekfkd2dvcsbmekcq.eastus-01.azurewebsites.net/api/"

    val api: QuranApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuranApi::class.java)
    }

}

object UmmAlQuraRetrofitClient {
    private const val BASE_URL = "https://api.aladhan.com/v1/"

    val api: UmmAlQuraApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UmmAlQuraApi::class.java)
    }
}
