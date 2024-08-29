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

//    @GET("surah/{surahNumber}/en.asad")
//    suspend fun getEnglishTranslation(@Path("surahNumber") surahNumber: Int): EnglishVerseResponse
//
//    @GET("surah/{surahNumber}/ur.ahmedali")
//    suspend fun getUrduTranslation(@Path("surahNumber") surahNumber: Int): UrduVerseResponse


}
object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.11:5207/api/"

    val api: QuranApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuranApi::class.java)
    }

}