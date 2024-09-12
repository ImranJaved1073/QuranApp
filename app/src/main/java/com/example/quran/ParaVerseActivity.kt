package com.example.quran

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
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

    private var count: Int = 0
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

        // Creating a list of distinct Surah IDs or names dynamically
        val surahListOnSpinner = ayatsList.map { it.suraID }.distinct()  // Replace 'suraID' with 'suraName' if needed

        // Create the spinner and set its adapter
        val spinner = Spinner(this)
        val adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item_custom,
            surahListOnSpinner  // Assuming you want to show IDs; replace with names if you have them
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_custom)
        spinner.adapter = adapter

        // Set up the input field
        val input = EditText(this)

        // Set layout parameters with margin for spinner
        val spinnerLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        spinnerLayoutParams.setMargins(16, 16, 16, 8)  // Set margins (left, top, right, bottom)
        spinner.layoutParams = spinnerLayoutParams

        // Set layout parameters with margin for input field
        val inputLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        inputLayoutParams.setMargins(16, 8, 16, 16)
        input.layoutParams = inputLayoutParams

        // Add both spinner and input field to the dialog layout
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(24, 24, 24, 24)  // Set padding (left, top, right, bottom)
        layout.addView(spinner)
        layout.addView(input)

        builder.setView(layout)

        // Initial calculation of count based on the first Surah selected
        fun updateCount() {
            val selectedSurahId = spinner.selectedItem.toString().toInt()  // Assuming suraID is an integer
            val count = if (searchType == "Ayah") {
                ayatsList.filter { selectedSurahId == it.suraID }.size
            } else {
                ayatsList.filter { selectedSurahId == it.suraID }.groupBy { it.rakuID }.size
            }
            val startingNumber = if (searchType == "Ayah") {
                ayatsList.firstOrNull { it.suraID == selectedSurahId }?.ayaNo ?: 1
            } else {
                ayatsList.firstOrNull { it.suraID == selectedSurahId }?.rakuID ?: 1
            }
            input.hint = "$startingNumber-${count + startingNumber - 1}"
        }

        // Set listener to update count whenever a new Surah is selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateCount()  // Update the count when a Surah is selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Initialize count and input hint for the first Surah selected
        updateCount()

        // Create the dialog and "Find" button
        val dialog = builder.create()

        // Set positive button initially disabled
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Find") { _, _ ->
            val query = input.text.toString()
            searchAyat(query, spinner.selectedItem.toString(), searchType)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { _, _ -> dialog.dismiss() }

        dialog.show()

        // Disable "Find" button initially
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        // Add TextWatcher to enable/disable the "Find" button based on input text
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Enable the "Find" button only if there is text in the EditText
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun searchAyat(ayatNo: String,surahNo: String, searchType: String) {
        if (ayatsList.isNotEmpty()) {
            val index = when (searchType) {
                "Ruku" -> ayatsList.indexOfFirst { it.rakuID.toString() == ayatNo && it.suraID.toString() == surahNo }
                "Ayah" -> ayatsList.indexOfFirst { it.ayaNo.toString() == ayatNo && it.suraID.toString() == surahNo }
                else -> -1
            }

            if (index != -1) {
                recyclerView.scrollToPosition(index)
            } else {
                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
                openSearchDialog(searchType)
            }
        } else {
            Toast.makeText(this, "No data loaded", Toast.LENGTH_SHORT).show()
            openSearchDialog(searchType)
        }
    }

//    private fun searchAyat(query: String) {
//        // Ensure ayatsList is not empty or null
//        if (ayatsList.isNotEmpty()) {
//            val index = ayatsList.indexOfFirst {
//                it.pAyatID.toString() == query ||
//                        it.pAyatID.toString().contains(query, ignoreCase = true)
//            }
//
//            if (index != -1) {
//                recyclerView.scrollToPosition(index)
//            } else {
//                Toast.makeText(this, "Invalid Ayat No", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(this, "No data loaded", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
