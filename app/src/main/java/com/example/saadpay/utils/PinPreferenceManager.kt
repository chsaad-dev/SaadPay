package com.example.saadpay.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth

class PinPreferenceManager(context: Context) {

    companion object {
        private const val PREF_FILE = "pin_prefs"
        private const val KEY_PIN = "user_pin_"     // suffix with UID
        private const val KEY_BIOMETRIC = "biometric_enabled_"
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

    private fun currentUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun savePin(pin: String) {
        val uid = currentUid() ?: return
        prefs.edit().putString(KEY_PIN + uid, pin).apply()
    }

    fun getPin(): String? {
        val uid = currentUid() ?: return null
        return prefs.getString(KEY_PIN + uid, null)
    }

    fun isPinSet(): Boolean {
        return getPin() != null
    }

    fun clearPin() {
        val uid = currentUid() ?: return
        prefs.edit().remove(KEY_PIN + uid).apply()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val uid = currentUid() ?: return
        prefs.edit().putBoolean(KEY_BIOMETRIC + uid, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        val uid = currentUid() ?: return false
        return prefs.getBoolean(KEY_BIOMETRIC + uid, false)
    }

    fun clearBiometric() {
        val uid = currentUid() ?: return
        prefs.edit().remove(KEY_BIOMETRIC + uid).apply()
    }
}
