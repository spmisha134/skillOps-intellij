package com.spmisha134.skillops.insights.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(
    name = "SkillOpsInsightsSettingsState",
    storages = [Storage("skillops-insights.xml")]
)
class SkillOpsInsightsSettingsState : PersistentStateComponent<SkillOpsInsightsSettings> {
    var settings: SkillOpsInsightsSettings = SkillOpsInsightsSettings()

    override fun getState(): SkillOpsInsightsSettings = settings

    override fun loadState(state: SkillOpsInsightsSettings) {
        settings = state.normalized()
    }

    companion object {
        fun getInstance(): SkillOpsInsightsSettingsState = service()
    }
}
