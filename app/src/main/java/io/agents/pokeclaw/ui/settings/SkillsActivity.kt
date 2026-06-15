// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.agents.pokeclaw.R
import io.agents.pokeclaw.agent.skill.PromptSkillManager
import io.agents.pokeclaw.base.BaseActivity
import io.agents.pokeclaw.ui.chat.ThemeManager
import io.agents.pokeclaw.widget.CommonToolbar
import io.agents.pokeclaw.widget.MenuGroup
import java.io.File

class SkillsActivity : BaseActivity() {

    private lateinit var colors: ThemeManager.ChatColors
    private lateinit var skillsContainer: LinearLayout
    private lateinit var emptyView: TextView
    private lateinit var summaryView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colors = ThemeManager.getColors()
        window.statusBarColor = colors.toolbarBg
        window.decorView.setBackgroundColor(colors.bg)

        setContentView(R.layout.activity_skills)

        findViewById<View>(android.R.id.content)?.setBackgroundColor(colors.bg)
        findViewById<View>(R.id.rootContent)?.setBackgroundColor(colors.bg)
        summaryView = findViewById(R.id.tvSummary)
        skillsContainer = findViewById(R.id.skillsContainer)
        emptyView = findViewById(R.id.tvEmpty)

        initToolbar()
        initActions()
        initPaths()
        refreshSkillList()
    }

    private fun initToolbar() {
        findViewById<CommonToolbar>(R.id.toolbar).apply {
            setTitle("Skills")
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
            leadingIcon = android.R.drawable.ic_popup_sync,
            title = "Reload Skills",
            onClick = {
                PromptSkillManager.reload(this)
                refreshSkillList()
                Toast.makeText(this, "Skills reloaded", Toast.LENGTH_SHORT).show()
            },
            showDivider = true,
        )
        group.addMenuItem(
            leadingIcon = android.R.drawable.ic_menu_add,
            title = "Create Example Skill",
            onClick = { createExampleSkill() },
            showDivider = true,
        )
        group.addMenuItem(
            leadingIcon = android.R.drawable.ic_menu_edit,
            title = "Copy SKILL.md Template",
            onClick = {
                copyToClipboard("PokeClaw SKILL.md template", SAMPLE_SKILL)
                Toast.makeText(this, "Template copied", Toast.LENGTH_SHORT).show()
            },
            showDivider = false,
        )
        applyThemeToGroup(group)
    }

    private fun initPaths() {
        val group = findViewById<MenuGroup>(R.id.pathsGroup)
        group.setTitle("Watched Folders")
        val dirs = PromptSkillManager.getUserSkillDirectories(this)
        dirs.forEachIndexed { index, dir ->
            group.addMenuItem(
                leadingIcon = android.R.drawable.ic_menu_upload,
                title = dir.displayPath(),
                onClick = {
                    copyToClipboard("PokeClaw skills folder", dir.absolutePath)
                    Toast.makeText(this, "Path copied", Toast.LENGTH_SHORT).show()
                },
                trailingText = if (dir.exists()) "Ready" else "Missing",
                showDivider = index != dirs.lastIndex,
            )
        }
        applyThemeToGroup(group)
    }

    private fun refreshSkillList() {
        val skills = PromptSkillManager.getAll()
        summaryView.setTextColor(colors.aiText)
        summaryView.text = "${skills.size} loaded. Put Claude-style skills in a watched folder as <skill-id>/SKILL.md."
        findViewById<TextView>(R.id.tvSkillsHeader)?.setTextColor(colors.aiText)

        skillsContainer.removeAllViews()
        emptyView.setTextColor(colors.aiText)
        emptyView.background = rounded(colors.toolbarBg, 8f, colors.divider)
        emptyView.visibility = if (skills.isEmpty()) View.VISIBLE else View.GONE

        skills.forEach { skill ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = rounded(colors.toolbarBg, 8f, colors.divider)
                setPadding(dp(14), dp(12), dp(14), dp(12))
                isClickable = true
                foreground = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).let {
                    val drawable = it.getDrawable(0)
                    it.recycle()
                    drawable
                }
                setOnClickListener {
                    copyToClipboard("PokeClaw skill source", skill.source)
                    Toast.makeText(this@SkillsActivity, "Skill source copied", Toast.LENGTH_SHORT).show()
                }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(dp(16), dp(6), dp(16), dp(6))
            }
            card.layoutParams = params

            card.addView(text(skill.name, 16f, colors.aiText, Typeface.BOLD))
            card.addView(text(skill.id, 12f, colors.sendColor, Typeface.NORMAL))
            card.addView(text(skill.description, 13f, colors.aiText, Typeface.NORMAL).apply {
                alpha = 0.82f
                setPadding(0, dp(8), 0, 0)
            })
            val tools = if (skill.allowedTools.isEmpty()) "Tools: any available tool" else "Tools: ${skill.allowedTools.joinToString(", ")}"
            card.addView(text(tools, 12f, colors.aiText, Typeface.NORMAL).apply {
                alpha = 0.62f
                setPadding(0, dp(8), 0, 0)
            })
            card.addView(text(skill.source, 11f, colors.aiText, Typeface.NORMAL).apply {
                alpha = 0.48f
                setPadding(0, dp(6), 0, 0)
            })
            skillsContainer.addView(card)
        }
    }

    private fun createExampleSkill() {
        val root = getExternalFilesDir(null)?.let { File(it, "skills") }
            ?: File(filesDir, "skills")
        val skillDir = File(root, "example-open-youtube")
        val skillFile = File(skillDir, "SKILL.md")
        skillDir.mkdirs()
        if (!skillFile.exists()) {
            skillFile.writeText(SAMPLE_SKILL)
        }
        PromptSkillManager.reload(this)
        refreshSkillList()
        copyToClipboard("Example skill path", skillFile.absolutePath)
        Toast.makeText(this, "Example created and path copied", Toast.LENGTH_LONG).show()
    }

    private fun applyThemeToGroup(group: MenuGroup) {
        group.setTitleColor(colors.aiText)
        group.setCardBackgroundColor(colors.toolbarBg)
        for (i in 0 until group.getMenuItemCount()) {
            group.getMenuItemAt(i)?.apply {
                setTitleColor(colors.aiText)
                setTrailingTextColor(colors.sendColor)
                setLeadingIconColor(colors.aiText)
                setTrailingIconColor(colors.aiText)
            }
        }
    }

    private fun text(value: String, sizeSp: Float, color: Int, style: Int): TextView =
        TextView(this).apply {
            text = value
            textSize = sizeSp
            setTextColor(color)
            typeface = Typeface.create(Typeface.DEFAULT, style)
        }

    private fun copyToClipboard(label: String, value: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    }

    private fun File.displayPath(): String =
        absolutePath
            .replace("/storage/emulated/0", "/sdcard")
            .replace("${getExternalFilesDir(null)?.absolutePath}", "App files")
            .replace(filesDir.absolutePath, "Private files")

    private fun rounded(color: Int, radius: Float, stroke: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val SAMPLE_SKILL = """---
name: Open YouTube
description: Use when the user asks to open YouTube or launch the YouTube app.
allowed-tools: open_app, finish
---

# Open YouTube

Open YouTube quickly.

## Steps

1. Confirm the user wants YouTube opened.
2. Use `open_app` to launch YouTube.
3. Use `finish` to confirm YouTube is open.
"""
    }
}
