package com.example.quran.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quran.adapters.BookmarkAdapter
import com.example.quran.dataAccess.SQLiteHelper
import com.example.quran.databinding.FragmentBookmarkBinding
import com.example.quran.models.Bookmark

class BookMarkFragment : Fragment() {

    private lateinit var binding: FragmentBookmarkBinding
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private lateinit var dbHelper: SQLiteHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)

        // Initialize the SQLiteHelper
        dbHelper = SQLiteHelper(requireContext())

        // Retrieve all bookmarks from the database
        val bookmarks: List<Bookmark> = dbHelper.getAllBookmarks()

        if (bookmarks.isEmpty()) {
            binding.tvNoBookmarks.visibility = View.VISIBLE // Show "No bookmarks" message
            binding.bookMarkAyatRV.visibility = View.GONE // Hide the RecyclerView
        } else {
            binding.tvNoBookmarks.visibility = View.GONE // Hide "No bookmarks" message
            binding.bookMarkAyatRV.visibility = View.VISIBLE // Show the RecyclerView

            // Initialize the RecyclerView and Adapter
            bookmarkAdapter = BookmarkAdapter(bookmarks.toMutableList()) { bookmark ->
                showDeleteConfirmationDialog(bookmark)
            }

            binding.bookMarkAyatRV.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = bookmarkAdapter
            }
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
            if (bookmarkAdapter.bookmarkList.isEmpty()) {
                binding.tvNoBookmarks.visibility = View.VISIBLE
                binding.bookMarkAyatRV.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog(bookmark: Bookmark) {
        // Create an AlertDialog to confirm deletion
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Delete Bookmark")
        dialogBuilder.setMessage("Are you sure you want to delete this bookmark?")

        dialogBuilder.setPositiveButton("Yes") { _, _ ->
            deleteBookmark(bookmark) // Delete the bookmark if user confirms
        }

        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss() // Dismiss the dialog if user cancels
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dbHelper.close()
    }
}
