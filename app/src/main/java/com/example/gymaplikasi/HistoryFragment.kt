package com.example.gymaplikasi

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.data.GymLog
import com.example.gymaplikasi.domain.Muscle
import com.example.gymaplikasi.domain.exerciseToMuscleMap
import com.example.gymaplikasi.repository.GymRepository
import com.example.gymaplikasi.viewmodel.SyncViewModel
import com.example.gymaplikasi.viewmodel.SyncViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// === IMPORT KHUSUS KALENDER ===
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

class HistoryFragment : Fragment(R.layout.fragment_history){

    private lateinit var chart: LineChart
    private lateinit var spinnerChart: Spinner
    private lateinit var spinnerMuscle: Spinner
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var tvChartMax: TextView
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    private lateinit var syncViewModel: SyncViewModel

    // Variabel Kalender
    private lateinit var calendarView: CalendarView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: TextView
    private lateinit var btnNextMonth: TextView
    private lateinit var tvListHeaderTitle: TextView

    // State Kalender (Hari ini sebagai default)
    private var selectedDate: LocalDate = LocalDate.now()
    private val today = LocalDate.now()

    private var allLogsForList: List<GymLog> = emptyList()
    private var currentMuscleFilter: String = "All Muscles"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = GymRepository(db.gymLogDao(), FirebaseFirestore.getInstance())
        val factory = SyncViewModelFactory(repository)
        syncViewModel = ViewModelProvider(this, factory)[SyncViewModel::class.java]

        chart = view.findViewById(R.id.chartHistory)
        spinnerChart = view.findViewById(R.id.spinnerFilterHistory)
        spinnerMuscle = view.findViewById(R.id.spinnerFilterMuscle)
        rvHistory = view.findViewById(R.id.rvHistory)
        tvChartMax = view.findViewById(R.id.tvChartMax)

        // Init View Kalender
        calendarView = view.findViewById(R.id.calendarView)
        tvMonthYear = view.findViewById(R.id.tvMonthYear)
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById(R.id.btnNextMonth)
        tvListHeaderTitle = view.findViewById(R.id.tvListHeaderTitle)

        setupRecyclerView()
        setupChartDesign()
        setupSwipeToDelete()

        loadSpinnerData()
        setupMuscleSpinner()

        setupCalendar()
    }

    // LOGIKA KALENDER KIZITONWOSE
    private fun setupCalendar() {
        // Definisikan Kelas Pembungkus Hari
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.tvDayText)
            val cardView =
                view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardDay)
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        // Refresh UI untuk tanggal lama dan tanggal baru yang diklik
                        calendarView.notifyDateChanged(oldDate)
                        calendarView.notifyDateChanged(selectedDate)

                        // Tarik data dari database untuk tanggal ini
                        loadLogsForSelectedDate()
                    }
                }
            }
        }

        //Terapkan logika UI ke DayBinder
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                container.textView.text = day.date.dayOfMonth.toString()

                if (day.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    when (day.date) {
                        selectedDate -> {
                            container.cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gym_primary))
                            container.textView.setTextColor(Color.BLACK)
                        }
                        today -> {
                            container.cardView.setCardBackgroundColor(Color.parseColor("#333333"))
                            container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gym_primary))
                        }
                        else -> {
                            container.cardView.setCardBackgroundColor(Color.TRANSPARENT)
                            container.textView.setTextColor(Color.WHITE)
                        }
                    }
                } else {
                    // Sembunyikan tanggal dari bulan sebelumnya/selanjutnya agar bersih
                    container.textView.visibility = View.INVISIBLE
                }
            }
        }

        // Update Judul Bulan saat di-scroll
        calendarView.monthScrollListener = object : MonthScrollListener {
            override fun invoke(month: com.kizitonwose.calendar.core.CalendarMonth) {
                val titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
                tvMonthYear.text = titleFormatter.format(month.yearMonth)
            }
        }

        // Inisialisasi Rentang Kalender
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12) // Mundur 1 tahun
        val endMonth = currentMonth.plusMonths(12)    // Maju 1 tahun
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // Tombol Navigasi Bulan
        btnNextMonth.setOnClickListener {
            val nextMonth = calendarView.findFirstVisibleMonth()?.yearMonth?.plusMonths(1)
            nextMonth?.let { calendarView.smoothScrollToMonth(it) }
        }
        btnPrevMonth.setOnClickListener {
            val prevMonth = calendarView.findFirstVisibleMonth()?.yearMonth?.minusMonths(1)
            prevMonth?.let { calendarView.smoothScrollToMonth(it) }
        }

        // Tarik data untuk hari ini saat pertama kali dibuka
        loadLogsForSelectedDate()
    }

    // LOGIKA TARIK DATA BERDASARKAN TANGGAL KALENDER
    private fun loadLogsForSelectedDate() {
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        tvListHeaderTitle.text = "Latihan: ${dateFormatter.format(selectedDate)}"

        // Ubah localDate jadi Milisecond untuk room DB
        val zoneId = ZoneId.systemDefault()
        val startOfDay = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        lifecycleScope.launch {
            db.gymLogDao().getLogsByDateRange(myUserId, startOfDay, endOfDay).collect { logs ->
                allLogsForList = logs
                applyListFilter() // Terapkan ulang filter otot (jika ada)
            }
        }
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
                    .setTitle("Hapus Workout?")
                    .setMessage("Apakah yakin ingin hapus ${logToDelete.exercise} (${logToDelete.weight}kg)?")
                    .setPositiveButton("Hapus") { _, _ ->
                        val myUserId = FirebaseAuth.getInstance().currentUser?.uid

                        // hapus di cloud
                        if (myUserId != null) {
                            syncViewModel.triggerDeleteSync(myUserId, logToDelete.id)
                        }

                        // hapus di room
                        lifecycleScope.launch {
                            db.gymLogDao().deleteGymLog(logToDelete)
                        }
                    }
                    .setNegativeButton("Batal") { dialog, _ ->
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
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            db.gymLogDao().getUniqueExerciseNames(userId = myUserId).collect { names ->
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
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        lifecycleScope.launch {
            db.gymLogDao().getLogsByExercise(userId = myUserId, exerciseName = exerciseName).collect { logs ->
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