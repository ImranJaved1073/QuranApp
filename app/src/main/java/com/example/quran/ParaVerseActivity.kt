package com.example.quran

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.adapters.VerseAdapter
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.models.Verse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ParaVerseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var verseAdapter: VerseAdapter
    private var ayatsList: List<Verse> = emptyList() // Initialize as an empty list
    private lateinit var sqLiteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_para_verse)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Set the toolbar title to an empty string

        sqLiteHelper = SQLiteHelper(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isArabicEnabled = preferences.getBoolean("arabic_enabled", true)
        val isTranslationEnabled = preferences.getBoolean("translation_enabled", true)
        val arabicFontSize = preferences.getInt("arabic_font_size", 22)
        val translationFontSize = preferences.getInt("translation_font_size", 22)
        val isEnglishTranslationEnabled = preferences.getBoolean("english_translation_enabled", true)
        val englishTranslationFontSize = preferences.getInt("english_translation_font_size", 18)
        val urduTranslationSelected = preferences.getInt("urdu_translation_selected", 0)
        val englishTranslationSelected = preferences.getInt("english_translation_selected", 0)


        recyclerView = findViewById(R.id.ayatParaRV)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val paraNumber = intent.getIntExtra("PARA_NUMBER", 1)
        val paraName = intent.getStringExtra("PARA_NAME")
        val paraArabicName = intent.getStringExtra("PARA_ARABIC_NAME")
        val ayahNumberToScroll = intent.getIntExtra("AYAH_NUMBER", -1)

        // Update the UI with Surah details
        findViewById<TextView>(R.id.tvParaName).text = paraName
        findViewById<TextView>(R.id.tvParaArabicName).text = paraArabicName


        fetchAyats(paraNumber, ayahNumberToScroll,isArabicEnabled, isTranslationEnabled, arabicFontSize, translationFontSize, isEnglishTranslationEnabled,englishTranslationFontSize, urduTranslationSelected, englishTranslationSelected)
    }

    private fun showBottomSheet(verse: Verse) {
        val bottomSheet = VerseBottomSheetFragment(verse)
        bottomSheet.show(supportFragmentManager, VerseBottomSheetFragment.TAG)
    }

    private fun fetchAyats(paraNumber: Int, ayahNumberToScroll: Int,isArabicEnabled: Boolean,
                           isTranslationEnabled: Boolean,
                           arabicFontSize: Int,
                           translationFontSize: Int, isEnglishTranslationEnabled: Boolean, englishTranslationFontSize: Int,
                           urduTranslationSelected: Int, englishTranslationSelected: Int) {
        GlobalScope.launch {
            try {
                // Fetch Ayats using Retrofit with coroutines
                val fetchedAyats = RetrofitClient.api.getParaVerses(paraNumber)
                val para = fetchedAyats.para
                val ayahs = fetchedAyats.ayahs

                // Update the UI on the main thread
                runOnUiThread {
                    ayatsList = ayahs // Update ayatsList with the fetched data
                    verseAdapter = VerseAdapter(ayatsList,isArabicEnabled,
                        isTranslationEnabled,
                        arabicFontSize,
                        translationFontSize, isEnglishTranslationEnabled, englishTranslationFontSize,urduTranslationSelected, englishTranslationSelected){ verse ->
                        showBottomSheet(verse)
                    }
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
                runOnUiThread {
                    Toast.makeText(this@ParaVerseActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VerseFragment", "Network Error: ${e.message}")
                }
            } catch (e: HttpException) {
                // Handle API errors
                runOnUiThread {
                    Toast.makeText(this@ParaVerseActivity, "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VerseFragment", "API Error: ${e.message}")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_surah, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        searchItem?.icon?.setTint(getColor(R.color.white))
        val settingItem = menu?.findItem(R.id.action_settings)
        settingItem?.icon?.setTint(getColor(R.color.white))

        // Handle menu item clicks
        searchItem?.setOnMenuItemClickListener {
            showSearchOptionsDialog()
            true
        }
        settingItem?.setOnMenuItemClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }

        return true
    }

    private fun showSearchOptionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Find By")

        // Add options
        val options = arrayOf("Ayah", "Ruku")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> { // Search by Ayah
                    openSearchDialog("Ayah")
                }
                1 -> { // Search by Ruku
                    openSearchDialog("Ruku")
                }
            }
        }
        builder.show()
    }

    private fun openSearchDialog(searchType: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Find by $searchType")

        // Set up the input field
        val input = EditText(this)
        input.hint = "Enter $searchType Number"
        builder.setView(input)

        builder.setPositiveButton("Find") { _, _ ->
            val query = input.text.toString()
            searchAyat(query, searchType)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun searchAyat(query: String, searchType: String) {
        if (ayatsList.isNotEmpty()) {
            val index = when (searchType) {
                "Ruku" -> ayatsList.indexOfFirst { it.rakuID.toString() == query }
                "Ayah" -> ayatsList.indexOfFirst {
                    it.ayaNo.toString() == query ||
                            it.arabicText.contains(query, ignoreCase = true) ||
                            it.drMohsinKhan.contains(query, ignoreCase = true) ||
                            it.fatehMuhammadJalandhrield.contains(query, ignoreCase = true)
                }
                else -> -1
            }

            if (index != -1) {
                recyclerView.scrollToPosition(index)
            } else {
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No data loaded", Toast.LENGTH_SHORT).show()
        }
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
                Toast.makeText(this, "Invalid Ayat No", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No data loaded", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
