package com.example.gymaplikasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymaplikasi.data.GymLog
import com.example.gymaplikasi.domain.exerciseToMuscleMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private var logList: List<GymLog>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){

    // Format Tanggal: "29" (Hari)
    private val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    // Format Bulan: "OKT" (Singkatan Bulan)
    private val monthFormat = SimpleDateFormat("MMM", Locale.forLanguageTag("id-ID")) // "id" untuk Indonesia

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvMonth: TextView = view.findViewById(R.id.tvMonth)
        val tvExerciseName: TextView = view.findViewById(R.id.tvExerciseName)
        val tvWeight: TextView = view.findViewById(R.id.tvWeight)
        val tvReps: TextView = view.findViewById(R.id.tvReps)
        val tvMuscleGroup: TextView = view.findViewById(R.id.tvSubDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val log = logList[position]

        // Konversi Timestamp ke Tanggal
        val date = Date(log.date)
        holder.tvDate.text = dayFormat.format(date)
        holder.tvMonth.text = monthFormat.format(date).uppercase() // "OKT"

        // Set Data Lainnya
        holder.tvExerciseName.text = log.exercise

        // ambil data otot dari Map berdasarkan nama latihan
        val targetData = exerciseToMuscleMap[log.exercise]
        if (targetData != null) {
            val muscleName = targetData.muscle.name.lowercase().replaceFirstChar { it.uppercase() }
            holder.tvMuscleGroup.text = "$muscleName Group"
        } else {
            // kalo ga ditemuin di Map
            holder.tvMuscleGroup.text = "General"
        }
        // Format angka (hapus .0 jika bulat)
        val weightText = if (log.weight % 1f == 0f) log.weight.toInt().toString() else log.weight.toString()
        holder.tvWeight.text = "$weightText kg"

        holder.tvReps.text = "x ${log.reps} reps"
    }

    override fun getItemCount(): Int = logList.size

    fun getItem(position: Int): GymLog = logList[position]

    // Fungsi untuk update data dari Fragment/Database
    fun updateData(newLogs: List<GymLog>) {
        logList = newLogs
        notifyDataSetChanged()
    }
}