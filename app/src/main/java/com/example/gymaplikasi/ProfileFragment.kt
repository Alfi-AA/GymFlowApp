package com.example.gymaplikasi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.gymaplikasi.utils.UserPreferences

class ProfileFragment : Fragment(){

    private lateinit var userPreferences: UserPreferences

    private lateinit var tvName: TextView
    private lateinit var tvNim: TextView
    private lateinit var tvClass: TextView
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnCalculate: AppCompatButton
    private lateinit var tvBmiResult: TextView
    private lateinit var tvBmiStatus: TextView
    private lateinit var etTargetWeight: EditText
    private lateinit var btnSaveTarget: AppCompatButton
    private lateinit var btnLogout: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userPreferences = UserPreferences(requireContext())

        initViews(view)
        loadUserData()
        setupActions()

        return view
    }

    private fun initViews(view: View) {
        tvName = view.findViewById(R.id.tvProfileName)
        tvNim = view.findViewById(R.id.tvProfileNim)
        tvClass = view.findViewById(R.id.tvProfileClass)
        etHeight = view.findViewById(R.id.etHeight)
        etWeight = view.findViewById(R.id.etWeight)
        btnCalculate = view.findViewById(R.id.btnCalculateBmi)
        tvBmiResult = view.findViewById(R.id.tvBmiResult)
        tvBmiStatus = view.findViewById(R.id.tvBmiStatus)
        etTargetWeight = view.findViewById(R.id.etTargetWeight)
        btnSaveTarget = view.findViewById(R.id.btnSaveTarget)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    // Menampilkan data profil yang tersimpan di SharedPreferences
    private fun loadUserData() {
        val name = userPreferences.getUserName() ?: "User"
        val nim = userPreferences.getNim() ?: "-"
        val kelas = userPreferences.getKelas() ?: "-"
        val targetBb = userPreferences.getTargetBb()

        tvName.text = name
        tvNim.text = "NIM: $nim"
        tvClass.text = "KELAS $kelas"
        etTargetWeight.setText(targetBb.toString())
    }

    private fun setupActions() {
        btnCalculate.setOnClickListener {
            calculateBMI()
        }

        // Menyimpan target berat badan baru ke SharedPreferences
        btnSaveTarget.setOnClickListener {
            val newTargetStr = etTargetWeight.text.toString()
            if (newTargetStr.isNotEmpty()) {
                userPreferences.saveTargetWeight(newTargetStr.toFloat())
                Toast.makeText(requireContext(), "Target Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout: menghapus sesi dan kembali ke LoginActivity
        btnLogout.setOnClickListener {
            userPreferences.logout()

            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    // Menghitung BMI berdasarkan tinggi dan berat badan inputan user
    private fun calculateBMI() {
        val heightStr = etHeight.text.toString()
        val weightStr = etWeight.text.toString()

        if (heightStr.isNotEmpty() && weightStr.isNotEmpty()) {
            val heightCm = heightStr.toFloat()
            val weightKg = weightStr.toFloat()
            val heightM = heightCm / 100
            val bmi = weightKg / (heightM * heightM)

            tvBmiResult.text = String.format("%.1f", bmi)

            // Menentukan status BMI
            val status = when {
                bmi < 18.5 -> "KURUS"
                bmi in 18.5..24.9 -> "NORMAL"
                else -> "GEMUK"
            }
            tvBmiStatus.text = "- $status"
        } else {
            Toast.makeText(requireContext(), "Isi tinggi dan berat badan!", Toast.LENGTH_SHORT).show()
        }
    }
}