package com.example.quran

data class VerseResponse(
    val data: SurahData
)

data class UrduVerseResponse(
    val data: SurahUrdu
)

data class EnglishVerseResponse(
    val data: SurahEnglish
)

data class SurahData(
    val ayahs: List<Verse>
)


data class SurahEnglish(
    val ayahs: List<Verse>
)

data class SurahUrdu(
    val ayahs: List<Verse>
)