package com.example.gymaplikasi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private lateinit var auth: FirebaseAuth
    private var oobCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // menangkap oobCode dari auth activity
        oobCode = arguments?.getString("OOB_CODE")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val etNewPassword = view.findViewById<TextInputEditText>(R.id.et_new_password)
        val etConfirmPassword = view.findViewById<TextInputEditText>(R.id.et_confirm_new_password)
        val btnConfirmReset = view.findViewById<Button>(R.id.btn_confirm_reset)

        // cek dulu kalo oobCodenya ksoong
        if (oobCode == null) {
            Toast.makeText(requireContext(), "Akses ditolak. Gunakan link dari email Anda.", Toast.LENGTH_LONG).show()
            backToLogin()
            return
        }

        btnConfirmReset.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            //validasi kalo input kosong
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom kata sandi harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //validasi kecocokan sandi
            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Kata sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //valdiasi keamanan standar sandi harus lebih dari 6 karakter
            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Kata sandi minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //eksekusi ganti password
            auth.confirmPasswordReset(oobCode!!, newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Kata sandi berhasil diubah! Silakan login kembali.", Toast.LENGTH_LONG).show()
                        // Lempar user kembali ke halaman Login
                        backToLogin()
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengubah kata sandi. Link mungkin sudah kedaluwarsa.", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun backToLogin() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.authFragmentContainer, LoginFragment())
            .commit()
    }
}