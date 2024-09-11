package com.example.quran

import com.example.quran.models.Para
import com.example.quran.models.Surah
import com.example.quran.models.Verse
import com.example.quran.responses.ParaResponse
import retrofit2.Call
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

    @GET("{suraID}{ayaID}.mp3")
    suspend fun getAyatAudio(
        @Path("suraID") suraID: String,
        @Path("ayaID") ayaID: String
    ): String

}

interface AladhanApi {
    @GET("v1/gToH")
    fun getHijriDate(@Query("date") date: String): Call<HijriDateResponse>
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

object RetrofitInstance {
    private const val BASE_URL = "https://api.aladhan.com/"

    val api: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AladhanApi::class.java)
    }
}