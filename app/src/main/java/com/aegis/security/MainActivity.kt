
package com.aegis.security

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import java.security.MessageDigest

class MainActivity : Activity() {

    private var hindi = false
    private lateinit var root: LinearLayout
    private lateinit var reports: TextView

    private val bg = Color.rgb(8, 15, 30)
    private val panel = Color.rgb(20, 33, 56)
    private val blue = Color.rgb(45, 137, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(165, 180, 205)
    private val green = Color.rgb(65, 210, 145)

    private val prefs by lazy {
        getSharedPreferences("aegis_reports", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        render()
    }

    private fun render() {
        val density = resources.displayMetrics.density
        val screenWidth = resources.configuration.screenWidthDp
        val columns = if (screenWidth >= 400) 2 else 1

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((16*density).toInt(), (16*density).toInt(),
                (16*density).toInt(), (24*density).toInt())
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }
        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        brand.addView(label("AEGIS", 28, white, true))
        brand.addView(label("MOBILE SECURITY • 10.1", 11, muted, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        val language = Button(this).apply {
            text = if (hindi) "EN" else "हिंदी"
            textSize = 13f
            setTextColor(white)
            background = shape(blue, 14)
            setOnClickListener {
                hindi = !hindi
                render()
            }
        }
        header.addView(language)
        root.addView(header)

        space(22)
        root.addView(label(
            if (hindi) "आपका सुरक्षा केंद्र" else "Your Security Center",
            23, white, true, Gravity.START
        ))
        root.addView(label(
            if (hindi) "डिवाइस की वास्तविक सुरक्षा जांच" else
                "Real device security checks",
            13, muted, false, Gravity.START
        ))

        space(18)

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(18, 22, 18, 22)
            background = shape(panel, 22)
        }
        status.addView(label("🛡️", 38, white))
        status.addView(label(
            if (hindi) "AEGIS सुरक्षा स्थिति" else "AEGIS SECURITY STATUS",
            13, muted, true
        ))
        status.addView(label(
            if (hindi) "जांच चलाने के लिए तैयार" else "Ready for security check",
            20, white, true
        ))
        status.addView(label(
            if (hindi) "नीचे Quick Scan दबाएं" else
                "Run Quick Scan to check your device",
            12, muted
        ))
        root.addView(status, matchWidth())

        space(22)
        root.addView(label(
            if (hindi) "सुरक्षा उपकरण" else "SECURITY TOOLS",
            14, muted, true, Gravity.START
        ))
        space(10)

        val items = listOf(
            Triple("🔍", "Quick Scan", "त्वरित स्कैन"),
            Triple("📁", "File Scanner", "फाइल स्कैनर"),
            Triple("🔐", "Privacy Guard", "प्राइवेसी सुरक्षा"),
            Triple("📶", "Network Security", "नेटवर्क सुरक्षा"),
            Triple("🌐", "Web Safety", "वेब सुरक्षा"),
            Triple("📋", "Scan Reports", "स्कैन रिपोर्ट")
        )

        for (i in items.indices step columns) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            for (j in 0 until columns) {
                val index = i + j
                if (index < items.size) {
                    val item = items[index]
                    val card = toolCard(item.first,
                        if (hindi) item.third else item.second) {
                        when (index) {
                            0 -> quickScan()
                            1 -> chooseFile()
                            2 -> privacyGuard()
                            3 -> networkCheck()
                            4 -> webSafety()
                            5 -> showReports()
                        }
                    }
                    val params = LinearLayout.LayoutParams(
                        0, (116*density).toInt(), 1f
                    )
                    if (j > 0) params.marginStart = (9*density).toInt()
                    row.addView(card, params)
                } else {
                    row.addView(View(this),
                        LinearLayout.LayoutParams(0, 1, 1f))
                }
            }
            root.addView(row)
            space(10)
        }

        space(12)
        root.addView(label(
            if (hindi) "पिछली जांच" else "LATEST SCAN REPORT",
            14, muted, true, Gravity.START
        ))
        space(8)

        reports = label(
            prefs.getString("latest", if (hindi)
                "अभी कोई रिपोर्ट उपलब्ध नहीं है"
                else "No scan report available yet")!!,
            13, white, false, Gravity.START
        )
        reports.setPadding(14, 16, 14, 16)
        reports.background = shape(panel, 16)
        root.addView(reports, matchWidth())

        space(18)
        root.addView(label(
            if (hindi)
                "ध्यान दें: AEGIS डिवाइस जांच और फाइल जानकारी प्रदान करता है। यह पूर्ण एंटीवायरस नहीं है।"
            else
                "Note: AEGIS provides device checks and file inspection. It is not a complete antivirus.",
            11, muted, false, Gravity.START
        ))
        space(12)
        root.addView(label(
            "AEGIS MOBILE SECURITY • VERSION 10.1",
            10, muted, true
        ))
    }

    private fun toolCard(
        emoji: String,
        title: String,
        action: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 10, 8, 10)
            background = shape(panel, 18)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }

