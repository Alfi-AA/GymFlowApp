package com.example.gymaplikasi

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.data.GymLog
import com.example.gymaplikasi.domain.Muscle
import com.example.gymaplikasi.domain.exerciseToMuscleMap
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history){

    private lateinit var chart: LineChart
    private lateinit var spinnerChart: Spinner
    private lateinit var spinnerMuscle: Spinner
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var tvChartMax: TextView
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private var allLogsForList: List<GymLog> = emptyList()
    private var currentMuscleFilter: String = "All Muscles"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chart = view.findViewById(R.id.chartHistory)
        spinnerChart = view.findViewById(R.id.spinnerFilterHistory)
        spinnerMuscle = view.findViewById(R.id.spinnerFilterMuscle)
        rvHistory = view.findViewById(R.id.rvHistory)
        tvChartMax = view.findViewById(R.id.tvChartMax)

        setupRecyclerView()
        setupChartDesign()
        setupSwipeToDelete()

        loadSpinnerData()
        setupMuscleSpinner()
        loadHistoryList()
    }

    // Menyiapkan RecyclerView untuk daftar riwayat
    private fun setupRecyclerView() {
        adapter = HistoryAdapter(emptyList())
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter
    }

    // Swipe to delete
    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            // ini untuk kalo digeser penuh
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val logToDelete = adapter.getItem(position)

                // pop up konfirmasi
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Workout?")
                    .setMessage("Are you sure you want to delete ${logToDelete.exercise} (${logToDelete.weight}kg)?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            db.gymLogDao().deleteGymLog(logToDelete)
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        // BOUNCE BACK (Kembali ke semula)
                        adapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    .setOnCancelListener {
                        // Jika user klik di luar kotak, bounce back juga
                        adapter.notifyItemChanged(position)
                    }
                    .show()
            }

            // ganti merah saat digeser
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView

                    val swipeRatio = (Math.abs(dX) / itemView.width).coerceIn(0f, 1f)

                    val defaultColor = ContextCompat.getColor(requireContext(), R.color.gym_surface)
                    val swipeColor = android.graphics.Color.parseColor("#FF3232")

                    val blendedColor = androidx.core.graphics.ColorUtils.blendARGB(defaultColor, swipeColor, swipeRatio)

                    if (itemView is com.google.android.material.card.MaterialCardView) {
                        itemView.setCardBackgroundColor(blendedColor)
                    } else {
                        itemView.background?.mutate()?.setTint(blendedColor)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                val defaultColor = ContextCompat.getColor(requireContext(), R.color.gym_surface)
                if (viewHolder.itemView is com.google.android.material.card.MaterialCardView) {
                    (viewHolder.itemView as com.google.android.material.card.MaterialCardView).setCardBackgroundColor(defaultColor)
                } else {
                    viewHolder.itemView.background?.mutate()?.setTint(defaultColor)
                    viewHolder.itemView.background?.mutate()?.clearColorFilter()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvHistory)
    }


    // Mengambil data 7 hari latihan ke belakang
    private fun loadHistoryList() {
        lifecycleScope.launch {
            db.gymLogDao().getAllLogs().collect { logs ->
                val sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000
                val sevenDaysAgo = System.currentTimeMillis() - sevenDaysInMillis

                allLogsForList = logs.filter { it.date >= sevenDaysAgo }

                applyListFilter()
            }
        }
    }

    private fun setupMuscleSpinner() {
        val muscleOptions = mutableListOf("All Muscles")
        muscleOptions.addAll(Muscle.entries.map { it.name })

        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_inline_selected, muscleOptions)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinnerMuscle.adapter = adapter

        spinnerMuscle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentMuscleFilter = muscleOptions[position]
                applyListFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun applyListFilter() {
        if (currentMuscleFilter == "All Muscles") {
            adapter.updateData(allLogsForList)
        } else {
            val filteredList = allLogsForList.filter { log ->
                val targetData = exerciseToMuscleMap[log.exercise]
                targetData?.muscle?.name == currentMuscleFilter
            }
            adapter.updateData(filteredList)
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
        spinnerChart.adapter = adapter

        // Update grafik saat opsi latihan dipilih
        spinnerChart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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