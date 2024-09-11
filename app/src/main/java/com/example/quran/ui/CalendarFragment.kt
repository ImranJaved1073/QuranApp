package com.example.quran.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.quran.HijriDateResponse
import com.example.quran.R
import com.example.quran.RetrofitClient
import com.example.quran.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var ayahArabicText: TextView
    private lateinit var ayahEnglishTranslation: TextView
    private lateinit var ayahUrduTranslation: TextView
    private lateinit var gregorianDateTextView: TextView
    private lateinit var gregorianTimeTextView: TextView
    private lateinit var islamicDateTextView: TextView
    private lateinit var islamicMonthTextView: TextView
    private lateinit var ayatnumber: TextView
    private lateinit var surahname: TextView
    private  lateinit var islamicYearTextView: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "quran_prefs"
    private val KEY_RANDOM_AYAH = "random_ayah"
    private val KEY_AYAH_DATE = "ayah_date"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // Initialize TextViews
        ayahArabicText = view.findViewById(R.id.ayahArabicText)
        ayahEnglishTranslation = view.findViewById(R.id.ayahEnglishTranslation)
        ayahUrduTranslation = view.findViewById(R.id.ayahUrduTranslation)
        surahname = view.findViewById(R.id.surah_Name)
        gregorianDateTextView = view.findViewById(R.id.currentDate)
        gregorianTimeTextView = view.findViewById(R.id.currentTime)
        islamicMonthTextView = view.findViewById(R.id.islamicDate)
        islamicDateTextView = view.findViewById(R.id.dateNumber)
        ayatnumber = view.findViewById(R.id.ayahNumberLabel)
        islamicYearTextView = view.findViewById(R.id.islamicYear)


        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Update date and time
        fetchHijriDate()

        // Fetch or retrieve random Ayah for the day
        fetchOrRetrieveRandomAyah()

        return view
    }



    private fun fetchHijriDate() {
        val currentDayname = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        gregorianDateTextView.text = currentDate
        gregorianTimeTextView.text = currentDayname

        GlobalScope.launch(Dispatchers.IO) {
            val call = RetrofitInstance.api.getHijriDate(currentDate)
            call.enqueue(object : Callback<HijriDateResponse> {
                override fun onResponse(call: Call<HijriDateResponse>, response: Response<HijriDateResponse>) {
                    if (response.isSuccessful) {
                        val hijriDate = response.body()?.data?.hijri
                        hijriDate?.let {
                            GlobalScope.launch(Dispatchers.Main) {
                                islamicDateTextView.text = it.day
                                islamicMonthTextView.text = it.month.en
                                islamicYearTextView.text = it.year
                            }
                        } ?: run {
                            Log.d("MainActivity", "No Hijri date data available")
                        }
                    } else {
                        Log.e("MainActivity", "API response error: ${response.code()} ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<HijriDateResponse>, t: Throwable) {
                    Log.e("MainActivity", "API call failed: ${t.message}")
                }
            })
        }
    }


    private fun fetchOrRetrieveRandomAyah() {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedAyahDate = sharedPreferences.getString(KEY_AYAH_DATE, null)
        val savedAyahNumber = sharedPreferences.getString(KEY_RANDOM_AYAH, null)

        if (savedAyahDate == todayDate && savedAyahNumber != null) {
            // Use the saved Ayah
            displayAyah(savedAyahNumber.toInt())
        } else {
            // Generate a new random Ayah
            val randomAyahNumber = (1..6236).random() // Assuming there are 6236 Ayahs

            lifecycleScope.launch {
                try {
                    val randomAyah = RetrofitClient.api.getAyat(randomAyahNumber)
                    if (randomAyah != null) {
                        // Save the new Ayah and the date
                        sharedPreferences.edit().putString(KEY_RANDOM_AYAH, randomAyahNumber.toString()).apply()
                        sharedPreferences.edit().putString(KEY_AYAH_DATE, todayDate).apply()

                        // Display the new Ayah
                        displayAyah(randomAyahNumber)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle error
                }
            }
        }
    }

    private fun displayAyah(ayahNumber: Int) {
        lifecycleScope.launch {
            try {
                val ayah = RetrofitClient.api.getAyat(ayahNumber)
                if (ayah != null) {
                    // Update UI with Ayah details
                    ayahArabicText.text = ayah.arabicText
                    ayahEnglishTranslation.text = ayah.drMohsinKhan
                    ayahUrduTranslation.text = ayah.fatehMuhammadJalandhrield
                    ayatnumber.text = "${ayah.paraID}:${ayah.ayaNo}"
                    surahname.text = ayah.suraID.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }
}
