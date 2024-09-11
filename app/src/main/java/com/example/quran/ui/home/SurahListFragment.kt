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
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.RetrofitClient
import com.example.quran.adapters.SurahAdapter
import com.example.quran.databinding.FragmentSurahListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.example.quran.VerseActivity
import com.example.quran.dataAccess.SQLiteHelper
import kotlinx.coroutines.withTimeoutOrNull

class SurahListFragment : Fragment() {

    private var _binding: FragmentSurahListBinding? = null
    private val binding get() = _binding!!
    private lateinit var surahListViewModel: SurahListViewModel
    private lateinit var adapter: SurahAdapter
    private lateinit var sqliteHelper: SQLiteHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        surahListViewModel = ViewModelProvider(this)[SurahListViewModel::class.java]
        _binding = FragmentSurahListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sqliteHelper = SQLiteHelper(requireContext())

        binding.surahRV.layoutManager = LinearLayoutManager(context)
        adapter = SurahAdapter(emptyList()) { surahNumber ->
            navigateToVerseActivity(surahNumber, -1)
        }
        binding.surahRV.adapter = adapter

        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val lastReadSurahNumber = preferences.getInt("LAST_READ_SURAH_NUMBER", -1)
        val lastReadAyahNumber = preferences.getInt("LAST_READ_AYAH_NUMBER", -1)

        // Navigate to the last read ayat when FAB is clicked
        binding.fabLastRead.setOnClickListener {
            if (lastReadSurahNumber != -1 && lastReadAyahNumber != -1) {
                navigateToVerseActivity(lastReadSurahNumber, lastReadAyahNumber)
            } else {
                Toast.makeText(requireContext(), "No last read ayat found.", Toast.LENGTH_SHORT).show()
            }
        }

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

        fetchSurahs()

        return root
    }

    private fun navigateToVerseActivity(surahNumber: Int, ayahNumber: Int) {
        val intent = Intent(requireContext(), VerseActivity::class.java).apply {
            putExtra("SURAH_NUMBER", surahNumber)
            putExtra("AYAH_NUMBER", ayahNumber)
            putExtra("SURAH_NAME", surahListViewModel.getSurahName(surahNumber))
            putExtra("SURAH_ARABIC_NAME", surahListViewModel.getSurahArabicName(surahNumber))
            putExtra("REVELATION_TYPE", surahListViewModel.getSurahRevelationType(surahNumber))
        }
        startActivity(intent)
    }

    private fun fetchSurahs() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            // Wait for up to 5 seconds for network response
            val surahs = withTimeoutOrNull(4000) {
                try {
                    RetrofitClient.api.getSurahs().also { fetchedSurahs ->
                        // Save fetched surahs to local database
                        if (sqliteHelper.getSurahs().isEmpty()) {
                            sqliteHelper.insertSurahs(fetchedSurahs)
                        }
                    }
                } catch (e: IOException) {
                    null  // Return null in case of network errors
                } catch (e: HttpException) {
                    null  // Return null in case of HTTP errors
                }
            }

            withContext(Dispatchers.Main) {
                if (surahs != null) {
                    surahListViewModel.setSurahs(surahs)
                    adapter.updateSurahs(surahs)
                } else {
                    // Fetch from local DB if network call times out or fails
                    val localSurahs = sqliteHelper.getSurahs()
                    if (localSurahs.isNotEmpty()) {
                        surahListViewModel.setSurahs(localSurahs)
                        adapter.updateSurahs(localSurahs)
                    } else {
                        showToast("No data available offline.")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {  // Check if the fragment is attached to an activity
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

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
