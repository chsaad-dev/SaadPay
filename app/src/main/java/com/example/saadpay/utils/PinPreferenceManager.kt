package com.example.saadpay.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PinPreferenceManager(context: Context) {

    companion object {
        private const val PREF_FILE = "pin_prefs"
        private const val KEY_PIN = "user_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREF_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(): String? {
        return prefs.getString(KEY_PIN, null)
    }

    fun isPinSet(): Boolean {
        return getPin() != null
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    // ✅ NEW: Biometric toggle handling
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true) // default: true
    }
}
