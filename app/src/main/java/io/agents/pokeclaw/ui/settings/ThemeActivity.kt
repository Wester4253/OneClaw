// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.ui.settings

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import io.agents.pokeclaw.R
import io.agents.pokeclaw.base.BaseActivity
import io.agents.pokeclaw.utils.KVUtils
import io.agents.pokeclaw.widget.CommonToolbar

class ThemeActivity : BaseActivity() {

    data class ThemeConfig(
        val id: String,
        val name: String,
        val isDark: Boolean,
        val bg: Int,
        val userBubble: Int,
        val aiBubble: Int,
        val avatar: Int,
        val inputBar: Int,
        val accent: Int
    )

    private val themes = listOf(
        ThemeConfig("ember_dark", "Ember", true, Color.parseColor("#151211"), Color.parseColor("#D45A30"), Color.parseColor("#342C28"), Color.parseColor("#C0542E"), Color.parseColor("#332B27"), Color.parseColor("#E8845A")),
        ThemeConfig("abyss_dark", "Abyss", true, Color.parseColor("#0C111B"), Color.parseColor("#2563EB"), Color.parseColor("#1E2D45"), Color.parseColor("#1D4ED8"), Color.parseColor("#1E293B"), Color.parseColor("#60A5FA")),
        ThemeConfig("moss_dark", "Moss", true, Color.parseColor("#0F1410"), Color.parseColor("#2D7A4F"), Color.parseColor("#243524"), Color.parseColor("#2D7A4F"), Color.parseColor("#233023"), Color.parseColor("#6EE7A0")),
        ThemeConfig("onyx_dark", "Onyx", true, Color.parseColor("#111111"), Color.parseColor("#444444"), Color.parseColor("#2C2C2C"), Color.parseColor("#444444"), Color.parseColor("#2A2A2A"), Color.parseColor("#999999")),
        ThemeConfig("graphite_dark", "Graphite", true, Color.parseColor("#101314"), Color.parseColor("#2F7A78"), Color.parseColor("#242A2D"), Color.parseColor("#9B6A3D"), Color.parseColor("#30383A"), Color.parseColor("#66D4C8")),
        ThemeConfig("orchid_dark", "Orchid", true, Color.parseColor("#171216"), Color.parseColor("#9B5C8F"), Color.parseColor("#30252E"), Color.parseColor("#B45B63"), Color.parseColor("#3A2D37"), Color.parseColor("#F08AA0")),
        ThemeConfig("ember_light", "Ember Light", false, Color.parseColor("#F5EDE5"), Color.parseColor("#C0542E"), Color.parseColor("#EAE0D4"), Color.parseColor("#C0542E"), Color.parseColor("#C8BAB0"), Color.parseColor("#C0542E")),
        ThemeConfig("abyss_light", "Abyss Light", false, Color.parseColor("#E8EDF4"), Color.parseColor("#2563EB"), Color.parseColor("#D5DFEE"), Color.parseColor("#2563EB"), Color.parseColor("#C8D4E4"), Color.parseColor("#2563EB")),
        ThemeConfig("moss_light", "Moss Light", false, Color.parseColor("#E4EFE4"), Color.parseColor("#2D7A4F"), Color.parseColor("#D0E8D0"), Color.parseColor("#2D7A4F"), Color.parseColor("#B8D0B8"), Color.parseColor("#2D7A4F")),
        ThemeConfig("onyx_light", "Onyx Light", false, Color.parseColor("#E8E8E8"), Color.parseColor("#444444"), Color.parseColor("#D8D8D8"), Color.parseColor("#444444"), Color.parseColor("#CCCCCC"), Color.parseColor("#555555")),
        ThemeConfig("graphite_light", "Graphite Light", false, Color.parseColor("#F2F4F1"), Color.parseColor("#2F7A78"), Color.parseColor("#DDE5E1"), Color.parseColor("#9B6A3D"), Color.parseColor("#BCC9C5"), Color.parseColor("#2F7A78")),
        ThemeConfig("orchid_light", "Orchid Light", false, Color.parseColor("#F5EEF3"), Color.parseColor("#9B5C8F"), Color.parseColor("#E7D7E2"), Color.parseColor("#B45B63"), Color.parseColor("#D8C3D2"), Color.parseColor("#9B5C8F")),
    )

    private var selectedThemeId = "ember_dark"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tc = io.agents.pokeclaw.ui.chat.ThemeManager.getColors()
        window.statusBarColor = tc.toolbarBg
        window.decorView.setBackgroundColor(tc.bg)

