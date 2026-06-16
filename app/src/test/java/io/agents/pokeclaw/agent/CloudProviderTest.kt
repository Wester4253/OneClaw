// Copyright 2026 PokeClaw (agents.io). All rights reserved.
// Licensed under the Apache License, Version 2.0.

package io.agents.pokeclaw.agent

import io.agents.pokeclaw.agent.llm.CloudModelConfig
import io.agents.pokeclaw.agent.llm.OpenRouterHeaders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudProviderTest {

    @Test
    fun openRouterProvider_hasOfficialBaseUrlAndCuratedModels() {
        val provider = CloudProvider.OPENROUTER

        assertEquals(OpenRouterHeaders.BASE_URL, provider.defaultBaseUrl)
        assertFalse(provider.showBaseUrl)
        assertTrue(provider.models.any { it.id == "google/gemini-2.5-flash" })
        assertSame(provider, CloudProvider.findProviderForModel("google/gemini-2.5-flash"))
    }

    @Test
    fun cloudModelConfig_mapsOpenRouterToOpenRouterRuntimeProvider() {
        val config = CloudModelConfig(
            providerName = "OPENROUTER",
            modelName = "google/gemini-2.5-flash",
            baseUrl = "",
            apiKey = "sk-or-test"
        )

        assertEquals(CloudProvider.OPENROUTER, config.provider)
        assertEquals(OpenRouterHeaders.BASE_URL, config.resolvedBaseUrl)
        assertEquals(LlmProvider.OPENROUTER, config.agentProvider)
    }

    @Test
    fun modelPricing_supportsOpenRouterPrefixedModelIds() {
        val cost = ModelPricing.estimateCost(
            model = "openai/gpt-5.2",
            inputTokens = 1_000_000,
            outputTokens = 1_000_000
        )

        assertEquals(15.75, cost, 0.0001)
    }
}
