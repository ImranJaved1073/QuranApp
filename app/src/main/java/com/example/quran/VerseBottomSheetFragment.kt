package com.example.quran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.models.Verse
import com.example.quran.models.Bookmark
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VerseBottomSheetFragment(private val verse: Verse) : BottomSheetDialogFragment() {

    private lateinit var sqLiteHelper: SQLiteHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verse_bottom_sheet, container, false)

        // Initialize SQLiteHelper
        sqLiteHelper = SQLiteHelper(requireContext())

        // Initialize UI elements
        val playButton = view.findViewById<Button>(R.id.playButton)
        val bookmarkButton = view.findViewById<Button>(R.id.bookmarkButton)
        val copyButton = view.findViewById<Button>(R.id.copyButton)
        val closeButton = view.findViewById<Button>(R.id.cancelButton)

        // Set up button listeners
        playButton.setOnClickListener {
            Toast.makeText(context, "Play ${verse.arabicText}", Toast.LENGTH_SHORT).show()
            // Handle play logic
        }

        bookmarkButton.setOnClickListener {
            val bookmark : Bookmark = Bookmark(verse.ayaID, verse.suraID, verse.ayaNo)
            addVerseToBookmarks(bookmark) // Add to bookmarks when clicked
            Toast.makeText(context, "Bookmarked ${verse.arabicText}", Toast.LENGTH_SHORT).show()
            // Handle bookmark logic
        }

        copyButton.setOnClickListener {
            Toast.makeText(context, "Copied ${verse.arabicText}", Toast.LENGTH_SHORT).show()
            // Handle copy logic
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun addVerseToBookmarks(bookmark: Bookmark) {
        sqLiteHelper.insertBookmark(bookmark)
    }

    companion object {
        const val TAG = "VerseBottomSheetFragment"
    }
}