        setContentView(R.layout.activity_theme)

        val contentFrame = findViewById<android.view.ViewGroup>(android.R.id.content)
        contentFrame?.setBackgroundColor(tc.bg)
        (contentFrame?.getChildAt(0) as? android.view.View)?.setBackgroundColor(tc.bg)

        findViewById<CommonToolbar>(R.id.toolbar).apply {
            setTitle("Appearance")
            setTitleColor(tc.aiText)
            setBackgroundColor(tc.toolbarBg)
            showBackButton(true) { finish() }
            findViewById<android.widget.ImageView>(R.id.ivBack)?.setColorFilter(tc.aiText)
        }
        findViewById<TextView>(R.id.tvCurrentTheme)?.setTextColor(tc.aiText)
        findViewById<TextView>(R.id.tvDarkThemes)?.setTextColor(tc.aiText)
        findViewById<TextView>(R.id.tvLightThemes)?.setTextColor(tc.aiText)

        selectedThemeId = KVUtils.getString("THEME_ID", "ember_dark")

        val viewIds = themeViewIds()

        themes.forEachIndexed { index, theme ->
            val view = findViewById<View>(viewIds[index])
            setupThemePreview(view, theme)
        }

        updateSelection()
    }

    private fun setupThemePreview(view: View, theme: ThemeConfig) {
        val card = view.findViewById<LinearLayout>(R.id.cardPreview)
        val userBubble = view.findViewById<View>(R.id.previewUserBubble)
        val userBubble2 = view.findViewById<View>(R.id.previewUserBubble2)
        val aiBubble = view.findViewById<View>(R.id.previewAiBubble)
        val avatar = view.findViewById<View>(R.id.previewAvatar)
        val inputBar = view.findViewById<View>(R.id.previewInputBar)
        val name = view.findViewById<TextView>(R.id.tvThemeName)

        // Card background
        val cardBg = GradientDrawable().apply {
            setColor(theme.bg)
            cornerRadius = dp(8f)
        }
        card.background = cardBg

        // User bubble
        userBubble.background = roundRect(theme.userBubble, 8f)
        userBubble2.background = roundRect(theme.userBubble, 8f)

        // AI bubble
        aiBubble.background = roundRect(theme.aiBubble, 8f)

        // Avatar
        avatar.background = oval(theme.avatar)

        // Input bar
        inputBar.background = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke(dp(1).toInt(), theme.inputBar)
            cornerRadius = dp(6f)
        }

        name.text = theme.name
        name.setTextColor(if (theme.isDark) Color.parseColor("#D8D8D8") else Color.parseColor("#4A4A4A"))

        view.setOnClickListener {
            selectedThemeId = theme.id
            KVUtils.putString("THEME_ID", theme.id)

            // Use system uimode command (works on MIUI where AppCompatDelegate doesn't)
            try {
                val mode = if (theme.isDark) "yes" else "no"
                Runtime.getRuntime().exec(arrayOf("cmd", "uimode", "night", mode))
            } catch (_: Exception) {
                // Fallback to AppCompatDelegate
                val newMode = if (theme.isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(newMode)
            }

            // Restart app to apply theme everywhere
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun updateSelection() {
        val allViews = themeViewIds()

        themes.forEachIndexed { index, theme ->
            val view = findViewById<View>(allViews[index])
            val indicator = view.findViewById<View>(R.id.selectedIndicator)
            val isSelected = theme.id == selectedThemeId

            if (isSelected) {
                indicator.visibility = View.VISIBLE
                indicator.background = roundRect(theme.accent, 2f)
            } else {
                indicator.visibility = View.INVISIBLE
            }
        }

        val current = themes.find { it.id == selectedThemeId }
        val label = current?.name ?: selectedThemeId
        findViewById<TextView>(R.id.tvCurrentTheme).text = "Current: $label"
    }

    private fun themeViewIds(): List<Int> = listOf(
        R.id.themeEmberDark,
        R.id.themeAbyssDark,
        R.id.themeMossDark,
        R.id.themeOnyxDark,
        R.id.themeGraphiteDark,
        R.id.themeOrchidDark,
        R.id.themeEmberLight,
        R.id.themeAbyssLight,
        R.id.themeMossLight,
        R.id.themeOnyxLight,
        R.id.themeGraphiteLight,
        R.id.themeOrchidLight,
    )

    private fun roundRect(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius)
    }

    private fun oval(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
    private fun dp(v: Int): Float = v * resources.displayMetrics.density
}
