package com.example.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quran.models.Para

class TasbihViewModel : ViewModel() {

    private val _parahs = MutableLiveData<List<Para>>()
    val parahs: LiveData<List<Para>> = _parahs

    // Use postValue to safely set LiveData from background threads
    fun setParahs(parahs: List<Para>) {
        _parahs.postValue(parahs)
    }

    fun getParahName(paraNumber: Int): String {
        return _parahs.value?.get(paraNumber - 1)?.englishName ?: ""
    }

    fun getParahArabicName(paraNumber: Int): String {
        return _parahs.value?.get(paraNumber - 1)?.arabicName ?: ""
    }
}
