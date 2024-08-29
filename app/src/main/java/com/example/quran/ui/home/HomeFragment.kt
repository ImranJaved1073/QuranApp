package com.example.quran.ui.home

import android.content.Intent
import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresExtension
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.RetrofitClient
import com.example.quran.SurahAdapter
import com.example.quran.VerseActivity
import com.example.quran.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: SurahAdapter

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.surahRV.layoutManager = LinearLayoutManager(context)
        adapter = SurahAdapter(emptyList()) { surahNumber ->
            val intent = Intent(context, VerseActivity::class.java)
            intent.putExtra("SURAH_NUMBER", surahNumber)
            intent.putExtra("SURAH_NAME", homeViewModel.getSurahName(surahNumber))
            intent.putExtra("SURAH_ARABIC_NAME", homeViewModel.getSurahArabicName(surahNumber))
            startActivity(intent)
        }
        binding.surahRV.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    private fun fetchSurahs() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getSurahs()
                val surahs = response.data

                // Update the LiveData on the main thread
                withContext(Dispatchers.Main) {
                    homeViewModel.setSurahs(surahs)
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
