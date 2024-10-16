package com.example.quran.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quran.models.Surah

class SurahListViewModel : ViewModel() {

    private val _surahs = MutableLiveData<List<Surah>>()
    val surahs: LiveData<List<Surah>> = _surahs

    // Use postValue to safely set LiveData from background threads
    fun setSurahs(surahs: List<Surah>) {
        _surahs.postValue(surahs)
    }

    fun getSurahName(surahNumber: Int): String {
        return _surahs.value?.get(surahNumber - 1)?.englishNameTranslation ?: ""
    }

    fun getSurahArabicName(surahNumber: Int): String {
        return _surahs.value?.get(surahNumber - 1)?.name ?: ""
    }

    fun getSurahRevelationType(surahNumber: Int): String {
        return _surahs.value?.get(surahNumber - 1)?.revelationType ?: ""
    }
}
