package com.example.quran

data class Verse(
    val ayaID: Int?, // Nullable Int for AyaID
    val suraID: Int?, // Nullable Int for SuraID
    val ayaNo: Int?, // Nullable Int for AyaNo
    val arabicText: String, // Non-nullable String for ArabicText
    val fatehMuhammadJalandhrield: String, // Non-nullable String for FatehMuhammadJalandhrield
    val mehmoodulHassan: String, // Non-nullable String for MehmoodulHassan
    val drMohsinKhan: String, // Non-nullable String for DrMohsinKhan
    val muftiTaqiUsmani: String, // Non-nullable String for MuftiTaqiUsmani
    val rakuID: Int?, // Nullable Int for RakuID
    val pRakuID: Int?, // Nullable Int for PRakuID
    val paraID: Int?, // Nullable Int for ParaID
    val pAyatID: Int? // Nullable Int for PAyaID
)