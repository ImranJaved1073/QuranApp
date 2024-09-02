package com.example.quran

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verse) // Use the same layout


        // Ensure home button is enabled
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the toolbar title to an empty string
        supportActionBar?.title = ""

        recyclerView = findViewById(R.id.ayatRV)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve extras from the Intent
        val surahNumber = intent.getIntExtra("SURAH_NUMBER", 1)
        val surahName = intent.getStringExtra("SURAH_NAME")
        val surahArabicName = intent.getStringExtra("SURAH_ARABIC_NAME")
        val ayahNumberToScroll = intent.getIntExtra("AYAH_NUMBER", -1)

        // Update the UI with Surah details
        findViewById<TextView>(R.id.tvSurahArabicName).text = surahNumber.toString()
        findViewById<TextView>(R.id.tvSurahName).text = surahName

        // Fetch Ayats
        fetchAyats(surahNumber, ayahNumberToScroll)
    }

    private fun fetchAyats(surahNumber: Int, ayahNumberToScroll: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Fetch Ayats using Retrofit with coroutines
                val fetchedAyats = RetrofitClient.api.getVerses(surahNumber)
                val surah = fetchedAyats.surah
                val ayahs = fetchedAyats.ayahs

                // Update the UI on the main thread
                withContext(Dispatchers.Main) {
                    ayatsList = ayahs // Update ayatsList with the fetched data
                    verseAdapter = VerseAdapter(ayatsList)
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VerseActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                // Handle API errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@VerseActivity, "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_surah, menu)

        // Handle menu item clicks
        menu?.findItem(R.id.action_search)?.setOnMenuItemClickListener {
            showSearchOptionsDialog()
            true
        }
        return true
    }

    private fun showSearchOptionsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search By")

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
        builder.setTitle("Search by $searchType")

        // Set up the input field
        val input = EditText(this)
        input.hint = "Enter $searchType Number"
        builder.setView(input)

        builder.setPositiveButton("Search") { _, _ ->
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
}
