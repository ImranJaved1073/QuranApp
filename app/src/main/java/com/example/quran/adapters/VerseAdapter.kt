package com.example.quran.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.R
import com.example.quran.models.Verse

class VerseAdapter(
    private val verses: List<Verse>,
    private val isArabicEnabled: Boolean,
    private val isTranslationEnabled: Boolean,
    private val arabicFontSize: Int,
    private val translationFontSize: Int,
    private val isEnglishTranslationEnabled: Boolean,
    private val englishTranslationFontSize: Int,
    private val urduTranslationSelected: Int,
    private val englishTranslationSelected: Int,
    private val onItemClickListener: (Verse) -> Unit // Add a click listener
) : RecyclerView.Adapter<VerseAdapter.AyatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ayat, parent, false)
        return AyatViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyatViewHolder, position: Int) {
        val verse = verses[position]

        val englishTranslations = arrayOf(verse.drMohsinKhan, verse.muftiTaqiUsmani)
        val urduTranslations = arrayOf(verse.fatehMuhammadJalandhrield, verse.mehmoodulHassan)

        // Toggle Arabic visibility and set Arabic text and font size
        holder.arabicTextView.visibility = if (isArabicEnabled) View.VISIBLE else View.GONE
        holder.arabicTextView.text = verse.arabicText
        holder.arabicTextView.textSize = arabicFontSize.toFloat()

        // Toggle Translation visibility and set translation text and font size
        holder.translationTextView.visibility = if (isTranslationEnabled) View.VISIBLE else View.GONE
        holder.translationTextView.text = urduTranslations[urduTranslationSelected]
        holder.translationTextView.textSize = translationFontSize.toFloat()

        // Toggle English translation visibility and set text and font size
        holder.englishTextView.visibility = if (isEnglishTranslationEnabled) View.VISIBLE else View.GONE
        holder.englishTextView.text = englishTranslations[englishTranslationSelected]
        holder.englishTextView.textSize = englishTranslationFontSize.toFloat()

        // Set Ayah number label
        holder.ayahNumberLabel.text = "${verse.suraID} : ${verse.ayaNo}"
        holder.ayatDesc.text = " آیت نمبر${verse.ayaNo} پماره ${verse.paraID} يمارة ركوع ${verse.pRakuID} سورة ركوع ${verse.rakuID}"

        // Handle item click to open the bottom sheet
        holder.itemView.setOnClickListener {
            onItemClickListener(verse) // Trigger the click listener
        }
    }

    override fun getItemCount(): Int = verses.size

    // Helper function to scroll to a specific ayah number
    fun scrollToAyahNumber(ayahNumber: Int) {
        val position = verses.indexOfFirst { it.ayaNo == ayahNumber }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    class AyatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val arabicTextView: TextView = itemView.findViewById(R.id.ayahArabicText)
        val translationTextView: TextView = itemView.findViewById(R.id.ayahUrduTranslation)
        val englishTextView: TextView = itemView.findViewById(R.id.ayahEnglishTranslation)
        val ayahNumberLabel: TextView = itemView.findViewById(R.id.ayahNumberLabel)
        val ayatDesc: TextView = itemView.findViewById(R.id.ayatDescription)
    }
}