            addView(label(emoji, 27, white))
            addView(label(title, 14, white, true))
            addView(label(
                if (hindi) "खोलने के लिए दबाएं" else "Tap to open",
                10, muted
            ))
        }
    }

    private fun quickScan() {
        val keyguard = getSystemService(KEYGUARD_SERVICE)
                as android.app.KeyguardManager

        val secure = keyguard.isDeviceSecure
        val vpn = isVpnActive()
        val internet = hasValidatedInternet()
        val debug = (applicationInfo.flags and
                android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val result = StringBuilder()
        result.append(if (hindi) "डिवाइस जांच परिणाम\n\n"
            else "DEVICE CHECK RESULTS\n\n")

        result.append("Android: ${Build.VERSION.RELEASE}\n")
        result.append("API: ${Build.VERSION.SDK_INT}\n\n")

        result.append(
            (if (secure) "✓ " else "⚠ ") +
            if (hindi) "स्क्रीन लॉक सुरक्षित" else "Screen lock configured"
        ).append("\n")

        result.append(
            (if (internet) "✓ " else "⚠ ") +
            if (hindi) "इंटरनेट कनेक्शन" else "Internet connectivity"
        ).append("\n")

        result.append(
            (if (vpn) "ℹ " else "✓ ") +
            if (hindi) "VPN ${if (vpn) "सक्रिय" else "पता नहीं चला"}"
            else "VPN ${if (vpn) "detected" else "not detected"}"
        ).append("\n")

        result.append(
            (if (debug) "⚠ " else "✓ ") +
            if (hindi) "ऐप Debug स्थिति: $debug"
            else "App debug build: $debug"
        )

        val report = result.toString()
        saveReport(report)

        AlertDialog.Builder(this)
            .setTitle(if (hindi) "त्वरित स्कैन" else "Quick Scan")
            .setMessage(report)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        try {
            startActivityForResult(intent, 101)
        } catch (e: Exception) {
            message("File picker could not be opened")
        }
    }

    @Deprecated("Deprecated by Android API; retained for broad compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            inspectFile(uri)
        }
    }

    private fun inspectFile(uri: Uri) {
        try {
            val name = queryFileName(uri)
            val digest = MessageDigest.getInstance("SHA-256")
            val input = contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open file")

            input.use { stream ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count <= 0) break
                    digest.update(buffer, 0, count)
                }
            }

            val hash = digest.digest().joinToString("") {
                "%02x".format(it)
            }

            val report = "File: $name\nSHA-256:\n$hash\n\n" +
                if (hindi)
                    "फाइल का hash निकाला गया है। यह malware detection नहीं है।"
                else
                    "File hash calculated. This is not a malware detection result."

            saveReport(report)

            AlertDialog.Builder(this)
                .setTitle(if (hindi) "फाइल जांच" else "File Inspection")
                .setMessage(report)
                .setPositiveButton("OK", null)
                .show()

        } catch (e: Exception) {
            message("File inspection failed: ${e.message}")
        }
    }

    private fun queryFileName(uri: Uri): String {
        var name = "Selected file"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(
                android.provider.OpenableColumns.DISPLAY_NAME
            )
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index) ?: name
            }
        }
        return name
    }

    private fun privacyGuard() {
        val options = arrayOf(
            if (hindi) "ऐप अनुमतियां" else "App permissions",
            if (hindi) "ऐप जानकारी" else "App information",
            if (hindi) "सभी ऐप सेटिंग्स" else "All app settings"
        )

        AlertDialog.Builder(this)
            .setTitle(if (hindi) "प्राइवेसी गार्ड" else "Privacy Guard")
            .setItems(options) { _, which ->
                try {
                    val intent = when (which) {
                        0 -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.parse("package:$packageName"))
                        1 -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.parse("package:$packageName"))
                        else -> Intent(Settings.ACTION_SETTINGS)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    message("Settings could not be opened")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun networkCheck() {
        val cm = getSystemService(CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val network = cm.activeNetwork
        val caps = if (network != null)
            cm.getNetworkCapabilities(network) else null

        val connected = caps?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true

        val validated = caps?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        ) == true

        val wifi = caps?.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        ) == true

        val mobile = caps?.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR
        ) == true

        val vpn = caps?.hasTransport(
            NetworkCapabilities.TRANSPORT_VPN
        ) == true

        val type = when {
            wifi -> "Wi-Fi"
            mobile -> "Mobile data"
            vpn -> "VPN"
            else -> "Unknown / disconnected"
        }

        val report = """
            Network type: $type
            Internet capability: $connected
            Internet validated: $validated
            VPN transport: $vpn
        """.trimIndent()

        saveReport(report)
        AlertDialog.Builder(this)
            .setTitle(if (hindi) "नेटवर्क सुरक्षा" else "Network Security")
            .setMessage(report)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun webSafety() {
        val text = if (hindi)
            "वेब सुरक्षा सुझाव:\n\n" +
            "• अनजान लिंक न खोलें।\n" +
            "• वेबसाइट का HTTPS जांचें।\n" +
            "• OTP और पासवर्ड साझा न करें।\n" +
            "• अनजान APK इंस्टॉल न करें।\n\n" +
            "यह सुविधा अभी ब्राउज़र ट्रैफिक को मॉनिटर नहीं करती।"
        else
            "Web safety tips:\n\n" +
            "• Avoid unknown links.\n" +
            "• Check HTTPS before entering details.\n" +
            "• Never share OTPs or passwords.\n" +
            "• Avoid installing unknown APKs.\n\n" +
            "This module does not monitor browser traffic."

        AlertDialog.Builder(this)
            .setTitle(if (hindi) "वेब सुरक्षा" else "Web Safety")
            .setMessage(text)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showReports() {
        val report = prefs.getString(
            "latest",
            if (hindi) "कोई रिपोर्ट नहीं" else "No reports available"
        )

        AlertDialog.Builder(this)
            .setTitle(if (hindi) "स्कैन रिपोर्ट" else "Scan Reports")
            .setMessage(report)
            .setPositiveButton("OK", null)
            .setNeutralButton(
                if (hindi) "साफ करें" else "Clear"
            ) { _, _ ->
                prefs.edit().clear().apply()
                reports.text = if (hindi)
                    "कोई रिपोर्ट नहीं" else "No reports available"
            }
            .show()
    }

    private fun saveReport(value: String) {
        val timestamp = java.text.SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        prefs.edit()
            .putString("latest", "$timestamp\n\n$value")
            .apply()

        if (::reports.isInitialized) {
            reports.text = "$timestamp\n\n$value"
        }
    }

    private fun hasValidatedInternet(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        )
    }

    private fun isVpnActive(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    private fun message(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show()
    }

    private fun label(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean = false,
        alignment: Int = Gravity.CENTER
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            gravity = alignment
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(3, 4, 3, 4)
        }
    }

    private fun shape(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius * resources.displayMetrics.density
        }
    }

    private fun matchWidth() =
        LinearLayout.LayoutParams(-1, -2)

    private fun space(height: Int) {
        root.addView(
            View(this),
            LinearLayout.LayoutParams(1, height)
        )
    }
}

