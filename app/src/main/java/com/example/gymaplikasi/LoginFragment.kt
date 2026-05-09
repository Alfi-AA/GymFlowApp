package com.example.gymaplikasi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gymaplikasi.data.AppDatabase
import com.example.gymaplikasi.repository.GymRepository
import com.example.gymaplikasi.utils.UserPreferences
import com.example.gymaplikasi.viewmodel.SyncViewModel
import com.example.gymaplikasi.viewmodel.SyncViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button


    private lateinit var userPreferences: UserPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var syncViewModel: SyncViewModel

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

        userPreferences = UserPreferences(requireContext())
        auth = FirebaseAuth.getInstance()

        val dao = AppDatabase.getDatabase(requireContext()).gymLogDao()
        val repository = GymRepository(dao, FirebaseFirestore.getInstance())
        val factory = SyncViewModelFactory(repository)
        syncViewModel = ViewModelProvider(this, factory)[SyncViewModel::class.java]

        // Cek jika user sudah login, langsung arahkan ke MainActivity
        if (auth.currentUser != null) {
            goToMainActivity()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Kode otomatis dari Firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        initViews(view)
        setupAction()

        val btnGoToRegister = view.findViewById<Button>(R.id.btn_register)
        btnGoToRegister.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.authFragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        tvForgotPassword.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.authFragmentContainer, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.et_email)
        etPass = view.findViewById(R.id.et_password)
        btnLogin = view.findViewById(R.id.btn_login)
        btnGoogle = view.findViewById(R.id.btn_google)

        // memori untuk mengingat email terakhir
        val loginPrefs = requireActivity().getSharedPreferences("LoginPrefs", android.content.Context.MODE_PRIVATE)
        val lastEmail = loginPrefs.getString("LAST_EMAIL", "")

        if (!lastEmail.isNullOrEmpty()) {
            etEmail.setText(lastEmail)
            etPass.requestFocus()
        }
    }

    private fun setupAction() {
        //login email biasa
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPass.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Tolong isi semua field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(requireContext(), "Mencoba masuk...", Toast.LENGTH_SHORT).show()

            // Autentikasi menggunakan firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // refresh data user dari server firebase
                        user?.reload()?.addOnCompleteListener { reloadTask ->
                            if (reloadTask.isSuccessful) {
                                // cek email apakah sudah diverifikasi
                                if (user.isEmailVerified) {
                                    // kalp udh, lanjut login
                                    val userEmail = user.email
                                    if (userEmail != null) {
                                        val loginPrefs = requireActivity().getSharedPreferences("LoginPrefs", android.content.Context.MODE_PRIVATE)
                                        loginPrefs.edit().putString("LAST_EMAIL", userEmail).apply()
                                    }
                                    loginSuccess()
                                } else {
                                    // paksa logout kalo belom
                                    Toast.makeText(requireContext(), "Email belum diverifikasi! Silakan cek kotak masuk/spam Anda.", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Gagal memeriksa status akun.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Login Gagal: Email atau Password salah", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        //login untuk google
        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Toast.makeText(requireContext(), "Memverifikasi akun Google...", Toast.LENGTH_SHORT).show()
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val userEmail = auth.currentUser?.email
                    if (userEmail != null) {
                        val loginPrefs = requireActivity().getSharedPreferences("LoginPrefs", android.content.Context.MODE_PRIVATE)
                        loginPrefs.edit().putString("LAST_EMAIL", userEmail).apply()
                    }
                    loginSuccess()
                } else {
                    Toast.makeText(requireContext(), "Firebase Autentikasi Gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginSuccess() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Toast.makeText(requireContext(), "Memeriksa profil akun...", Toast.LENGTH_SHORT).show()

            //cek apakah profile user udh ada di firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // kondisi user lama
                        val nameFromCloud = document.getString("name") ?: "User"
                        val genderFromCloud = document.getString("gender") ?: "Male"


                        userPreferences.saveUser(nameFromCloud)
                        userPreferences.setGender(genderFromCloud)

                        Toast.makeText(requireContext(), "Memulihkan data dari Cloud...", Toast.LENGTH_SHORT).show()

                        syncViewModel.triggerInitialSync(userId) {
                            Toast.makeText(requireContext(), "Selamat Datang kembali, $nameFromCloud!", Toast.LENGTH_SHORT).show()
                            goToMainActivity()
                        }
                    } else {
                        // kondisi user baru
                        val intent = Intent(requireActivity(), OnboardingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memeriksa akun. Coba lagi.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}