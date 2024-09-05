package com.example.quran.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quran.models.Para
import com.example.quran.R

class ParaAdapter(
    private var paras: List<Para>,
    private val onParaClick: (Int) -> Unit
) : RecyclerView.Adapter<ParaAdapter.ParaViewHolder>() {

    private var fullParaList = paras

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.para_list_layout, parent, false)
        return ParaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParaViewHolder, position: Int) {
        val para = paras[position]
        holder.bind(para)
        holder.itemView.setOnClickListener {
            onParaClick(para.paraID)
        }
    }

    override fun getItemCount(): Int = paras.size

    fun updateParas(newPasras: List<Para>) {
        paras = newPasras
        fullParaList = newPasras
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filteredList = fullParaList.filter {
            it.paraID.toString().contains(query, ignoreCase = true)
        }
        paras = filteredList
        notifyDataSetChanged()
    }

    class ParaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(para: Para) {
            itemView.findViewById<TextView>(R.id.paraNumber).text = para.paraID.toString()
            itemView.findViewById<TextView>(R.id.paraArabicName).text = para.arabicName
            itemView.findViewById<TextView>(R.id.paraEnglishName).text = para.englishName
            itemView.findViewById<TextView>(R.id.paraVerses).text = "${para.totalAyas} VERSES"
        }
    }
}
