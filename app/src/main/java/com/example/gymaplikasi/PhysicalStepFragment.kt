package com.example.gymaplikasi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.viewmodel.OnboardingViewModel

class PhysicalStepFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_physical_step, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewmodel
        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]

        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)

        val savedWeight = viewModel.weight.value
        val savedHeight = viewModel.height.value


        if (savedWeight > 0f) {
            etWeight.setText(if (savedWeight % 1 == 0f) savedWeight.toInt().toString() else savedWeight.toString())
        }
        if (savedHeight > 0f) {
            etHeight.setText(if (savedHeight % 1 == 0f) savedHeight.toInt().toString() else savedHeight.toString())
        }

        etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateViewModel()
            }
        })

        etHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateViewModel()
            }
        })
    }

    //fungsi kirim nilai dari input
    private fun updateViewModel() {
        val weightStr = etWeight.text.toString()
        val heightStr = etHeight.text.toString()

        val weightValue = weightStr.toFloatOrNull() ?: 0f
        val heightValue = heightStr.toFloatOrNull() ?: 0f

        viewModel.setPhysicalData(newHeight = heightValue, newWeight = weightValue)
    }
}