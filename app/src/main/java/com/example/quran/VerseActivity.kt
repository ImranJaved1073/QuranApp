package com.example.quran

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.adapters.VerseAdapter
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.models.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class VerseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var verseAdapter: VerseAdapter
    private var ayatsList: List<Verse> = emptyList() // Initialize as an empty list
    private lateinit var sqLiteHelper: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verse) // Use the same layout
        setSupportActionBar(findViewById(R.id.toolbar))


        // Ensure home button is enabled
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Set the toolbar title to an empty string

        sqLiteHelper = SQLiteHelper(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isArabicEnabled = preferences.getBoolean("arabic_enabled", true)
        val isTranslationEnabled = preferences.getBoolean("translation_enabled", true)
        val isEnglishTranslationEnabled = preferences.getBoolean("english_translation_enabled", true)
        val arabicFontSize = preferences.getInt("arabic_font_size", 22)
        val translationFontSize = preferences.getInt("translation_font_size", 22)
        val englishTranslationFontSize = preferences.getInt("english_translation_font_size", 18)
        val urduTranslationSelected = preferences.getInt("urdu_translation_selected", 0)
        val englishTranslationSelected = preferences.getInt("english_translation_selected", 0)

        recyclerView = findViewById(R.id.ayatRV)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Retrieve extras from the Intent
        val surahNumber = intent.getIntExtra("SURAH_NUMBER", 1)
        val surahName = intent.getStringExtra("SURAH_NAME")
        val surahArabicName = intent.getStringExtra("SURAH_ARABIC_NAME")
        val ayahNumberToScroll = intent.getIntExtra("AYAH_NUMBER", -1)
        val revelationType = intent.getStringExtra("REVELAION_TYPE")


        // Update the UI with Surah details
        findViewById<TextView>(R.id.tvSurahArabicName).text = surahNumber.toString()
        findViewById<TextView>(R.id.tvSurahName).text = surahName
        findViewById<ImageView>(R.id.backgroundImageView).setImageResource(if (revelationType == "Meccan") R.drawable.ic_makkah else R.drawable.ic_madinah)


        // Fetch Ayats
        fetchAyats(surahNumber, ayahNumberToScroll, isArabicEnabled, isTranslationEnabled, arabicFontSize, translationFontSize, isEnglishTranslationEnabled,englishTranslationFontSize, urduTranslationSelected , englishTranslationSelected)
    }

    private fun showBottomSheet(verse: Verse) {
        val bottomSheet = VerseBottomSheetFragment(verse)
        bottomSheet.show(supportFragmentManager, VerseBottomSheetFragment.TAG)
    }

    private fun fetchAyats(
        surahNumber: Int,
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
        if (isOnline()) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val fetchedAyats = RetrofitClient.api.getVerses(surahNumber)
                    val surah = fetchedAyats.surah
                    val ayahs = fetchedAyats.ayahs

                    sqLiteHelper = SQLiteHelper(this@VerseActivity)
                    sqLiteHelper.insertVerses(ayahs)

                    withContext(Dispatchers.Main) {
                        ayatsList = ayahs

                        verseAdapter = VerseAdapter(
                            ayatsList,
                            isArabicEnabled,
                            isTranslationEnabled,
                            arabicFontSize,
                            translationFontSize,
                            isEnglishTranslationEnabled,
                            englishTranslationFontSize,
                            urduTranslationSelected,
                            englishTranslationSelected
                        ){ verse ->
                            showBottomSheet(verse)
                        }

                        recyclerView.adapter = verseAdapter

                        if (ayahNumberToScroll != -1) {
                            recyclerView.scrollToPosition(
                                ayatsList.indexOfFirst { it.ayaNo == ayahNumberToScroll }
                            )
                        }
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@VerseActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: HttpException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@VerseActivity, "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            try {
                sqLiteHelper = SQLiteHelper(this)
                ayatsList = sqLiteHelper.getVerses(surahNumber)
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
                        englishTranslationSelected
                    ) { verse ->
                        showBottomSheet(verse)
                    }
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
                // Handle exception when the table does not exist
                Toast.makeText(this, "Error: Database not initialized.", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_surah, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        //searchItem?.icon?.setTint(getColor(R.color.white))
        val settingItem = menu?.findItem(R.id.action_settings)
        settingItem?.icon?.setTint(getColor(R.color.black))

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

}
