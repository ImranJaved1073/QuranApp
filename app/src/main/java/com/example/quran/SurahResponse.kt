package com.example.quran

data class SurahResponse(
    val code: Int,
    val status: String,
    val data: List<Surah>
)