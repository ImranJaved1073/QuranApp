package com.example.quran

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.adapters.VerseAdapter
import com.example.quran.models.Verse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ParaVerseFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var verseAdapter: VerseAdapter
    private var ayatsList: List<Verse> = emptyList() // Initialize as an empty list

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_para_verse, container, false)

        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isArabicEnabled = preferences.getBoolean("arabic_enabled", true)
        val isTranslationEnabled = preferences.getBoolean("translation_enabled", true)
        val arabicFontSize = preferences.getInt("arabic_font_size", 22)
        val translationFontSize = preferences.getInt("translation_font_size", 22)
        val isEnglishTranslationEnabled = preferences.getBoolean("english_translation_enabled", true)
        val englishTranslationFontSize = preferences.getInt("english_translation_font_size", 18)

        recyclerView = view.findViewById(R.id.ayatParaRV)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val paraNumber = arguments?.getInt("PARA_NUMBER", 1) ?: 1
        val paraName = arguments?.getString("PARA_NAME")
        val paraArabicName = arguments?.getString("PARA_ARABIC_NAME")
        val ayahNumberToScroll = arguments?.getInt("AYAH_NUMBER", -1) ?: -1

        // Update the UI with Surah details
        view.findViewById<TextView>(R.id.tvParaName).text = paraName
        view.findViewById<TextView>(R.id.tvParaArabicName).text = paraArabicName

        // Fetch Ayats and setup SearchView
        setupSearchView(view)
        fetchAyats(paraNumber, ayahNumberToScroll,isArabicEnabled, isTranslationEnabled, arabicFontSize, translationFontSize, isEnglishTranslationEnabled,englishTranslationFontSize)

        return view
    }

    private fun fetchAyats(paraNumber: Int, ayahNumberToScroll: Int,isArabicEnabled: Boolean,
                           isTranslationEnabled: Boolean,
                           arabicFontSize: Int,
                           translationFontSize: Int, isEnglishTranslationEnabled: Boolean, englishTranslationFontSize: Int) {
        GlobalScope.launch {
            try {
                // Fetch Ayats using Retrofit with coroutines
                val fetchedAyats = RetrofitClient.api.getParaVerses(paraNumber)
                val para = fetchedAyats.para
                val ayahs = fetchedAyats.ayahs

                // Update the UI on the main thread
                requireActivity().runOnUiThread {
                    ayatsList = ayahs // Update ayatsList with the fetched data
                    verseAdapter = VerseAdapter(ayatsList,isArabicEnabled,
                        isTranslationEnabled,
                        arabicFontSize,
                        translationFontSize, isEnglishTranslationEnabled, englishTranslationFontSize)
                    recyclerView.adapter = verseAdapter

                    // Scroll to the specified Ayah if provided
                    if (ayahNumberToScroll != -1) {
                        recyclerView.scrollToPosition(
                            ayatsList.indexOfFirst { it.ayaNo == ayahNumberToScroll }
                        )
                    }
                }
            } catch (e: IOException) {
                // Handle network errors
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VerseFragment", "Network Error: ${e.message}")
                }
            } catch (e: HttpException) {
                // Handle API errors
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VerseFragment", "API Error: ${e.message}")
                }
            }
        }
    }

    private fun setupSearchView(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.paraAyatSearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchAyat(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchAyat(it)
                }
                return true
            }
        })
    }

    private fun searchAyat(query: String) {
        // Ensure ayatsList is not empty or null
        if (ayatsList.isNotEmpty()) {
            val index = ayatsList.indexOfFirst {
                it.pAyatID.toString() == query ||
                        it.pAyatID.toString().contains(query, ignoreCase = true)
            }

            if (index != -1) {
                recyclerView.scrollToPosition(index)
            } else {
                Toast.makeText(requireContext(), "Invalid Ayat No", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No data loaded", Toast.LENGTH_SHORT).show()
        }
    }
}
