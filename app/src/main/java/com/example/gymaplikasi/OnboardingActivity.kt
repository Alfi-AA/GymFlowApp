package com.example.gymaplikasi

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.repository.GymRepository
import com.example.gymaplikasi.viewmodel.OnboardingViewModel
import com.example.gymaplikasi.viewmodel.OnboardingViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewModel: OnboardingViewModel
    private var currentStep = 1

    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // inisialisasi view model onboarding
        val dao = AppDatabase.getDatabase(this).gymLogDao()
        val repository = GymRepository(dao, FirebaseFirestore.getInstance())
        val factory = OnboardingViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[OnboardingViewModel::class.java]

        progressBar = findViewById(R.id.onboardingProgress)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        //fragment pertama saat di start
        if (savedInstanceState == null) {
            showFragment(1)
        }

        setupActions()
    }

    private fun setupActions() {
        btnNext.setOnClickListener {
            if (currentStep < 4) {
                currentStep++
                showFragment(currentStep)
            } else {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                viewModel.saveProfileToDatabase(userId) {
                    val userPrefs = com.example.gymaplikasi.utils.UserPreferences(this@OnboardingActivity)
                    userPrefs.saveUser(viewModel.name.value)
                    userPrefs.setGender(viewModel.gender.value)

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        btnBack.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                showFragment(currentStep)
            }
        }
    }

    private fun showFragment(step: Int) {
        val fragment = when (step) {
            1 -> NameStepFragment()
            2 -> GenderStepFragment()
            3 -> DobStepFragment()
            4 -> PhysicalStepFragment()
            else -> NameStepFragment()
        }

        //update UI dan tombol
        progressBar.progress = step
        btnBack.visibility = if (step == 1) View.GONE else View.VISIBLE
        btnNext.text = if (step == 4) "Mulai Latihan" else "Berikutnya"

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            .replace(R.id.onboardingContainer, fragment)
            .commit()
    }
}