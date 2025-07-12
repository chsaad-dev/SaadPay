package com.example.saadpay.presentation.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.saadpay.databinding.ActivityDashboardBinding
import com.example.saadpay.presentation.ui.loadmoney.LoadMoneyActivity
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.ui.security.ChangePinActivity
import com.example.saadpay.presentation.ui.security.ForgotPinActivity
import com.example.saadpay.presentation.ui.sendmoney.SendMoneyActivity
import com.example.saadpay.presentation.ui.security.PinLockActivity
import com.example.saadpay.presentation.ui.security.SetPinActivity
import com.example.saadpay.presentation.ui.transactionhistory.TransactionHistoryActivity
import com.example.saadpay.presentation.viewmodel.DashboardViewModel
import com.example.saadpay.utils.PinPreferenceManager
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    private var isAuthenticated = false
    private lateinit var pinPreferenceManager: PinPreferenceManager

    private val pinLockLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            onUserAuthenticated()
        } else {
            finish()
        }
    }

    private val setPinLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            setupBiometric()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executor = ContextCompat.getMainExecutor(this)
        pinPreferenceManager = PinPreferenceManager(this)

        if (!pinPreferenceManager.isPinSet()) {
            val intent = Intent(this, SetPinActivity::class.java)
            setPinLauncher.launch(intent)
        } else {
            setupBiometric()
        }
    }

    private fun setupBiometric() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            else -> {
                launchPinLock()
            }
        }
    }

    private fun showBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    launchPinLock()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                    onUserAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for SaadPay")
            .setSubtitle("Use your fingerprint or face to unlock")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun launchPinLock() {
        val intent = Intent(this, PinLockActivity::class.java)
        pinLockLauncher.launch(intent)
    }

    private fun onUserAuthenticated() {
        isAuthenticated = true
        initDashboard()
    }

    private fun initDashboard() {
        viewModel.startListeningToUser()

        viewModel.userName.observe(this) { name ->
            binding.welcomeTextView.text = "Welcome, $name"
        }

        viewModel.balance.observe(this) { amount ->
            binding.balanceTextView.text = "Balance: Rs. %.2f".format(amount)
        }

        binding.loadMoneyButton.setOnClickListener {
            startActivity(Intent(this, LoadMoneyActivity::class.java))
        }

        binding.sendMoneyButton.setOnClickListener {
            startActivity(Intent(this, SendMoneyActivity::class.java))
        }

        binding.transactionHistoryButton.setOnClickListener {
            startActivity(Intent(this, TransactionHistoryActivity::class.java))
        }

        binding.changePinButton.setOnClickListener {
            startActivity(Intent(this, ChangePinActivity::class.java))
        }

        binding.forgotPinButton.setOnClickListener {
            startActivity(Intent(this, ForgotPinActivity::class.java))
        }


        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAuthenticated) {
            viewModel.fetchCurrentUser()
        }
    }
}
