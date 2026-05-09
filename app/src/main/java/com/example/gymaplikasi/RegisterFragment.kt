package com.example.gymaplikasi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
class RegisterFragment : Fragment(R.layout.fragment_register) {

    // variabel firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Google Sign-In Dibatalkan / Gagal", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val etEmail = view.findViewById<EditText>(R.id.et_email_register)
        val etPassword = view.findViewById<EditText>(R.id.et_password_register)
        val etConfirmPassword = view.findViewById<EditText>(R.id.et_confirm_password_register)
        val btnRegister = view.findViewById<Button>(R.id.btn_register_submit)
        val btnBack = view.findViewById<Button>(R.id.btn_back_to_login)
        val btnGoogle = view.findViewById<Button>(R.id.btn_google_register)

        // Logika untuk kembali ke halaman Login (membunuh fragment Register)
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // logika button bikin akun
        btnRegister.setOnClickListener {
            // ambil teks user
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // cek kolom kosong
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // cek password sama atau ngga
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Kata sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // password minimal 6 karakter
            if (password.length < 6) {
                Toast.makeText(requireContext(), "Kata sandi minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // ini untuk eksekusi ke firebase
            Toast.makeText(requireContext(), "Mendaftarkan akun...", Toast.LENGTH_SHORT).show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Akun berhasil dibuat! Silakan cek email Anda untuk verifikasi.", Toast.LENGTH_LONG).show()

                                //LOGOUT paksa
                                auth.signOut()
                                requireActivity().supportFragmentManager.popBackStack()
                            } else {
                                Toast.makeText(requireContext(), "Gagal kirim email verifikasi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Kalau gagal (misal email udah pernah dipakai, atau format email salah)
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthUserCollisionException) {
                            etEmail.error = "Email ini sudah terdaftar!"
                            etEmail.requestFocus()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

        //logika kalo pake google
        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    // untuk login firebase
    private fun firebaseAuthWithGoogle(idToken: String) {
        Toast.makeText(requireContext(), "Memproses akun Google...", Toast.LENGTH_SHORT).show()
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // cek akun baru atau lama
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    if (isNewUser) {
                        Toast.makeText(requireContext(), "Pendaftaran Google Berhasil! Silakan Login.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Akun ini sudah terdaftar! Silakan Login.", Toast.LENGTH_SHORT).show()
                    }

                    // Apapun hasilnya, logout paksa dan kembali ke layar login
                    auth.signOut()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Pendaftaran Google Gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }
}