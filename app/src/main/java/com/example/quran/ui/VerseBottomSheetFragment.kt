package com.example.quran.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.quran.R
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.models.Verse
import com.example.quran.models.Bookmark
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VerseBottomSheetFragment(private val verse: Verse) : BottomSheetDialogFragment() {

    private lateinit var sqLiteHelper: SQLiteHelper
    private var mediaPlayer: MediaPlayer? = null

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


        bookmarkButton.setOnClickListener {
            val bookmark = Bookmark(verse.ayaID, verse.suraID, verse.ayaNo)
            addVerseToBookmarks(bookmark) // Add to bookmarks when clicked
            Toast.makeText(context, "Bookmarked Added", Toast.LENGTH_SHORT).show()
            // Handle bookmark logic
        }

        copyButton.setOnClickListener {
            val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val isArabicEnabled = preferences.getBoolean("arabic_enabled", true)
            val isTranslationEnabled = preferences.getBoolean("translation_enabled", true)
            val isEnglishTranslationEnabled = preferences.getBoolean("english_translation_enabled", true)
            val builder = StringBuilder()
            if (isArabicEnabled) {
                builder.append(verse.arabicText).append('\n')
            }
            if (isTranslationEnabled) {
                builder.append(verse.fatehMuhammadJalandhrield).append('\n')
            }
            if (isEnglishTranslationEnabled) {
                builder.append(verse.drMohsinKhan).append('\n')
            }
            val shareText = builder.toString().trim()
            copyToClipboard(shareText)
            Toast.makeText(context, "Copy to clipboard", Toast.LENGTH_SHORT).show()
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

//    private fun playAyatAudio(surahId: Int, ayatId: Int) {
//        // Construct the audio URL
//        val audioUrl = "https://everyayah.com/data/AbdulSamad_64kbps_QuranExplorer.Com/${"%03d".format(surahId)}${"%03d".format(ayatId)}.mp3"
//        if (mediaPlayer == null) {
//            mediaPlayer = MediaPlayer().apply {
//                setDataSource(audioUrl)
//                prepare()
//                start()
//            }
//        } else {
//            mediaPlayer?.stop()
//            mediaPlayer?.reset()
//            mediaPlayer?.setDataSource(audioUrl)
//            mediaPlayer?.prepare()
//            mediaPlayer?.start()
//        }
//
//        Toast.makeText(context, "Playing Ayat Audio", Toast.LENGTH_SHORT).show()
//    }

    private fun addVerseToBookmarks(bookmark: Bookmark) {
        sqLiteHelper.insertBookmark(bookmark)
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }


    companion object {
        const val TAG = "VerseBottomSheetFragment"
    }
}
