
package com.aegis.security

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import java.security.MessageDigest
import java.security.SecureRandom

class MainActivity : Activity() {

    private lateinit var layout: LinearLayout
    private val random = SecureRandom()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDashboard()
    }

    private fun showDashboard() {
        layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 35, 28, 30)
            setBackgroundColor(0xFF101820.toInt())
        }

        val scroll = ScrollView(this)
        scroll.addView(layout)

        val title = TextView(this).apply {
            text = "AEGIS"
            textSize = 32f
            setTextColor(0xFF00E5FF.toInt())
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        layout.addView(title)

        addText("MOBILE SECURITY CENTER", 15f, 0xFFFFFFFF.toInt())
        addText("Your device security dashboard", 13f, 0xFFB0BEC5.toInt())

        addButton("SCAN FILE") { chooseFile() }
        addButton("CHECK PHISHING URL") { checkUrl() }
        addButton("PASSWORD STRENGTH CHECKER") { passwordChecker() }
        addButton("GENERATE STRONG PASSWORD") { generatePassword() }
        addButton("NETWORK SECURITY INFO") { networkInfo() }
        addButton("PRIVACY CHECKLIST") { privacyChecklist() }
        addButton("ANDROID SECURITY UPDATE") { openSettings(Settings.ACTION_SECURITY_SETTINGS) }
        addButton("APP PERMISSIONS") { openSettings(Settings.ACTION_APPLICATION_SETTINGS) }

        addText(
            "Note: Aegis performs basic local checks. It is not a full antivirus and cannot guarantee detection of malware.",
            12f,
            0xFFFFD180.toInt()
        )

        setContentView(scroll)
    }

    private fun addText(value: String, size: Float, color: Int) {
        val text = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(5, 14, 5, 14)
        }
        layout.addView(text)
    }

    private fun addButton(label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF007C91.toInt())
            textSize = 14f
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 9, 0, 9)
        layout.addView(button, params)
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 101)
    }

    @Deprecated("Legacy file picker callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            scanFile(uri)
        }
    }

    private fun scanFile(uri: Uri) {
        try {
            val input = contentResolver.openInputStream(uri)
                ?: throw Exception("File open nahi ho payi")

            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var total = 0L

            input.use { stream ->
                while (true) {
                    val count = stream.read(buffer)
                    if (count == -1) break

                    total += count
                    if (total > 100L * 1024 * 1024) {
                        throw Exception("File 100 MB se badi hai")
                    }

                    digest.update(buffer, 0, count)
                }
            }

            val hash = digest.digest().joinToString("") {
                "%02x".format(it)
            }

            val name = uri.lastPathSegment ?: "Selected file"

            AlertDialog.Builder(this)
                .setTitle("File Scan Report")
                .setMessage(
                    "File: $name\n\n" +
                    "Size: $total bytes\n\n" +
                    "SHA-256:\n$hash\n\n" +
                    "Basic scan complete. Hash alone does not prove a file is safe."
                )
                .setPositiveButton("OK", null)
                .show()

        } catch (e: Exception) {
            showMessage("Scan error: ${e.message}")
        }
    }

    private fun checkUrl() {
        val input = EditText(this).apply {
            hint = "https://example.com"
        }

        AlertDialog.Builder(this)
            .setTitle("Phishing URL Checker")
            .setMessage("URL enter karein:")
            .setView(input)
            .setPositiveButton("CHECK") { _, _ ->
                val url = input.text.toString().trim()
                val result = inspectUrl(url)
                showMessage(result)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun inspectUrl(url: String): String {
        if (url.isBlank()) return "URL enter nahi kiya."

        val lower = url.lowercase()

        val suspicious = listOf(
            "login-verify",
            "account-suspended",
            "free-gift",
            "password-reset",
            "verify-account",
            "secure-update"
        )

        val warnings = mutableListOf<String>()

        if (!lower.startsWith("https://")) {
            warnings.add("HTTPS nahi hai")
        }

        if ("@" in url) {
            warnings.add("URL mein @ symbol hai")
        }

        if (lower.contains("xn--")) {
            warnings.add("Punycode domain mila")
        }

        if (suspicious.any { lower.contains(it) }) {
            warnings.add("Suspicious keyword mila")
        }

        return if (warnings.isEmpty()) {
            "Koi basic warning nahi mili.\n\nIska matlab URL safe hone ki guarantee nahi hai."
        } else {
            "WARNING: Suspicious URL ho sakta hai!\n\n" +
            warnings.joinToString("\n") +
            "\n\nLink open na karein jab tak verify na ho."
        }
    }

    private fun passwordChecker() {
        val input = EditText(this).apply {
            hint = "Password enter karein"
        }

        AlertDialog.Builder(this)
            .setTitle("Password Strength")
            .setView(input)
            .setPositiveButton("CHECK") { _, _ ->
                val p = input.text.toString()
                var score = 0

                if (p.length >= 12) score++
                if (p.any { it.isUpperCase() }) score++
                if (p.any { it.isLowerCase() }) score++
                if (p.any { it.isDigit() }) score++
                if (p.any { !it.isLetterOrDigit() }) score++

                val result = when {
                    p.length < 8 -> "Weak"
                    score <= 2 -> "Weak"
                    score <= 3 -> "Medium"
                    else -> "Strong"
                }

                showMessage("Password rating: $result\n\n" +
                    "Ye basic estimate hai, guarantee nahi.")
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun generatePassword() {
        val chars =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#\$%&*"

        val password = (1..20).map {
            chars[random.nextInt(chars.length)]
        }.joinToString("")

        val box = EditText(this).apply {
            setText(password)
            isFocusable = false
        }

        AlertDialog.Builder(this)
            .setTitle("Strong Password")
            .setMessage("Apna password copy karke secure jagah rakhein.")
            .setView(box)
            .setPositiveButton("DONE", null)
            .show()
    }

    private fun networkInfo() {
        try {
            val manager =
                getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

            val network = manager.activeNetwork
            val capabilities = manager.getNetworkCapabilities(network)

            val type = when {
                capabilities == null -> "No active network"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
                else -> "Other network"
            }

            val vpn = capabilities?.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN
            ) == true

            showMessage(
                "Network: $type\nVPN detected: $vpn\n\n" +
                "VPN detection does not verify network safety."
            )
        } catch (e: Exception) {
            showMessage("Network information unavailable.")
        }
    }

    private fun privacyChecklist() {
        val items = arrayOf(
            "Screen lock enabled",
            "Unknown app installation disabled",
            "App permissions reviewed",
            "Android updated",
            "Google Play Protect enabled"
        )

        AlertDialog.Builder(this)
            .setTitle("Privacy Checklist")
            .setMultiChoiceItems(items, BooleanArray(items.size)) {
                _, _, _ -> 
            }
            .setPositiveButton("DONE", null)
            .show()
    }

    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Aegis Security")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
