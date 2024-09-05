//package com.example.quran
//
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.SearchView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.HttpException
//import java.io.IOException
//
//class VerseFragment : Fragment() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var verseAdapter: VerseAdapter
//    private var ayatsList: List<Verse> = emptyList() // Initialize as an empty list
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_verse, container, false)
//
//        recyclerView = view.findViewById(R.id.ayatRV)
//        recyclerView.layoutManager = LinearLayoutManager(context)
//
//        val surahNumber = arguments?.getInt("SURAH_NUMBER", 1) ?: 1
//        val surahName = arguments?.getString("SURAH_NAME")
//        val surahArabicName = arguments?.getString("SURAH_ARABIC_NAME")
//        val ayahNumberToScroll = arguments?.getInt("AYAH_NUMBER", -1) ?: -1
//
//        // Update the UI with Surah details
//        view.findViewById<TextView>(R.id.tvSurahArabicName).text = surahNumber.toString()
//        view.findViewById<TextView>(R.id.tvSurahName).text = surahName
//
//        // Fetch Ayats and setup SearchView
//        setupSearchView(view)
//        fetchAyats(surahNumber, ayahNumberToScroll)
//
//        return view
//    }
//
//    private fun fetchAyats(surahNumber: Int, ayahNumberToScroll: Int) {
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                // Fetch Ayats using Retrofit with coroutines
//                val fetchedAyats = RetrofitClient.api.getVerses(surahNumber)
//                val surah = fetchedAyats.surah
//                val ayahs = fetchedAyats.ayahs
//
//                // Update the UI on the main thread
//                withContext(Dispatchers.Main) {
//                    ayatsList = ayahs // Update ayatsList with the fetched data
//                    verseAdapter = VerseAdapter(ayatsList)
//                    recyclerView.adapter = verseAdapter
//
//                    // Scroll to the specified Ayah if provided
//                    if (ayahNumberToScroll != -1) {
//                        recyclerView.scrollToPosition(
//                            ayatsList.indexOfFirst { it.ayaNo == ayahNumberToScroll }
//                        )
//                    }
//                }
//            } catch (e: IOException) {
//                // Handle network errors
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: HttpException) {
//                // Handle API errors
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(requireContext(), "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//    private fun setupSearchView(view: View) {
//        val searchView = view.findViewById<SearchView>(R.id.searchView)
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let {
//                    searchAyat(it)
//                }
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let {
//                    searchAyat(it)
//                }
//                return true
//            }
//        })
//    }
//
//    private fun searchAyat(query: String) {
//        // Ensure ayatsList is not empty or null
//        if (ayatsList.isNotEmpty()) {
//            val index = ayatsList.indexOfFirst {
//                it.ayaNo.toString() == query ||
//                        it.arabicText.contains(query, ignoreCase = true) ||
//                        it.drMohsinKhan.contains(query, ignoreCase = true) ||
//                        it.fatehMuhammadJalandhrield.contains(query, ignoreCase = true)
//            }
//
//            if (index != -1) {
//                recyclerView.scrollToPosition(index)
//            } else {
//                Toast.makeText(requireContext(), "Invalid Ayat No", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(requireContext(), "No data loaded", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
