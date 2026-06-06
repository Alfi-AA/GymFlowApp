package com.example.gymaplikasi

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gymaplikasi.utils.UserPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ProfileFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences

    // view header profile
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileAge: TextView
    private lateinit var tvProfileGender: TextView
    private lateinit var tvProfileWeight: TextView
    private lateinit var tvProfileHeight: TextView

    // view kalkulator bmi
    private lateinit var tvName: TextView
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnCalculate: AppCompatButton
    private lateinit var tvBmiResult: TextView
    private lateinit var tvBmiStatus: TextView
    private lateinit var btnLogout: AppCompatButton

    // view edit profil
    private lateinit var etEditName: EditText
    private lateinit var cardMale: com.google.android.material.card.MaterialCardView
    private lateinit var cardFemale: com.google.android.material.card.MaterialCardView
    private lateinit var tvMaleIcon: TextView
    private lateinit var tvMaleText: TextView
    private lateinit var tvFemaleIcon: TextView
    private lateinit var tvFemaleText: TextView
    private lateinit var etEditDob: EditText
    private lateinit var etEditWeight: EditText
    private lateinit var etEditHeight: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var btnEditProfile: Button

    // Status State
    private var isEditing = false
    private var selectedGender = "" // untuk gender

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userPreferences = UserPreferences(requireContext())

        initViews(view)
        loadUserData()
        setEditMode(false) // Awalnya dalam mode baca
        setupActions()

        return view
    }

    private fun initViews(view: View) {
        //init header views
        tvProfileName = view.findViewById(R.id.tvProfileName)
        tvProfileAge = view.findViewById(R.id.tvProfileAge)
        tvProfileGender = view.findViewById(R.id.tvProfileGender)
        tvProfileWeight = view.findViewById(R.id.tvProfileWeight)
        tvProfileHeight = view.findViewById(R.id.tvProfileHeight)

        //init bmi views
        tvName = view.findViewById(R.id.tvProfileName)
        etHeight = view.findViewById(R.id.etHeight)
        etWeight = view.findViewById(R.id.etWeight)
        btnCalculate = view.findViewById(R.id.btnCalculateBmi)
        tvBmiResult = view.findViewById(R.id.tvBmiResult)
        tvBmiStatus = view.findViewById(R.id.tvBmiStatus)
        btnLogout = view.findViewById(R.id.btnLogout)

        //ini edit profile views
        etEditName = view.findViewById(R.id.etEditName)
        cardMale = view.findViewById(R.id.cardMale)
        cardFemale = view.findViewById(R.id.cardFemale)
        tvMaleIcon = view.findViewById(R.id.tvMaleIcon)
        tvMaleText = view.findViewById(R.id.tvMaleText)
        tvFemaleIcon = view.findViewById(R.id.tvFemaleIcon)
        tvFemaleText = view.findViewById(R.id.tvFemaleText)
        etEditDob = view.findViewById(R.id.etEditDob)
        etEditWeight = view.findViewById(R.id.etEditWeight)
        etEditHeight = view.findViewById(R.id.etEditHeight)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
    }

    // Menampilkan data profil yang tersimpan di SharedPreferences
    private fun loadUserData() {
        val name = userPreferences.getUserName() ?: "User"
        val gender = userPreferences.getGender() ?: "Male"
        val dobRaw = userPreferences.getDob() ?: ""
        val dob = formatDob(dobRaw)
        val weight = userPreferences.getWeight() ?: ""
        val height = userPreferences.getHeight() ?: ""

        // header info
        tvProfileName.text = name
        tvProfileGender.text = gender
        tvProfileWeight.text = if (weight.isNotEmpty()) "$weight KG" else "0 KG"
        tvProfileHeight.text = if (height.isNotEmpty()) "$height CM" else "0 CM"

        //kalkulasi umur
        var calculatedAge = "-"
        if (dob.isNotEmpty() && dob.contains("/")) {
            try {
                val parts = dob.split("/")
                if (parts.size == 3) {
                    val birthYear = parts[2].toInt()
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val age = currentYear - birthYear
                    calculatedAge = age.toString()
                }
            } catch (e: Exception) {
                calculatedAge = "-"
            }
        }
        tvProfileAge.text = calculatedAge

        tvName.text = name

        // isi form edit
        etEditName.setText(name)
        updateGenderUI(gender)
        etEditDob.setText(dob)
        etEditWeight.setText(weight)
        etEditHeight.setText(height)
    }

    private fun setupActions() {
        // buat kalkulator bmi
        btnCalculate.setOnClickListener {
            if (isEditing) {
                showWarningDialog {
                    // Kalau user maksa lanjut
                    setEditMode(false)
                    calculateBMI()
                }
            } else {
                calculateBMI()
            }
        }

        // logika edit profile
        btnEditProfile.setOnClickListener {
            setEditMode(true)
        }

        btnSaveProfile.setOnClickListener {
            saveProfileData()
        }

        // pilih gender
        cardMale.setOnClickListener {
            if (isEditing) updateGenderUI("Male")
        }
        cardFemale.setOnClickListener {
            if (isEditing) updateGenderUI("Female")
        }

        // kalender tanggal lahir
        etEditDob.setOnClickListener {
            if (isEditing) showDatePicker()
        }

        // logika logout
        btnLogout.setOnClickListener {
            if (isEditing) {
                showWarningDialog { executeLogout() }
            } else {
                executeLogout()
            }
        }
    }

    // membuka dan mengunci edit profil
    private fun setEditMode(enabled: Boolean) {
        isEditing = enabled

        etEditName.isEnabled = enabled
        etEditDob.isEnabled = enabled
        etEditWeight.isEnabled = enabled
        etEditHeight.isEnabled = enabled

        if (enabled) {
            btnSaveProfile.visibility = View.VISIBLE
            btnEditProfile.visibility = View.GONE
        } else {
            btnSaveProfile.visibility = View.GONE
            btnEditProfile.visibility = View.VISIBLE

            // Reset form ke data asli jika dibatalkan/dibuang
            loadUserData()
        }
    }

    private fun saveProfileData() {
        val newName = etEditName.text.toString().trim()
        val newDob = etEditDob.text.toString().trim()
        val newWeight = etEditWeight.text.toString().trim()
        val newHeight = etEditHeight.text.toString().trim()

        if (newName.isEmpty() || newDob.isEmpty() || newWeight.isEmpty() || newHeight.isEmpty()) {
            Toast.makeText(requireContext(), "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        // simpan ke shared preferences
        userPreferences.setUserName(newName)
        userPreferences.setGender(selectedGender)
        userPreferences.setDob(newDob)
        userPreferences.setWeight(newWeight)
        userPreferences.setHeight(newHeight)

        // siapkan data untuk kirim ke firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            val profileData = hashMapOf<String, Any>(
                "name" to newName,
                "gender" to selectedGender,
                "dob" to newDob,
                "height" to (newHeight.toFloatOrNull() ?: 0f),
                "weight" to (newWeight.toFloatOrNull() ?: 0f)
            )

            // kirim ke cloud
            db.collection("users").document(userId)
                .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profil berhasil disimpan ke Cloud", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal menyimpan ke Cloud. Data tersimpan di lokal.", Toast.LENGTH_SHORT).show()
                }
        }

        Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()

        loadUserData()

        // Kunci kembali form setelah simpan
        setEditMode(false)
    }

    private fun updateGenderUI(gender: String) {
        selectedGender = gender
        val activeColor = ContextCompat.getColor(requireContext(), R.color.gym_primary)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.white)
        val transparentColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)

        if (gender == "Male") {
            // Male Aktif
            cardMale.strokeColor = activeColor
            tvMaleIcon.setTextColor(activeColor)
            tvMaleText.setTextColor(activeColor)

            // Female Pasif
            cardFemale.strokeColor = transparentColor
            tvFemaleIcon.setTextColor(inactiveColor)
            tvFemaleText.setTextColor(inactiveColor)
        } else {
            // Female Aktif
            cardFemale.strokeColor = activeColor
            tvFemaleIcon.setTextColor(activeColor)
            tvFemaleText.setTextColor(activeColor)

            // Male Pasif
            cardMale.strokeColor = transparentColor
            tvMaleIcon.setTextColor(inactiveColor)
            tvMaleText.setTextColor(inactiveColor)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val dateStr = "$day/${month + 1}/$year"
                etEditDob.setText(dateStr)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    // translate tanggal
    private fun formatDob(dobStr: String): String {
        if (dobStr.isEmpty()) return ""

        // Cek apakah dobStr murni berisi Timestamp dari Onboarding
        return if (dobStr.toLongOrNull() != null) {
            try {
                // Ubah Timestamp ke format "DD/MM/YYYY"
                val date = java.util.Date(dobStr.toLong())
                val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                format.format(date)
            } catch (e: Exception) {
                dobStr // Jika gagal, kembalikan apa adanya
            }
        } else {
            // Jika formatnya sudah "DD/MM/YYYY" biarkan saja
            dobStr
        }
    }

    // peringatan kehilangan data
    private fun showWarningDialog(onConfirmDiscard: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Buang Perubahan?")
            .setMessage("Anda sedang mengedit profil. Jika beralih sekarang, perubahan tidak akan disimpan.")
            .setPositiveButton("Buang") { _, _ ->
                onConfirmDiscard()
            }
            .setNegativeButton("Lanjutkan Edit", null)
            .show()
    }

    // tangkap jika user ganti fragment
    override fun onPause() {
        super.onPause()
        if (isEditing) {
            Toast.makeText(requireContext(), "Perubahan profil dibatalkan", Toast.LENGTH_SHORT).show()
            setEditMode(false)
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

    private fun executeLogout() {
        userPreferences.logout()
        FirebaseAuth.getInstance().signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}