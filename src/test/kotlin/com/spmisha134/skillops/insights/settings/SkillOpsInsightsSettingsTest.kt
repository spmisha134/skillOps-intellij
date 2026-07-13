package com.spmisha134.skillops.insights.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillOpsInsightsSettingsTest {
    @Test
    fun `defaults match insights data contract`() {
        val settings = SkillOpsInsightsSettings()

        assertEquals("~/.codex", settings.codexHomePath)
        assertEquals(200, settings.maxSessionsToScan)
        assertEquals(true, settings.autoRefreshEnabled)
        assertEquals(10, settings.refreshIntervalSeconds)
        assertEquals(30, settings.completedRunQuietPeriodSeconds)
        assertEquals(120, settings.fallbackCompletedRunQuietPeriodSeconds)
        assertEquals(50_000, settings.largeOutputWarningBytes)
        assertEquals(250_000, settings.highOutputWarningBytes)
        assertEquals(10, settings.manySearchesThreshold)
        assertEquals(true, settings.showStatusBarWidget)
        assertEquals(true, settings.enableSuggestions)
    }

    @Test
    fun `valid settings have no validation errors`() {
        val errors = SkillOpsInsightsSettings().validationErrors()

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validation rejects invalid numeric values`() {
        val errors = SkillOpsInsightsSettings(
            maxSessionsToScan = 0,
            refreshIntervalSeconds = 4,
            largeOutputWarningBytes = 0,
            highOutputWarningBytes = -1,
            manySearchesThreshold = 0,
        ).validationErrors()

        assertEquals(5, errors.size)
        assertTrue(errors.any { it.contains("Maximum sessions") })
        assertTrue(errors.any { it.contains("Refresh interval") })
        assertTrue(errors.any { it.contains("Large output") })
        assertTrue(errors.any { it.contains("High output") })
        assertTrue(errors.any { it.contains("Many searches") })
    }

    @Test
    fun `normalization repairs persisted invalid values`() {
        val normalized = SkillOpsInsightsSettings(
            codexHomePath = "",
            maxSessionsToScan = -1,
            refreshIntervalSeconds = 1,
            completedRunQuietPeriodSeconds = -10,
            fallbackCompletedRunQuietPeriodSeconds = -20,
            largeOutputWarningBytes = -30,
            highOutputWarningBytes = -40,
            manySearchesThreshold = -50,
        ).normalized()

        assertEquals("~/.codex", normalized.codexHomePath)
        assertEquals(1, normalized.maxSessionsToScan)
        assertEquals(5, normalized.refreshIntervalSeconds)
        assertEquals(0, normalized.completedRunQuietPeriodSeconds)
        assertEquals(0, normalized.fallbackCompletedRunQuietPeriodSeconds)
        assertEquals(1, normalized.largeOutputWarningBytes)
        assertEquals(1, normalized.highOutputWarningBytes)
        assertEquals(1, normalized.manySearchesThreshold)
    }

    @Test
    fun `state normalizes loaded persisted settings`() {
        val state = SkillOpsInsightsSettingsState()

        state.loadState(SkillOpsInsightsSettings(maxSessionsToScan = 0))

        assertEquals(1, state.settings.maxSessionsToScan)
    }
}
