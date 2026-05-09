package com.example.gymaplikasi

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.viewmodel.OnboardingViewModel
import com.google.android.material.card.MaterialCardView

class GenderStepFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel

    private lateinit var cardMale: MaterialCardView
    private lateinit var tvMaleIcon: TextView
    private lateinit var tvMaleText: TextView

    private lateinit var cardFemale: MaterialCardView
    private lateinit var tvFemaleIcon: TextView
    private lateinit var tvFemaleText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gender_step, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewmodel
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]

        //UI
        cardMale = view.findViewById(R.id.cardMale)
        tvMaleIcon = view.findViewById(R.id.tvMaleIcon)
        tvMaleText = view.findViewById(R.id.tvMaleText)

        cardFemale = view.findViewById(R.id.cardFemale)
        tvFemaleIcon = view.findViewById(R.id.tvFemaleIcon)
        tvFemaleText = view.findViewById(R.id.tvFemaleText)

        cardMale.setOnClickListener {
            selectGender("Male")
        }

        cardFemale.setOnClickListener {
            selectGender("Female")
        }

        val savedGender = viewModel.gender.value
        if (savedGender.isNotEmpty()) {
            selectGender(savedGender)
        }
    }

    private fun selectGender(gender: String) {
        //simpan ke viewmodel
        viewModel.setGender(gender)

        val defaultBg = Color.parseColor("#2A2A2A")
        val defaultText = Color.WHITE

        val activeBg = ContextCompat.getColor(requireContext(), R.color.gym_primary)
        val activeText = Color.parseColor("#121212")

        //update UI
        if (gender == "Male") {
            cardMale.setCardBackgroundColor(activeBg)
            tvMaleIcon.setTextColor(activeText)
            tvMaleText.setTextColor(activeText)

            cardFemale.setCardBackgroundColor(defaultBg)
            tvFemaleIcon.setTextColor(defaultText)
            tvFemaleText.setTextColor(defaultText)
        } else if (gender == "Female") {
            cardFemale.setCardBackgroundColor(activeBg)
            tvFemaleIcon.setTextColor(activeText)
            tvFemaleText.setTextColor(activeText)

            cardMale.setCardBackgroundColor(defaultBg)
            tvMaleIcon.setTextColor(defaultText)
            tvMaleText.setTextColor(defaultText)
        }
    }
}