package com.example.quran.ui

import android.net.http.HttpException
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.ParaAdapter
import com.example.quran.ParaVerseFragment
import com.example.quran.R
import com.example.quran.RetrofitClient
import com.example.quran.TasbihViewModel
import com.example.quran.databinding.FragmentTasbihBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class TasbihFragment : Fragment() {

    private var _binding: FragmentTasbihBinding? = null
    private val binding get() = _binding!!
    private lateinit var tasbihViewModel: TasbihViewModel
    private lateinit var adapter: ParaAdapter

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        tasbihViewModel = ViewModelProvider(this).get(TasbihViewModel::class.java)
        _binding = FragmentTasbihBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.paraRV.layoutManager = LinearLayoutManager(context)
        adapter = ParaAdapter(emptyList()) { paraNumber ->
            // Create a new instance of VerseFragment
            val verseFragment = ParaVerseFragment().apply {
                arguments = Bundle().apply {
                    putInt("PARA_NUMBER", paraNumber)
                    putString("PARA_NAME", tasbihViewModel.getParahName(paraNumber))
                    putString("PARA_ARABIC_NAME", tasbihViewModel.getParahArabicName(paraNumber))
                }
            }

            findNavController().navigate(R.id.action_navigation_tasbih_to_navigation_Para_verse, verseFragment.arguments)
        }
        binding.paraRV.adapter = adapter

        binding.paraSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
    @RequiresApi(Build.VERSION_CODES.S)
    private fun fetchSurahs() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val parahs = RetrofitClient.api.getParahs()

                // Update the LiveData on the main thread
                withContext(Dispatchers.Main) {
                    tasbihViewModel.setParahs(parahs)
                    adapter.updateParas(parahs)
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

