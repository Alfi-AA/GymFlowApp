package com.example.gymaplikasi

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.data.GymLog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history){

    private lateinit var chart: LineChart
    private lateinit var spinner: Spinner
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var tvChartMax: TextView
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chart = view.findViewById(R.id.chartHistory)
        spinner = view.findViewById(R.id.spinnerFilterHistory)
        rvHistory = view.findViewById(R.id.rvHistory)
        tvChartMax = view.findViewById(R.id.tvChartMax)

        setupRecyclerView()
        setupChartDesign()

        loadSpinnerData()
        loadHistoryList()
    }

    // Menyiapkan RecyclerView untuk daftar riwayat
    private fun setupRecyclerView() {
        adapter = HistoryAdapter(emptyList())
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter
    }

    // Mengambil semua data log latihan dari database (terbaru di atas)
    private fun loadHistoryList() {
        lifecycleScope.launch {
            db.gymLogDao().getAllLogs().collect { logs ->
                adapter.updateData(logs)
            }
        }
    }

    // Mengambil nama latihan unik untuk opsi filter di Spinner
    private fun loadSpinnerData() {
        lifecycleScope.launch {
            db.gymLogDao().getUniqueExerciseNames().collect { names ->
                if (names.isNotEmpty()) {
                    setupSpinnerAdapter(names)
                }
            }
        }
    }

    private fun setupSpinnerAdapter(exercises: List<String>) {
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, exercises)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinner.adapter = adapter

        // Update grafik saat opsi latihan dipilih
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadChartData(exercises[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Mengambil data spesifik latihan untuk ditampilkan di grafik
    private fun loadChartData(exerciseName: String) {
        lifecycleScope.launch {
            db.gymLogDao().getLogsByExercise(exerciseName).collect { logs ->
                updateChart(logs)
            }
        }
    }

    // Menggambar data ke dalam LineChart
    private fun updateChart(logs: List<GymLog>) {
        if (logs.isEmpty()) {
            chart.clear()
            tvChartMax.text = "Max: 0 kg"
            return
        }

        val entries = ArrayList<Entry>()
        val dates = ArrayList<String>()
        var maxWeight = 0f

        logs.forEachIndexed { index, log ->
            entries.add(Entry(index.toFloat(), log.weight.toFloat()))
            val dateStr = SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(log.date))
            dates.add(dateStr)
            if (log.weight > maxWeight) maxWeight = log.weight.toFloat()
        }

        tvChartMax.text = "Max: ${maxWeight.toInt()} kg"

        // Konfigurasi tampilan garis grafik
        val dataSet = LineDataSet(entries, "Beban (kg)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.gym_primary)
            valueTextColor = Color.WHITE
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.gym_primary))
            lineWidth = 3f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.gym_primary)
            fillAlpha = 50
        }

        chart.data = LineData(dataSet)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        chart.invalidate()
        chart.animateY(1000)
    }

    private fun setupChartDesign() {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.parseColor("#888888")
            axisLeft.textColor = Color.WHITE
            axisLeft.gridColor = Color.parseColor("#333333")
        }
    }
}