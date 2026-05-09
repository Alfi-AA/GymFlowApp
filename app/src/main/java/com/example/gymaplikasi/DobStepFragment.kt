package com.example.gymaplikasi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.viewmodel.OnboardingViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DobStepFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var etDob: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dob_step, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewmodel
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]
        etDob = view.findViewById(R.id.etDob)

        val savedDob = viewModel.dob.value
        if (savedDob > 0L) {
            updateDateText(savedDob)
        }

        //untuk memunculkan kalender
        etDob.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        //popup kalender material design
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal Lahir")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.setDob(selection)
            updateDateText(selection)
        }

        datePicker.show(parentFragmentManager, "DOB_PICKER")
    }

    private fun updateDateText(timeInMillis: Long) {
        // Format: 25 Apr 2026
        val format = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateString = format.format(Date(timeInMillis))
        etDob.setText(dateString)
    }
}