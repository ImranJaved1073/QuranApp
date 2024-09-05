package com.example.quran.ui

import android.content.Intent
import retrofit2.HttpException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.RetrofitClient
import com.example.quran.SurahAdapter
import com.example.quran.TafseerQuranActivity
import com.example.quran.PrayerTimeViewModel
import com.example.quran.databinding.FragmentPrayertimeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class PrayerTimeFragment : Fragment() {

    private var _binding: FragmentPrayertimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prayerTimeViewModel: PrayerTimeViewModel
    private lateinit var adapter: SurahAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        prayerTimeViewModel = ViewModelProvider(this)[PrayerTimeViewModel::class.java]
        _binding = FragmentPrayertimeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.surahRV.layoutManager = LinearLayoutManager(context)
        adapter = SurahAdapter(emptyList()) { surahNumber ->
            // Create a new instance of VerseFragment
            val intent = Intent(requireContext(), TafseerQuranActivity::class.java).apply {
                putExtra("SURAH_NUMBER", surahNumber)
            }
            startActivity(intent)
        }
        binding.surahRV.adapter = adapter

        binding.suraListSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    adapter.filter(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapter.filter(newText)
                }
                return true
            }
        })
        // Fetch Surahs
        fetchSurahs()

        return root
    }

    private fun fetchSurahs() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val surahs = RetrofitClient.api.getSurahs()

                // Update the LiveData on the main thread
                withContext(Dispatchers.Main) {
                    prayerTimeViewModel.setSurahs(surahs)
                    adapter.updateSurahs(surahs)
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "API Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

