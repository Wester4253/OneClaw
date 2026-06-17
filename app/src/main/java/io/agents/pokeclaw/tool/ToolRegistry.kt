// Copyright 2026 OneClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.tool

import io.agents.pokeclaw.agent.knowledge.*
import io.agents.pokeclaw.tool.impl.*
import io.agents.pokeclaw.tool.impl.mobile.*
import io.agents.pokeclaw.tool.impl.tv.*
import io.agents.pokeclaw.utils.KVUtils

object ToolRegistry {

    enum class DeviceType { TV, MOBILE }

    private val tools = LinkedHashMap<String, BaseTool>()
    private val requiredTools = setOf("finish", "get_screen_info", "wait")
    var deviceType: DeviceType = DeviceType.TV
        private set

    @JvmStatic
    fun getInstance(): ToolRegistry = this

    fun registerAllTools(type: DeviceType = DeviceType.TV) {
        deviceType = type
        tools.clear()
        registerCommonTools()
        when (type) {
            DeviceType.TV -> registerTvTools()
            DeviceType.MOBILE -> registerMobileTools()
        }
    }

    private fun registerCommonTools() {
        register(GetScreenInfoTool())
        register(FindNodeInfoTool())
        register(InputTextTool())
        register(SystemKeyTool())
        register(OpenAppTool())
        register(GetInstalledAppsTool())
        register(TakeScreenshotTool())
        register(WaitTool())
        register(RepeatActionsTool())
        register(ClipboardTool())
        register(SendFileTool())
        register(GetDeviceInfoTool())
        register(GetNotificationsTool())
        register(MakeCallTool())
        register(FinishTool())
        // Knowledge Base tools — shared vault available in all modes
        register(KbWriteTool())
        register(KbReadTool())
        register(KbSearchTool())
        register(KbAppendTool())
        register(KbAddTodoTool())
    }

    private fun registerTvTools() {
        register(DpadUpTool())
        register(DpadDownTool())
        register(DpadLeftTool())
        register(DpadRightTool())
        register(DpadCenterTool())
        register(VolumeUpTool())
        register(VolumeDownTool())
        register(PressMenuTool())
        register(PressPowerTool())
    }

    private fun registerMobileTools() {
        register(TapTool())
        register(TapNodeTool())
        register(LongPressTool())
        register(SwipeTool())
        register(ScrollToFindTool())
        register(FindAndTapTool())
        register(SendMessageTool())
        register(AutoReplyTool())
    }

    fun register(tool: BaseTool) {
        tools[tool.getName()] = tool
    }

    fun getTool(name: String): BaseTool? = tools[name]?.takeIf { isToolEnabled(name) }

    fun getDisplayName(name: String): String = tools[name]?.getDisplayName() ?: name

    fun getAllTools(): List<BaseTool> = tools.values.filter { isToolEnabled(it.getName()) }

    fun getAllRegisteredTools(): List<BaseTool> = tools.values.toList()

    fun isRequiredTool(name: String): Boolean = name in requiredTools

    fun isToolEnabled(name: String): Boolean {
        return isRequiredTool(name) || KVUtils.isToolEnabled(name)
    }

    fun setToolEnabled(name: String, enabled: Boolean) {
        if (!isRequiredTool(name)) {
            KVUtils.setToolEnabled(name, enabled)
        }
    }

    fun enableAllTools() {
        KVUtils.setAllToolsEnabled()
    }

    fun executeTool(name: String, params: Map<String, Any>): ToolResult {
        val tool = tools[name] ?: return ToolResult.error("Unknown tool: $name")
        if (!isToolEnabled(name)) {
            return ToolResult.error("Tool disabled in Settings: ${tool.getDisplayName()}")
        }
        return try {
            tool.executeWithWaitAfter(params)
        } catch (e: Exception) {
            io.agents.pokeclaw.utils.XLog.e("ToolRegistry", "Tool '$name' execution failed with params=$params", e)
            ToolResult.error("Tool execution failed: ${e.message}")
        }
    }
}
