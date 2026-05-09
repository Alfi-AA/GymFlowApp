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
                } else {
                    Toast.makeText(this, "Link tidak valid", Toast.LENGTH_SHORT).show()
                    // ini kalo linknya tidak sesuai untuk lupa password, jadinya ttp kelempar ke login
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