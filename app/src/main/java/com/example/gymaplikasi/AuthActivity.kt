package com.example.gymaplikasi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        if (savedInstanceState == null) {
            val action = intent.action
            val data = intent.data

            //cek apakah aplikasi dibuka dari link tertentu
            if (Intent.ACTION_VIEW == action && data != null) {

                // ambil data spesifik dari url link firebase
                var mode = data.getQueryParameter("mode")
                var oobCode = data.getQueryParameter("oobCode")

                //bongkar url link jika ada parameter lain
                if (mode == null || oobCode == null) {
                    val innerLink = data.getQueryParameter("link")
                    if (innerLink != null) {
                        val innerUri = android.net.Uri.parse(innerLink)
                        mode = innerUri.getQueryParameter("mode")
                        oobCode = innerUri.getQueryParameter("oobCode")
                    }
                }

                // kalo bener link lupa password
                if (mode == "resetPassword" && oobCode != null) {

                    //siapkan RessetPasswordFragment dan titipkan oobCode
                    val resetFragment = ResetPasswordFragment().apply {
                        arguments = Bundle().apply {
                            putString("OOB_CODE", oobCode)
                        }
                    }

                    // lempar user ke halaman reset password
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.authFragmentContainer, resetFragment)
                        .commit()
                } else if (mode == "verifyEmail" && oobCode != null){
                    Toast.makeText(this, "Memverifikasi email Anda...", Toast.LENGTH_SHORT).show()

                    // Tembak langsung kode verifikasinya ke Firebase Auth
                    com.google.firebase.auth.FirebaseAuth.getInstance().applyActionCode(oobCode)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Email berhasil diverifikasi! Silakan masuk.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Verifikasi gagal atau link sudah kedaluwarsa.", Toast.LENGTH_LONG).show()
                            }

                            // Setelah verifikasi selesai (sukses/gagal), arahkan ke LoginFragment
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.authFragmentContainer, LoginFragment())
                                .commit()
                        }
                } else {
                    Toast.makeText(this, "Link tidak valid atau tidak dikenali", Toast.LENGTH_SHORT).show()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.authFragmentContainer, LoginFragment())
                        .commit()
                }
            } else {
                // kalo user buka aplkasi secara normal
                supportFragmentManager.beginTransaction()
                    .replace(R.id.authFragmentContainer, LoginFragment())
                    .commit()
            }
        }
    }
}