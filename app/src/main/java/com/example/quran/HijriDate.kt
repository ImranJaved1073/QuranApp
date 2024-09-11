package com.example.quran


data class HijriDateResponse(
    val code: Int,
    val status: String,
    val data: Data
)

data class Data(
    val hijri: HijriDate,
    val gregorian: Gregorian
)

data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: Weekday,
    val month: Month,
    val year: String,
    val designation: Designation,
    val holidays: List<Any>
)

data class Gregorian(
    val date: String,
    val format: String,
    val day: String,
    val weekday: Weekday,
    val month: Month,
    val year: String,
    val designation: Designation
)

data class Weekday(
    val en: String,
    val ar: String? = null
)

data class Month(
    val number: Int,
    val en: String,
    val ar: String? = null
)

data class Designation(
    val abbreviated: String,
    val expanded: String
)
