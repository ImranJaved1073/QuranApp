package com.example.quran.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.VerseActivity
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.databinding.ItemBookmarkBinding
import com.example.quran.models.Bookmark

class BookmarkAdapter(
    val bookmarkList: MutableList<Bookmark>,
    private val onDeleteClicked: (Bookmark) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(private val binding: ItemBookmarkBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bookmark: Bookmark) {
            val dbHelper = SQLiteHelper(this.itemView.context)
            val surah = bookmark.suraID?.let { dbHelper.getSurah(it) }
            binding.ayatNoBookmark.text = "آیت نمبر"+bookmark.ayaNo.toString()
            binding.suraNameBookmark.text = bookmark.suraID.toString()

            // Set delete button click listener
            binding.deleteBookmarkButton.setOnClickListener {
                onDeleteClicked(bookmark)
            }

            binding.root.setOnClickListener {
                val intent = Intent(this.itemView.context, VerseActivity::class.java).apply {
                    putExtra("SURAH_NUMBER", bookmark.suraID) // Surah Number
                    putExtra("AYAH_NUMBER", bookmark.ayaNo) // Ayah Number
                    putExtra("SURAH_NAME", surah?.englishNameTranslation)
                    putExtra("SURAH_ARABIC_NAME", surah?.name)
                    putExtra("REVELAION_TYPE", surah?.revelationType)
                }
                this.itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val binding = ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(bookmarkList[position])
    }

    override fun getItemCount(): Int = bookmarkList.size
}
