package com.example.quran.ui.tasbih

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
import com.example.quran.ParaVerseActivity
import com.example.quran.adapters.ParaAdapter
import com.example.quran.RetrofitClient
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.databinding.FragmentTasbihBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException


class TasbihFragment : Fragment() {

    private var _binding: FragmentTasbihBinding? = null
    private val binding get() = _binding!!
    private lateinit var tasbihViewModel: TasbihViewModel
    private lateinit var adapter: ParaAdapter
    private lateinit var sqliteHelper: SQLiteHelper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        tasbihViewModel = ViewModelProvider(this)[TasbihViewModel::class.java]
        _binding = FragmentTasbihBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sqliteHelper = SQLiteHelper(requireContext())

        binding.paraRV.layoutManager = LinearLayoutManager(context)
        adapter = ParaAdapter(emptyList()) { paraNumber ->
            // Create a new instance of VerseFragment
            val intent = Intent(requireContext(), ParaVerseActivity::class.java).apply {
                putExtra("PARA_NUMBER", paraNumber)
                putExtra("PARA_NAME", tasbihViewModel.getParahName(paraNumber))
                putExtra("PARA_ARABIC_NAME", tasbihViewModel.getParahArabicName(paraNumber))
            }
            startActivity(intent)
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

    private fun fetchSurahs() {

            GlobalScope.launch(Dispatchers.IO) {
                val parahs = withTimeoutOrNull(4000) {
                    try {
                        RetrofitClient.api.getParahs().also { fetchedParas ->
                            if (sqliteHelper.getParas().isEmpty()) {
                                sqliteHelper.insertParas(fetchedParas)
                            }
                        }
                    } catch (e: IOException) {
                        null
                    } catch (e: HttpException) {
                        null
                    }
                }
                withContext(Dispatchers.Main) {
                    if (parahs != null) {
                        tasbihViewModel.setParahs(parahs)
                        adapter.updateParas(parahs)
                    } else {
                        val localParahs = sqliteHelper.getParas()
                        if (localParahs.isNotEmpty()) {
                            tasbihViewModel.setParahs(localParahs)
                            adapter.updateParas(localParahs)
                        } else {
                            Toast.makeText(context, "No data available offline.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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

