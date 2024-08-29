package com.example.quran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VerseAdapter(
    private val arabicAyats: List<Verse>,
    private val englishAyats: List<Verse>,
    private val urduAyats: List<Verse>
) : RecyclerView.Adapter<VerseAdapter.AyatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ayat, parent, false)
        return AyatViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyatViewHolder, position: Int) {
        val arabicAyat = arabicAyats[position]
        val englishAyat = englishAyats[position]
        val urduAyat = urduAyats[position]

        holder.bind(arabicAyat, englishAyat, urduAyat)
    }

    override fun getItemCount(): Int = arabicAyats.size

    class AyatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(arabicAyat: Verse, englishAyat: Verse, urduAyat: Verse) {
            itemView.findViewById<TextView>(R.id.ayahNumber).text = arabicAyat.numberInSurah.toString()
            itemView.findViewById<TextView>(R.id.ayahArabicText).text = arabicAyat.text
            itemView.findViewById<TextView>(R.id.ayahEnglishTranslation).text = englishAyat.text
            itemView.findViewById<TextView>(R.id.ayahUrduTranslation).text = urduAyat.text
        }
    }
}
