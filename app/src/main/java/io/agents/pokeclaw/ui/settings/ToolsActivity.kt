// Copyright 2026 OneClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import io.agents.pokeclaw.R
import io.agents.pokeclaw.base.BaseActivity
import io.agents.pokeclaw.tool.BaseTool
import io.agents.pokeclaw.tool.ToolRegistry
import io.agents.pokeclaw.ui.chat.ThemeManager
import io.agents.pokeclaw.widget.CommonToolbar
import io.agents.pokeclaw.widget.MenuGroup

class ToolsActivity : BaseActivity() {

    private lateinit var colors: ThemeManager.ChatColors
    private lateinit var summaryView: TextView
    private lateinit var toolsContainer: LinearLayout
    private val registry = ToolRegistry.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colors = ThemeManager.getColors()
        window.statusBarColor = colors.toolbarBg
        window.decorView.setBackgroundColor(colors.bg)

        setContentView(R.layout.activity_tools)

        findViewById<View>(android.R.id.content)?.setBackgroundColor(colors.bg)
        findViewById<View>(R.id.rootContent)?.setBackgroundColor(colors.bg)
        summaryView = findViewById(R.id.tvSummary)
        toolsContainer = findViewById(R.id.toolsContainer)

        initToolbar()
        initActions()
        refreshToolList()
    }

    override fun onResume() {
        super.onResume()
        refreshToolList()
    }

    private fun initToolbar() {
        findViewById<CommonToolbar>(R.id.toolbar).apply {
            setTitle("Tools")
            setTitleColor(colors.aiText)
            setBackgroundColor(colors.toolbarBg)
            showBackButton(true) { finish() }
            findViewById<android.widget.ImageView>(R.id.ivBack)?.setColorFilter(colors.aiText)
        }
    }

    private fun initActions() {
        val group = findViewById<MenuGroup>(R.id.actionsGroup)
        group.setTitle("Management")
        group.addMenuItem(
            leadingIcon = android.R.drawable.ic_menu_revert,
            title = "Enable All Tools",
            onClick = {
                registry.enableAllTools()
                refreshToolList()
                Toast.makeText(this, "All optional tools enabled", Toast.LENGTH_SHORT).show()
            },
            showDivider = true,
        )
        group.addMenuItem(
            leadingIcon = android.R.drawable.ic_menu_save,
            title = "Copy Enabled Tool IDs",
            onClick = {
                val ids = registry.getAllTools().joinToString("\n") { it.getName() }
                copyToClipboard("OneClaw enabled tools", ids)
                Toast.makeText(this, "Enabled tool IDs copied", Toast.LENGTH_SHORT).show()
            },
            showDivider = false,
        )
        applyThemeToGroup(group)
    }

    private fun refreshToolList() {
        val allTools = registry.getAllRegisteredTools()
            .sortedWith(compareBy<BaseTool> { categoryOrder(categoryFor(it.getName())) }
                .thenBy { it.getDisplayName().lowercase() })
        val enabledCount = registry.getAllTools().size

        summaryView.setTextColor(colors.aiText)
        summaryView.text = "$enabledCount of ${allTools.size} tools enabled. Disabled tools are hidden from model tool specs and blocked at execution time."
        findViewById<TextView>(R.id.tvToolsHeader)?.setTextColor(colors.aiText)

        toolsContainer.removeAllViews()
        var currentCategory: String? = null
        allTools.forEach { tool ->
            val category = categoryFor(tool.getName())
            if (category != currentCategory) {
                currentCategory = category
                toolsContainer.addView(categoryHeader(category))
            }
            toolsContainer.addView(toolCard(tool))
        }
    }

    private fun toolCard(tool: BaseTool): View {
        val name = tool.getName()
        val required = registry.isRequiredTool(name)
        val enabled = registry.isToolEnabled(name)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = rounded(colors.toolbarBg, 18f, colors.divider)
            setPadding(dp(16), dp(14), dp(12), dp(14))
            isClickable = true
            foreground = selectableForeground()
            alpha = if (enabled) 1f else 0.58f
            setOnClickListener {
                copyToClipboard("OneClaw tool", name)
                Toast.makeText(this@ToolsActivity, "Tool ID copied: $name", Toast.LENGTH_SHORT).show()
            }
        }
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(dp(16), dp(5), dp(16), dp(5))
        }

        val textColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        textColumn.addView(text(tool.getDisplayName(), 15f, colors.aiText, Typeface.BOLD))
        textColumn.addView(text(name, 11f, colors.sendColor, Typeface.NORMAL).apply {
            setPadding(0, dp(2), 0, 0)
        })
        textColumn.addView(text(tool.getDescription(), 12f, colors.aiText, Typeface.NORMAL).apply {
            alpha = 0.72f
            setPadding(0, dp(7), 0, 0)
        })
        val params = tool.getParametersWithWaitAfter()
        val detail = if (required) {
            "Required core tool"
        } else if (params.isEmpty()) {
            "No parameters"
        } else {
            "${params.count { it.isRequired }} required, ${params.size} total parameters"
        }
        textColumn.addView(text(detail, 11f, colors.aiText, Typeface.NORMAL).apply {
            alpha = 0.48f
            setPadding(0, dp(7), 0, 0)
        })

        val toggle = SwitchCompat(this).apply {
            isChecked = enabled
            isEnabled = !required
            alpha = if (required) 0.42f else 1f
            setOnCheckedChangeListener { _, isChecked ->
                registry.setToolEnabled(name, isChecked)
                refreshSummaryOnly()
                row.alpha = if (isChecked) 1f else 0.58f
            }
        }
        toggle.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            marginStart = dp(12)
        }

        row.addView(textColumn)
        row.addView(toggle)
        return row
    }

    private fun refreshSummaryOnly() {
        val enabledCount = registry.getAllTools().size
        val allCount = registry.getAllRegisteredTools().size
        summaryView.text = "$enabledCount of $allCount tools enabled. Disabled tools are hidden from model tool specs and blocked at execution time."
    }

    private fun categoryHeader(category: String): TextView {
        return text(category, 12f, colors.aiText, Typeface.BOLD).apply {
            alpha = 0.68f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(dp(24), dp(14), dp(24), dp(3))
            }
        }
    }

    private fun categoryFor(name: String): String = when {
        name.startsWith("kb_") -> "Knowledge"
        name in setOf("send_message", "send_file", "make_call", "auto_reply") -> "Messaging"
        name in setOf("get_screen_info", "find_node_info", "take_screenshot", "get_installed_apps", "get_device_info", "get_notifications", "clipboard") -> "Observation"
        name in setOf("open_app", "input_text", "tap", "tap_node", "long_press", "swipe", "scroll_to_find", "find_and_tap", "system_key", "repeat_actions", "wait", "finish") -> "Control"
        name.contains("dpad") || name.contains("volume") || name.startsWith("press_") -> "TV"
        else -> "Other"
    }

    private fun categoryOrder(category: String): Int = when (category) {
        "Observation" -> 0
        "Control" -> 1
        "Messaging" -> 2
        "Knowledge" -> 3
        "TV" -> 4
        else -> 5
    }

    private fun applyThemeToGroup(group: MenuGroup) {
        group.setTitleColor(colors.aiText)
        group.setCardBackgroundColor(colors.toolbarBg)
    }

    private fun text(value: String, sp: Float, color: Int, style: Int): TextView {
        return TextView(this).apply {
            text = value
            textSize = sp
            setTextColor(color)
            typeface = Typeface.DEFAULT_BOLD.takeIf { style == Typeface.BOLD } ?: Typeface.DEFAULT
            setLineSpacing(0f, 1.08f)
        }
    }

    private fun rounded(color: Int, radiusDp: Float, strokeColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(colorWithAlpha(color, 230))
            cornerRadius = dp(radiusDp)
            setStroke(dp(1f).toInt(), colorWithAlpha(strokeColor, 170))
        }
    }

    private fun selectableForeground(): android.graphics.drawable.Drawable? {
        val attrs = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
        return attrs.getDrawable(0).also { attrs.recycle() }
    }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    }

    private fun colorWithAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
