package com.example.quran.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.adapters.BookmarkAdapter
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.databinding.FragmentFavouritesBinding
import com.example.quran.models.Bookmark

class FavouritesFragment : Fragment() {

    private lateinit var binding: FragmentFavouritesBinding
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var dbHelper: SQLiteHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)

        // Initialize the SQLiteHelper
        dbHelper = SQLiteHelper(requireContext())

        // Retrieve all bookmarks from the database
        val bookmarks: List<Bookmark> = dbHelper.getAllBookmarks()

        // Initialize the RecyclerView and Adapter
        bookmarkAdapter = BookmarkAdapter(bookmarks.toMutableList()) { bookmark ->
            deleteBookmark(bookmark)
        }

        binding.bookMarkAyatRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookmarkAdapter
        }

        return binding.root
    }

    private fun deleteBookmark(bookmark: Bookmark) {
        // Delete bookmark from the database
        val success = bookmark.ayaID?.let { dbHelper.deleteBookmark(it) }
        if (success == true) {
            // Remove the deleted bookmark from the list
            bookmarkAdapter.bookmarkList.remove(bookmark)
            bookmarkAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close() // Close the database connection when the fragment is destroyed
    }
}
