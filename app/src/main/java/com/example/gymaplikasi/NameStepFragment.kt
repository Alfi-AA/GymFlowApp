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

class NameStepFragment : Fragment() {

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var etName: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_name_step, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[OnboardingViewModel::class.java]

        etName = view.findViewById(R.id.etName)
        etName.setText(viewModel.name.value)

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Masukkan teks ke dalam ransel (ViewModel)
                viewModel.setName(s.toString())
            }
        })
    }
}