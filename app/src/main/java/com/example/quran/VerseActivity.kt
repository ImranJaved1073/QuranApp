package com.example.quran

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.quran.models.Bookmark
import com.example.quran.models.Verse
import com.example.quran.ui.MiniPlayerFragment
import com.example.quran.ui.VerseBottomSheetFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.io.IOException

class VerseActivity : AppCompatActivity() {

    private var count: Int = 0
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
        val isEnglishTranslationEnabled =
            preferences.getBoolean("english_translation_enabled", true)
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
        fetchAyats(
            surahNumber,
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

    private fun showBottomSheet(verse: Verse) {
        val bottomSheet = VerseBottomSheetFragment(verse)
        bottomSheet.show(supportFragmentManager, VerseBottomSheetFragment.TAG)
    }

    private fun playAyat(verse: Verse) {
        val miniPlayerFragment = MiniPlayerFragment()
        miniPlayerFragment.playAudio(verse.suraID, verse.ayaNo)
        miniPlayerFragment.show(supportFragmentManager, "MiniPlayerFragment")
    }

    private fun updateBookmarkedVerses(verses: List<Verse>, bookmarkedAyats: List<Bookmark>) {
        verses.forEach { verse ->
            verse.isBookmarked = bookmarkedAyats.any { it.ayaID == verse.ayaID }
        }
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
        GlobalScope.launch(Dispatchers.IO) {
            // Attempt to fetch data with a timeout
            val fetchedAyats = withTimeoutOrNull(5000) {
                try {
                    RetrofitClient.api.getVerses(surahNumber).also { fetchedVerses ->
                        val surah = fetchedVerses.surah
                        val ayahs = fetchedVerses.ayahs

                        sqLiteHelper = SQLiteHelper(this@VerseActivity)
                        if (sqLiteHelper.getVerses(surah.number).isEmpty()) {
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
                    ayatsList = fetchedAyats.ayahs

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
                    fetchFromLocalDatabase(
                        surahNumber,
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
        try {
            sqLiteHelper = SQLiteHelper(this)
            ayatsList = sqLiteHelper.getVerses(surahNumber)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_surah, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        //searchItem?.icon?.setTint(getColor(R.color.white))

        // Handle menu item clicks
        searchItem?.setOnMenuItemClickListener {
            showSearchOptionsDialog()
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

        if (searchType == "Ruku")
            count = ayatsList.groupBy { it.rakuID }.size
        else
            count= ayatsList.size


        // Set up the input field
        val input = EditText(this)
        input.hint = "1-$count"
        builder.setView(input)

        // Create the "Find" button
        builder.setPositiveButton("Find") { _, _ ->
            val query = input.text.toString()
            searchAyat(query, searchType)
        }
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()

        // Set the "Find" button to be initially disabled
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            positiveButton.isEnabled = false
            negativeButton.isEnabled = true

            // Add a TextWatcher to the input field
            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Enable the "Find" button if text is not empty
                    positiveButton.isEnabled = s?.isNotEmpty() == true
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        dialog.show()
    }


    private fun searchAyat(query: String, searchType: String) {
        if (ayatsList.isNotEmpty()) {
            val index = when (searchType) {
                "Ruku" -> ayatsList.indexOfFirst { it.rakuID.toString() == query }
                "Ayah" -> ayatsList.indexOfFirst { it.ayaNo.toString() == query }
                else -> -1
            }

            if (index != -1) {
                recyclerView.scrollToPosition(index)
            } else {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No data loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLastReadAyat(surahNumber: Int, ayahNumber: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putInt("LAST_READ_SURAH_NUMBER", surahNumber)
        editor.putInt("LAST_READ_AYAH_NUMBER", ayahNumber)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()

        // Get the current position of the visible Ayat
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (visiblePosition != RecyclerView.NO_POSITION && ayatsList.isNotEmpty()) {
            val currentAyat = ayatsList[visiblePosition]
            saveLastReadAyat(currentAyat.suraID, currentAyat.ayaNo) // Save the Surah and Ayah number
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
