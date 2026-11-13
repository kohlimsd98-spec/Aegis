package com.aegis.security

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private var hindi = false
    private lateinit var root: LinearLayout

    private val bg = Color.rgb(9, 16, 32)
    private val panel = Color.rgb(19, 31, 53)
    private val blue = Color.rgb(45, 137, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(165, 180, 205)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        render()
    }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 20)
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        brand.addView(label("AEGIS", 27, white, true))
        brand.addView(label("MOBILE SECURITY", 11, muted, true))

        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        val lang = Button(this).apply {
            text = if (hindi) "हिंदी" else "EN / HI"
            textSize = 12f
            setTextColor(white)
            background = shape(blue, 18)
            setOnClickListener {
                hindi = !hindi
                render()
            }
        }
        header.addView(lang)
        root.addView(header)

        space(24)
        root.addView(label(
            if (hindi) "आपका सुरक्षा केंद्र" else "Your Security Center",
            23, white, true
        ))
        root.addView(label(
            if (hindi) "आपके डिवाइस की सुरक्षा का अवलोकन" else
                "Your device protection overview",
            13, muted
        ))

        space(20)
        val score = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 24, 20, 24)
            background = shape(panel, 22)
        }

        score.addView(label("🛡️", 36, white))
        score.addView(label(
            if (hindi) "डिवाइस सुरक्षा स्थिति" else "DEVICE SECURITY STATUS",
            13, muted, true
        ))
        score.addView(label(
            if (hindi) "जांच की प्रतीक्षा में" else "Awaiting security check",
            21, white, true
        ))
        score.addView(label(
            if (hindi) "अभी पूर्ण सुरक्षा स्कैन नहीं हुआ है" else
                "A full security scan has not been performed",
            12, muted
        ))
        root.addView(score, matchWidth())

        space(22)
        root.addView(label(
            if (hindi) "त्वरित सुरक्षा उपकरण" else "QUICK SECURITY TOOLS",
            14, muted, true
        ))
        space(10)

        val tools = listOf(
            arrayOf("🔍", "Quick Scan", "त्वरित स्कैन"),
            arrayOf("📁", "File Scanner", "फाइल स्कैनर"),
            arrayOf("🌐", "Web Protection", "वेब सुरक्षा"),
            arrayOf("🔐", "Privacy Guard", "प्राइवेसी सुरक्षा"),
            arrayOf("📶", "Network Security", "नेटवर्क सुरक्षा"),
            arrayOf("📋", "Scan Reports", "स्कैन रिपोर्ट")
        )

        for (i in tools.indices step 2) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            row.addView(toolCard(tools[i]), LinearLayout.LayoutParams(0, 135, 1f))
            val second = if (i + 1 < tools.size) tools[i + 1] else tools[i]
            row.addView(toolCard(second), LinearLayout.LayoutParams(0, 135, 1f))
            root.addView(row)
            space(10)
        }

        space(12)
        root.addView(label(
            if (hindi) "सुरक्षा जानकारी" else "SECURITY INFORMATION",
            14, muted, true
        ))
        space(8)

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            background = shape(panel, 18)
        }

        info.addView(label(
            if (hindi) "यह Aegis का प्रारंभिक डैशबोर्ड है।" else
                "This is the initial Aegis dashboard.",
            14, white, true
        ))
        info.addView(label(
            if (hindi)
                "स्कैनिंग और सुरक्षा मॉड्यूल अगले चरणों में जोड़े जाएंगे। अभी यह स्क्रीन वास्तविक मैलवेयर सुरक्षा प्रदान नहीं करती।"
            else
                "Scanning and protection modules will be added in upcoming phases. This screen does not yet provide real malware protection.",
            12, muted
        ))
        root.addView(info, matchWidth())

        space(20)
        root.addView(label(
            "AEGIS MOBILE SECURITY  •  VERSION 2.0",
            10, muted, true
        ))
    }

    private fun toolCard(item: Array<String>): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
            background = shape(panel, 18)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    if (hindi) "यह मॉड्यूल अगले चरण में सक्रिय होगा"
                    else "This module will be activated in a future phase",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        card.addView(label(item[0], 27, white))
        card.addView(label(
            if (hindi) item[2] else item[1],
            14, white, true
        ))
        card.addView(label(
            if (hindi) "जल्द आ रहा है" else "Coming soon",
            10, muted
        ))
        return card
    }

    private fun label(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(2, 3, 2, 3)
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
        root.addView(View(this), LinearLayout.LayoutParams(1, height))
    }
}
