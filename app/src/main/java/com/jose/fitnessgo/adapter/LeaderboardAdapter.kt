package com.jose.fitnessgo.adapter

import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.jose.fitnessgo.LeaderboardEntry
import com.jose.fitnessgo.R

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    private val entriesList = mutableListOf<LeaderboardEntry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_leaderboard, parent, false)
        return ViewHolder(view)
    }

    fun addItem(entry: LeaderboardEntry) {
        val size = entriesList.size
        entriesList.add(entry)
        notifyItemInserted(size)
    }

    fun addAll(entries: List<LeaderboardEntry>) {
        val size = entriesList.size
        entriesList += entries
        notifyItemRangeInserted(size, entries.size)
    }

    fun deleteRow(position: Int) {
        entriesList.removeAt(position)
        notifyItemRemoved(position)
    }



    override fun getItemCount() = entriesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entriesList[position]
        holder.entry = entry

        holder.tvEntryName.text = entry.name
        holder.tvEntryPoints.text = entry.points.toString()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEntryName: TextView = itemView.findViewById(R.id.tvEntryName)
        val tvEntryPoints: TextView = itemView.findViewById(R.id.tvEntryPoints)

        var entry: LeaderboardEntry? = null

        init {
            itemView.setOnClickListener {
                Snackbar.make(
                        itemView.rootView,
                        "${entry?.name} has ${entry?.points} points. Try to beat them! ",
                        Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }


}
