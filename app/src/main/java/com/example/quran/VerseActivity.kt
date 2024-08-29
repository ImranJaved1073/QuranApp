package com.example.quran


import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class VerseActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verse)

        recyclerView = findViewById(R.id.ayatRV)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val surahNumber = intent.getIntExtra("SURAH_NUMBER", 1)
        val surahName = intent.getStringExtra("SURAH_NAME")
        val surahArabicName = intent.getStringExtra("SURAH_ARABIC_NAME")

        // Update the UI with Surah details
        findViewById<TextView>(R.id.tvSurahName).text = surahName
        findViewById<TextView>(R.id.tvSurahArabicName).text = surahArabicName

        // Fetch Ayats using GlobalScope.launch
        fetchAyats(surahNumber)
    }

    private fun fetchAyats(surahNumber: Int) {
        GlobalScope.launch {
            try {
                // Fetch Ayats using Retrofit with coroutines
                val arabicResponse = RetrofitClient.api.getVerses(surahNumber)
                val englishResponse = RetrofitClient.api.getEnglishTranslation(surahNumber)
                val urduResponse = RetrofitClient.api.getUrduTranslation(surahNumber)

                // Access the ayahs list from the response
                val arabicAyats = arabicResponse.data.ayahs
                val englishAyats = englishResponse.data.ayahs
                val urduAyats = urduResponse.data.ayahs

                // Update the UI on the main thread
                runOnUiThread {
                    recyclerView.adapter = VerseAdapter(arabicAyats, englishAyats, urduAyats)
                }
            } catch (e: IOException) {
                // Handle network errors
                runOnUiThread {
                    Toast.makeText(this@VerseActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VerseActivity", "Network Error: ${e.message}")
                }
            } catch (e: HttpException) {
                // Handle API errors
                runOnUiThread {
                    Toast.makeText(this@VerseActivity, "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("VerseActivity", "API Error: ${e.message}")
                }
            }
        }
    }
}
