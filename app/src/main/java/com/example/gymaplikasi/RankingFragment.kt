package com.example.gymaplikasi


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat // TAMBAHAN PENTING 1
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.databinding.FragmentRankingBinding
import com.example.gymaplikasi.domain.Muscle
import com.example.gymaplikasi.viewmodel.RankingViewModel
import com.example.gymaplikasi.viewmodel.RankingViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class RankingFragment : Fragment() {
    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RankingViewModel
    private var currentCategory = "Upper Body"

    private lateinit var muscleAdapter: MuscleProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModel (Pakai Factory karena butuh DAO)
        val dao = AppDatabase.getDatabase(requireContext()).gymLogDao()
        val factory = RankingViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[RankingViewModel::class.java]

        // Setup Tampilan Awal Grafik
        setupRadarChart()

        // Klik Dropdown untuk ganti kategori
        binding.tvSelectedCategory.parent.let { layout ->
            (layout as View).setOnClickListener { showCategoryDropdown(layout) }
        }

        // Observasi Data dari ViewModel
        observeData()
        val userPreferences = com.example.gymaplikasi.utils.UserPreferences(requireContext())
        val userGender = userPreferences.getGender() ?: "Male"

        // Muscle heatmap sesuai gender
        setupHeatmapGender(userGender)

        // Jalankan Kalkulasi
        viewModel.calculateScores(userGender)

        // list ranking
        setupRecyclerView()
    }

    private fun setupRadarChart() {
        binding.radarChart.apply {
            description.isEnabled = false
            webLineWidth = 1.5f
            webColor = Color.parseColor("#444444")
            webLineWidthInner = 1f
            webColorInner = Color.parseColor("#444444")
            webAlpha = 150

            // Sumbu Y (Skala 0 - 100)
            yAxis.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawLabels(false)
                labelCount = 5
            }

            // Sumbu X (Label Otot)
            xAxis.apply {
                textSize = 10f
                textColor = Color.WHITE
                position = XAxis.XAxisPosition.BOTTOM
            }

            legend.isEnabled = false
        }
    }

    private fun setupHeatmapGender(gender: String) {
        if (gender == "Female" || gender == "Wanita") {
            binding.ivBodyBaseFront.setImageResource(R.drawable.ic_body_front_female_base)
            binding.ivBodyBaseBack.setImageResource(R.drawable.ic_body_back_female_base)

            // ganti otot depan
            binding.ivAbsMuscle.setImageResource(R.drawable.ic_muscle_front_female_abs)
            binding.ivBicepsMuscle.setImageResource(R.drawable.ic_muscle_front_female_biceps)
            binding.ivCalvesMuscleFront.setImageResource(R.drawable.ic_muscle_front_female_calves)
            binding.ivChestMuscle.setImageResource(R.drawable.ic_muscle_front_female_chest)
            binding.ivQuadsMuscleFront.setImageResource(R.drawable.ic_muscle_front_female_quads)
            binding.ivShouldersMuscleFront.setImageResource(R.drawable.ic_muscle_front_female_shoulders)
            binding.ivTricepsMuscleFront.setImageResource(R.drawable.ic_muscle_front_female_triceps)

            // ganti otot belakang
            binding.ivBackMuscle.setImageResource(R.drawable.ic_muscle_back_female_back)
            binding.ivShouldersMuscleBack.setImageResource(R.drawable.ic_muscle_back_female_shoulders)
            binding.ivTricepsMuscleBack.setImageResource(R.drawable.ic_muscle_back_female_triceps)
            binding.ivQuadsMuscleBack.setImageResource(R.drawable.ic_muscle_back_female_quads)
            binding.ivGlutesMuscle.setImageResource(R.drawable.ic_muscle_back_female_glutes)
            binding.ivHamstringMuscle.setImageResource(R.drawable.ic_muscle_back_female_hamstring)
            binding.ivCalvesMuscleBack.setImageResource(R.drawable.ic_muscle_back_female_calves)
        }
    }

    // LOGIKA UNTUK MUSCLE HEATMAP
    private fun getColorForScore(score: Int): Int {
        val colorRes = when (score) {
            in 100..Int.MAX_VALUE -> R.color.rank_mythril // 100%
            in 88..99 -> R.color.rank_adamantium
            in 75..87 -> R.color.rank_titanium
            in 62..74 -> R.color.rank_diamond
            in 50..61 -> R.color.rank_platinum // RATA-RATA
            in 38..49 -> R.color.rank_gold
            in 25..37 -> R.color.rank_silver
            in 12..24 -> R.color.rank_bronze
            in 1..11 -> R.color.rank_iron // Pangkat Terendah
            else -> R.color.rank_unranked // 0
        }
        return ContextCompat.getColor(requireContext(), colorRes)
    }

    private fun applyColorToMuscle(muscle: Muscle, color: Int) {
        when (muscle) {
            Muscle.CHEST -> binding.ivChestMuscle.setColorFilter(color)
            Muscle.ABS -> binding.ivAbsMuscle.setColorFilter(color)
            Muscle.BICEP -> binding.ivBicepsMuscle.setColorFilter(color)
            Muscle.BACK -> binding.ivBackMuscle.setColorFilter(color)
            Muscle.HAMSTRINGS -> binding.ivHamstringMuscle.setColorFilter(color)
            Muscle.GLUTES -> binding.ivGlutesMuscle.setColorFilter(color)

            Muscle.TRICEP -> {
                binding.ivTricepsMuscleFront.setColorFilter(color)
                binding.ivTricepsMuscleBack.setColorFilter(color)
            }
            Muscle.SHOULDERS -> {
                binding.ivShouldersMuscleFront.setColorFilter(color)
                binding.ivShouldersMuscleBack.setColorFilter(color)
            }
            Muscle.QUADS -> {
                binding.ivQuadsMuscleFront.setColorFilter(color)
                binding.ivQuadsMuscleBack.setColorFilter(color)
            }
            Muscle.CALVES -> {
                binding.ivCalvesMuscleFront.setColorFilter(color)
                binding.ivCalvesMuscleBack.setColorFilter(color)
            }
        }
    }

    // Logika untuk list ranking
    private fun getIconForScore(score: Int): Int {
        return when (score) {
            in 100..Int.MAX_VALUE -> R.drawable.rank_mythril
            in 88..99 -> R.drawable.rank_adamantium
            in 75..87 -> R.drawable.rank_titanium
            in 62..74 -> R.drawable.rank_diamond
            in 50..61 -> R.drawable.rank_platinum
            in 38..49 -> R.drawable.rank_gold
            in 25..37 -> R.drawable.rank_silver
            in 12..24 -> R.drawable.rank_bronze
            in 1..11 -> R.drawable.rank_iron
            else -> R.drawable.rank_iron // Jika 0 tetap kasih Iron tapi mungkin abu-abu
        }
    }

    private fun getNameForScore(score: Int): String {
        return when (score) {
            in 100..Int.MAX_VALUE -> "Mythril"
            in 88..99 -> "Adamantium"
            in 75..87 -> "Titanium"
            in 62..74 -> "Diamond"
            in 50..61 -> "Platinum"
            in 38..49 -> "Gold"
            in 25..37 -> "Silver"
            in 12..24 -> "Bronze"
            in 1..11 -> "Iron"
            else -> "Unranked"
        }
    }

    //FUNGSI SETUP RECYCLER VIEW
    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan memasukkan 3 fungsi translator
        muscleAdapter = MuscleProgressAdapter(
            getScoreColor = { score -> getColorForScore(score) },
            getRankIcon = { score -> getIconForScore(score) },
            getRankName = { score -> getNameForScore(score) }
        )

        // ini buat pasang adapter ke recyclerview
        binding.rvMuscleProgress.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = muscleAdapter
        }
    }

    private fun observeData() {
        // Pantau Upper Body
        viewModel.upperBodyScores.observe(viewLifecycleOwner) { scores ->
            // radar chart untuk upper body
            if (currentCategory == "Upper Body") updateChart(scores)

            // heatmap untuk upper body
            scores.forEach { (muscle, score) ->
                val muscleColor = getColorForScore(score)
                applyColorToMuscle(muscle, muscleColor)
            }
        }

        // Pantau Lower Body
        viewModel.lowerBodyScores.observe(viewLifecycleOwner) { scores ->
            // radar chart untuk lower body
            if (currentCategory == "Lower Body") updateChart(scores)

            // heatmap untuk lower body
            scores.forEach { (muscle, score) ->
                val muscleColor = getColorForScore(score)
                applyColorToMuscle(muscle, muscleColor)
            }
        }

        // Pantau data list rank
        viewModel.muscleProgressList.observe(viewLifecycleOwner) { progressList ->
            muscleAdapter.submitList(progressList)
        }
    }

    private fun updateChart(muscleScores: Map<Muscle, Int>) {
        val entries = ArrayList<RadarEntry>()
        val labels = ArrayList<String>()

        muscleScores.forEach { (muscle, score) ->
            entries.add(RadarEntry(score.toFloat()))
            labels.add(muscle.name)
        }

        val dataSet = RadarDataSet(entries, "Strength")
        dataSet.apply {
            color = Color.parseColor("#B1EE2E") // Lime Green
            fillColor = Color.parseColor("#B1EE2E")
            setDrawFilled(true)
            fillAlpha = 60
            lineWidth = 3f
            valueTextColor = Color.TRANSPARENT // Sembunyikan angka biar bersih
        }

        binding.radarChart.apply {
            data = RadarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            animateY(800)
            invalidate()
        }
    }

    private fun showCategoryDropdown(anchorView: View) {
        val categories = arrayOf("Upper Body", "Lower Body")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)

        val listPopupWindow = ListPopupWindow(requireContext())
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.anchorView = anchorView

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            currentCategory = categories[position]
            binding.tvSelectedCategory.text = currentCategory

            if (currentCategory == "Upper Body") {
                viewModel.upperBodyScores.value?.let { updateChart(it) }
            } else {
                viewModel.lowerBodyScores.value?.let { updateChart(it) }
            }

            listPopupWindow.dismiss()
        }

        listPopupWindow.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}