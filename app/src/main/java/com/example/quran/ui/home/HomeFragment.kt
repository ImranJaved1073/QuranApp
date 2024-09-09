package com.example.quran.ui.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import retrofit2.HttpException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.RetrofitClient
import com.example.quran.adapters.SurahAdapter
import com.example.quran.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.example.quran.VerseActivity
import com.example.quran.dataAccess.SQLiteHelper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: SurahAdapter
    private lateinit var sqliteHelper: SQLiteHelper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sqliteHelper = SQLiteHelper(requireContext())

        binding.surahRV.layoutManager = LinearLayoutManager(context)
        adapter = SurahAdapter(emptyList()) { surahNumber ->
            // Create a new instance of VerseFragment
            val intent = Intent(requireContext(), VerseActivity::class.java).apply {
                putExtra("SURAH_NUMBER", surahNumber)
                putExtra("SURAH_NAME", homeViewModel.getSurahName(surahNumber))
                putExtra("SURAH_ARABIC_NAME", homeViewModel.getSurahArabicName(surahNumber))
                putExtra("REVELAION_TYPE", homeViewModel.getSurahRevelationType(surahNumber))
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

        if (isOnline()) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val surahs = RetrofitClient.api.getSurahs()

                    // Save fetched surahs to local database
                    sqliteHelper.insertSurahs(surahs)

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
        } else {
            // Fetch data from the local database
            val surahs = sqliteHelper.getSurahs()
            if (surahs.isNotEmpty()) {
                homeViewModel.setSurahs(surahs)
                adapter.updateSurahs(surahs)
            } else {
                Toast.makeText(context, "No data available offline.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper function to check network status
    private fun isOnline(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
