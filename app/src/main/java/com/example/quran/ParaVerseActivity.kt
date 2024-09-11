package com.example.quran

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.adapters.VerseAdapter
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.models.Bookmark
import com.example.quran.models.Verse
import com.example.quran.ui.MiniPlayerFragment
import com.example.quran.ui.VerseBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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

    private fun playAyat(verse: Verse) {
        val miniPlayerFragment = MiniPlayerFragment()
        miniPlayerFragment.playAudio(verse.suraID, verse.ayaNo)
        miniPlayerFragment.show(supportFragmentManager, "MiniPlayerFragment")
    }

    private fun fetchAyats(paraNumber: Int, ayahNumberToScroll: Int,isArabicEnabled: Boolean,
                           isTranslationEnabled: Boolean,
                           arabicFontSize: Int,
                           translationFontSize: Int, isEnglishTranslationEnabled: Boolean, englishTranslationFontSize: Int,
                           urduTranslationSelected: Int, englishTranslationSelected: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            // Attempt to fetch data with a timeout
            val fetchedAyats = withTimeoutOrNull(5000) {
                try {
                    RetrofitClient.api.getParaVerses(paraNumber).also { fetchedVerses ->
                        val para = fetchedVerses.para
                        val ayahs = fetchedVerses.ayahs

                        sqLiteHelper = SQLiteHelper(this@ParaVerseActivity)
                        if (sqLiteHelper.getVerses(para.paraID).isEmpty()) {
                            sqLiteHelper.insertVerses(ayahs)
                        }
                        updateBookmarkedVerses(ayahs, sqLiteHelper.getAllBookmarks())

                    }
                } catch (e: IOException) {
                    null
                } catch (e: HttpException) {
                    null
                }
            }
            withContext(Dispatchers.Main) {
                if (fetchedAyats != null) {
                    ayatsList = fetchedAyats.ayahs // Update ayatsList with the fetched data
                    verseAdapter = VerseAdapter(ayatsList,isArabicEnabled,
                        isTranslationEnabled,
                        arabicFontSize,
                        translationFontSize, isEnglishTranslationEnabled, englishTranslationFontSize,urduTranslationSelected, englishTranslationSelected,
                        { verse -> showBottomSheet(verse) }, // Bottom sheet click listener
                        { verse -> playAyat(verse) })
                    recyclerView.adapter = verseAdapter

                    // Scroll to the specified Ayah if provided
                    if (ayahNumberToScroll != -1) {
                        recyclerView.scrollToPosition(
                            ayatsList.indexOfFirst { it.ayaNo == ayahNumberToScroll }
                        )
                    }
                } else {
                    fetchFromLocalDatabase(
                        paraNumber,
                        ayahNumberToScroll,
                        isArabicEnabled,
                        isTranslationEnabled,
                        arabicFontSize,
                        translationFontSize,
                        isEnglishTranslationEnabled,
                        englishTranslationFontSize,
                        urduTranslationSelected,
                        englishTranslationSelected
                    )
                }

            }
        }
    }

    private fun fetchFromLocalDatabase(
        paraNumber: Int,
        ayahNumberToScroll: Int,
        isArabicEnabled: Boolean,
        isTranslationEnabled: Boolean,
        arabicFontSize: Int,
        translationFontSize: Int,
        isEnglishTranslationEnabled: Boolean,
        englishTranslationFontSize: Int,
        urduTranslationSelected: Int,
        englishTranslationSelected: Int
    ) {
        try {
            sqLiteHelper = SQLiteHelper(this)
            ayatsList = sqLiteHelper.getParaVerses(paraNumber)
            updateBookmarkedVerses(ayatsList, sqLiteHelper.getAllBookmarks())
            if (ayatsList.isNotEmpty()) {
                verseAdapter = VerseAdapter(
                    ayatsList,
                    isArabicEnabled,
                    isTranslationEnabled,
                    arabicFontSize,
                    translationFontSize,
                    isEnglishTranslationEnabled,
                    englishTranslationFontSize,
                    urduTranslationSelected,
                    englishTranslationSelected,
                    { verse -> showBottomSheet(verse) }, // Bottom sheet click listener
                    { verse -> playAyat(verse) } // Play button click listener
                )

                recyclerView.adapter = verseAdapter
                if (ayahNumberToScroll != -1) {
                    recyclerView.scrollToPosition(
                        ayatsList.indexOfFirst { it.ayaNo == ayahNumberToScroll }
                    )
                }
            } else {
                Toast.makeText(this, "No data available offline.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: Database not initialized.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBookmarkedVerses(verses: List<Verse>, bookmarkedAyats: List<Bookmark>) {
        verses.forEach { verse ->
            verse.isBookmarked = bookmarkedAyats.any { it.ayaID == verse.ayaID }
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
