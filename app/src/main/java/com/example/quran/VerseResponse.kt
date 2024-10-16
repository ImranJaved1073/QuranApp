package com.example.quran

import com.example.quran.models.Surah
import com.example.quran.models.Verse

data class VerseResponse(
    val ayahs: List<Verse>,
    val surah: Surah
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