package com.example.quran.ui

import android.app.AlertDialog
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.PdfPageAdapter
import com.example.quran.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class PrayerTimeFragment : Fragment() {

    private lateinit var pdfRenderer: PdfRenderer
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var tempFile: File? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var pdfPageAdapter: PdfPageAdapter

    private val surahStartPages = listOf(
        10, 11, 54, 78, 105, 124, 145, 168, 177, 196, 208, 221, 233, 239, 244,
        249, 263, 273, 284, 290, 299, 308, 317, 324, 333, 339, 348, 356, 366,
        373, 379, 382, 385, 394, 400, 405, 410, 417, 421, 429, 438, 443, 449,
        455, 457, 461, 465, 469, 472, 475, 477, 480, 482, 484, 487, 490, 493,
        497, 500, 504, 506, 508, 509, 511, 513, 515, 517, 519, 521, 523, 525,
        527, 529, 530, 532, 533, 535, 537, 538, 539, 541, 541, 542, 543, 544,
        545, 546, 546, 547, 548, 549, 549, 550, 550, 551, 551, 552, 552, 553,
        553, 553, 554, 554, 554, 555, 555, 555, 555, 556, 556, 556, 556, 557,
        557
    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prayertime, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewPdf)

        val buttonSearch: Button = view.findViewById(R.id.buttonSearch)

        buttonSearch.setOnClickListener {
            showSearchDialog()
        }

        try {
            // Copy PDF from assets to a temporary file
            tempFile = createTempFileFromAssets("quran.pdf")
            fileDescriptor = ParcelFileDescriptor.open(tempFile!!, ParcelFileDescriptor.MODE_READ_ONLY)

            pdfRenderer = PdfRenderer(fileDescriptor!!)

            // Set up RecyclerView
            pdfPageAdapter = PdfPageAdapter(pdfRenderer)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = pdfPageAdapter

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return view
    }

    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search, null)
        val editTextSurahNumber: EditText = dialogView.findViewById(R.id.editTextSurahNumber)
        val editTextPageNumber: EditText = dialogView.findViewById(R.id.editTextPageNumber)
        val editTextParaNumber: EditText = dialogView.findViewById(R.id.editTextParaNumber)
        val buttonSearch: Button = dialogView.findViewById(R.id.buttonSearch)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Search")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        buttonSearch.setOnClickListener {
            val surahNumber = editTextSurahNumber.text.toString().toIntOrNull()
            val pageNumber = editTextPageNumber.text.toString().toIntOrNull()
            val paraNumber = editTextParaNumber.text.toString().toIntOrNull()

            when {
                surahNumber != null -> navigateToSurah(surahNumber)
                pageNumber != null -> navigateToPage(pageNumber)
                paraNumber != null -> navigateToPara(paraNumber)
                else -> {} // No action if no valid input
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun createTempFileFromAssets(assetName: String): File {
        val tempFile = File(requireContext().cacheDir, assetName)
        val inputStream: InputStream = requireContext().assets.open(assetName)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }

    private fun navigateToSurah(surahNumber: Int) {
        val pageIndex = surahStartPages.getOrNull(surahNumber - 1) ?: return
        recyclerView.scrollToPosition(pageIndex - 1) // Adjust index for 0-based list
    }

    private fun navigateToPage(pageNumber: Int) {
        val pageIndex = pageNumber - 1
        recyclerView.scrollToPosition(pageIndex)
    }

    private fun navigateToPara(paraNumber: Int) {
        if (paraNumber == 1)
        {
            recyclerView.scrollToPosition(9)
            return
        }
        else if (paraNumber == 2)
        {
            recyclerView.scrollToPosition(28)
            return
        }
        else if (paraNumber == 29)
        {
            recyclerView.scrollToPosition(516)
            return
        }
        else if (paraNumber == 30)
        {
            recyclerView.scrollToPosition(536)
            return
        }
        else
        {
            val pageIndex = (29 + 18 * (paraNumber - 2)) - 1
            recyclerView.scrollToPosition(pageIndex)
        }
    }

    private fun getPageIndexFromSurah(surahNumber: Int): Int {
        // Placeholder logic
        return surahNumber - 1
    }

    private fun getPageIndexFromPara(paraNumber: Int): Int {
        // Placeholder logic
        return paraNumber - 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pdfRenderer.close()
        fileDescriptor?.close()
        tempFile?.delete() // Clean up the temporary file
    }
}
