package com.example.gymaplikasi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gymaplikasi.utils.UserPreferences

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userPreferences = UserPreferences(this)

        // Cek jika user sudah login, langsung arahkan ke MainActivity
        if (userPreferences.isLoggedIn()) {
            goToMainActivity()
        }

        initViews()
        setupAction()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.et_email)
        etPass = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
    }

    private fun setupAction() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPass.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Autentikasi sederhana (Hardcoded)
            if (email == "admin@gym.com" && password == "12345") {
                loginSuccess()
            } else {
                Toast.makeText(this, "Wrong Email or Password!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginSuccess() {
        // Data dummy user yang akan disimpan
        val userName = "Alfiansyah"
        val userNim = "2290343010"
        val userClass = "RJ22A"
        val targetWeight = 80.0f

        // Simpan data ke SharedPreferences
        userPreferences.saveUser(
            name = userName,
            nim = userNim,
            kelas = userClass,
            targetBb = targetWeight
        )

        Toast.makeText(this, "Welcome back, $userName!", Toast.LENGTH_SHORT).show()
        goToMainActivity()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Hapus stack activity agar user tidak bisa kembali ke Login dengan tombol Back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}