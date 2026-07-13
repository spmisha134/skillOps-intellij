package com.spmisha134.skillops.insights.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.Dimension
import javax.swing.JComponent

class RunInsightsDialog(
    project: Project,
    reportText: String,
) : DialogWrapper(project) {
    private val textArea = JBTextArea(reportText).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }

    init {
        title = "SkillOps Run Insights"
        init()
    }

    override fun createCenterPanel(): JComponent =
        JBScrollPane(textArea).apply {
            preferredSize = Dimension(720, 420)
        }
}
