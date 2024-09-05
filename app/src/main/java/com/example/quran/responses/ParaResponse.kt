package com.example.quran.responses

import com.example.quran.models.Para
import com.example.quran.models.Verse

data class ParaResponse(
    val ayahs: List<Verse>,
    val para: Para
)