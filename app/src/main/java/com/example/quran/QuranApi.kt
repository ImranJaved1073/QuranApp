package com.example.quran

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApi {
    @GET("surah")
    suspend fun getSurahs(): SurahResponse

    @GET("surah/{surahNumber}")
    suspend fun getVerses(@Path("surahNumber") surahNumber: Int): VerseResponse

    @GET("surah/{surahNumber}/en.asad")
    suspend fun getEnglishTranslation(@Path("surahNumber") surahNumber: Int): EnglishVerseResponse

    @GET("surah/{surahNumber}/ur.ahmedali")
    suspend fun getUrduTranslation(@Path("surahNumber") surahNumber: Int): UrduVerseResponse


}
object RetrofitClient {
    private const val BASE_URL = "https://api.alquran.cloud/v1/"

    val api: QuranApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuranApi::class.java)
    }

}