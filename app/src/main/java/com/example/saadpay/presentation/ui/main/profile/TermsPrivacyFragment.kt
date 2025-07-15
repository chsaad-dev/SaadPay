package com.example.saadpay.presentation.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.saadpay.R

class TermsPrivacyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_terms_privacy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isAdded) return
        super.onViewCreated(view, savedInstanceState)

        val termsText = """
            SaadPay – Terms and Conditions & Privacy Policy

            Effective Date: July 15, 2025
            Developer: Muhammad Saad
            Contact: saadw7751@gmail.com

            ====================
            TERMS AND CONDITIONS
            ====================

            1. Introduction
            SaadPay is a simulated mobile wallet Android application created purely for educational and portfolio purposes. It is not a licensed banking, finance, or money transfer service. By using the app, you agree to these Terms and Conditions.

            2. Educational Use Only
            All features in SaadPay (including sending money, transaction history, and balance display) are simulations. No real money is transferred or involved. This app is designed to demonstrate technical development skills only.

            3. User Registration
            Users may sign up using an email and password. There are no age restrictions, but by using the app, you acknowledge this is a non-commercial demo product and agree not to misuse it.

            4. Dummy Transactions
            Users may simulate sending money to other users via email addresses. These transactions:
            - Are not linked to real banks or wallets
            - Are stored for demo purposes only
            - Are irreversible once recorded

            5. No Refund or Reversal
            Since this app does not handle real funds, there is no refund, reversal, or dispute policy. Once a simulated transaction is confirmed, it is final.

            6. Security Features
            To simulate secure app features, SaadPay includes:
            - PIN code entry
            - Biometric fingerprint support (if available)
            These features are only for user experience demonstration and do not encrypt or secure real financial data.

            7. App Usage
            You agree to use SaadPay responsibly and only for its intended learning and demonstration purposes. Any misuse, reverse-engineering, or data extraction is strictly prohibited.

            =================
            PRIVACY POLICY
            =================

            1. Data Collection
            SaadPay stores limited user information in Firebase:
            - Email address
            - Full name
            - Simulated transaction history
            This data is saved solely to demonstrate UI updates and interactions within the app. No data is processed, analyzed, or monetized.

            2. Data Usage
            - Your data is only used locally in the app to populate profile and transaction screens.
            - No external APIs or services use your personal information.

            3. Data Sharing
            We do not share your information with any third party. This is a closed, educational project.

            4. Firebase Usage
            All data is stored on Google Firebase's secure cloud database. While Firebase provides security, the developer is not responsible for any data breaches or access outside of intended use.

            5. Data Retention
            Your simulated data may be cleared, updated, or overwritten at any time, especially when the app is rebuilt or reset. No persistent backups are kept.

            6. Account Deletion
            As this is an educational app, users may uninstall the app at any time. There is no formal delete account option, but you can email us at saadw7751@gmail.com to request deletion.

            7. Cookies & Tracking
            SaadPay does not use cookies, advertising SDKs, or location tracking.

            =======================
            LEGAL DISCLAIMER
            =======================

            SaadPay is not a licensed financial institution and should not be used for actual money transfer or banking. The developer, Muhammad Saad, makes no warranties or guarantees of any kind. This app is provided “as-is” for educational demonstration only.

            =======================
            SUPPORT
            =======================

            For feedback or help, please email:
            📧 saadw7751@gmail.com

            Thank you for using SaadPay responsibly.
        """.trimIndent()

        val termsBodyView = view.findViewById<TextView>(R.id.termsBody)
        termsBodyView?.text = termsText
    }
}
