package com.example.gymaplikasi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password){

    // variabel fireabase
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // manggil firebase
        auth = FirebaseAuth.getInstance()

        val etEmail = view.findViewById<EditText>(R.id.et_email_forgot)
        val btnKirim = view.findViewById<Button>(R.id.btn_send_reset_link)
        val btnBack = view.findViewById<Button>(R.id.btn_back)

        // tombol kembali
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // tombol kirim email
        btnKirim.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // Cek agar email ga kosong
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Email tidak boleh kosong, Bos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //konfigurasi firebase
            val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                .setUrl("https://gymflow-49832.firebaseapp.com/?mode=resetPassword")
                .setHandleCodeInApp(true) // Memaksa HP buka aplikasi
                .setAndroidPackageName(
                    "com.example.gymaplikasi",
                    true, // Install app kalau belum ada
                    "12"  // Minimum versi
                )
                .build()

            // kirim email degnan konfigurasi barusan
            auth.sendPasswordResetEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Kalau sukses terkirim
                        Toast.makeText(requireContext(), "Tautan pemulihan khusus aplikasi telah dikirim!", Toast.LENGTH_LONG).show()
                        etEmail.text.clear()
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengirim link: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}