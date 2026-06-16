// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.agent.llm

import io.agents.pokeclaw.agent.langchain.http.OkHttpClientBuilderAdapter

internal object OpenRouterHeaders {
    const val BASE_URL = "https://openrouter.ai/api/v1"

    private const val APP_REFERER = "https://github.com/agents-io/PokeClaw"
    private const val APP_TITLE = "PokeClaw"

    fun applyTo(builder: OkHttpClientBuilderAdapter): OkHttpClientBuilderAdapter {
        return builder
            .addDefaultHeader("HTTP-Referer", APP_REFERER)
            .addDefaultHeader("X-OpenRouter-Title", APP_TITLE)
    }

    fun isOpenRouterBaseUrl(baseUrl: String): Boolean {
        val normalized = baseUrl.trim().trimEnd('/')
        return normalized.equals(BASE_URL, ignoreCase = true)
    }
}
