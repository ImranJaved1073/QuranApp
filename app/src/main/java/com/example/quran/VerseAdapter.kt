package com.example.quran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VerseAdapter(
    private val arabicAyats: List<Verse>
) : RecyclerView.Adapter<VerseAdapter.AyatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ayat, parent, false)
        return AyatViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyatViewHolder, position: Int) {
        val arabicAyat = arabicAyats[position]

        holder.bind(arabicAyat)
    }

    override fun getItemCount(): Int = arabicAyats.size

    fun scrollToAyahNumber(ayahNumber: Int) {
        // This method should be called after the adapter is set to RecyclerView
        val position = arabicAyats.indexOfFirst { it.ayaNo == ayahNumber }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    class AyatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(arabicAyat: Verse) {
            itemView.findViewById<TextView>(R.id.ayahArabicText).text = arabicAyat.arabicText
            itemView.findViewById<TextView>(R.id.ayahEnglishTranslation).text = arabicAyat.drMohsinKhan
            itemView.findViewById<TextView>(R.id.ayahUrduTranslation).text = arabicAyat.fatehMuhammadJalandhrield
        }
    }
}
