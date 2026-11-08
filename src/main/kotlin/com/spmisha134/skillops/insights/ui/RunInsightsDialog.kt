package com.spmisha134.skillops.insights.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.table.JBTable
import com.spmisha134.skillops.insights.presentation.RunInsightsReportFormatter
import com.spmisha134.skillops.insights.presentation.InsightMetric
import com.spmisha134.skillops.insights.run.SkillOpsRunInsightsReport
import com.spmisha134.skillops.insights.run.SkillRunInsight
import com.spmisha134.skillops.presentation.NotificationPresenter
import com.spmisha134.skillops.sessions.terminal.CodexSessionTerminalLauncher
import com.spmisha134.skillops.sessions.terminal.ClaudeSessionTerminalLauncher
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel

class RunInsightsDialog(
    private val project: Project,
    private val report: SkillOpsRunInsightsReport,
    private val formatter: RunInsightsReportFormatter,
    private val terminalLauncher: CodexSessionTerminalLauncher = CodexSessionTerminalLauncher(),
    private val claudeTerminalLauncher: ClaudeSessionTerminalLauncher = ClaudeSessionTerminalLauncher(),
) : DialogWrapper(project) {
    private var selectedInsight: SkillRunInsight? = null
    private val modelOverride = ComboBox<String>()
    private val tokenHelp = JBLabel(
        "Token values describe recorded session usage. Context capacity is approximate and is not account quota.",
    )
    private val textArea = JBTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
    }
    private val metricTable = JBTable(MetricTableModel()).apply {
        columnModel.getColumn(0).preferredWidth = 100
        columnModel.getColumn(1).preferredWidth = 250
        columnModel.getColumn(2).preferredWidth = 150
        columnModel.getColumn(3).preferredWidth = 420
    }
    private val skillSelector = ComboBox(
        DefaultComboBoxModel(
            report.insights
                .flatMap(formatter::skillNames)
                .distinctBy(String::lowercase)
                .toTypedArray()
        )
    ).apply {
        isEnabled = report.insights.isNotEmpty()
        addActionListener { refreshRuns() }
    }
    private val runSelector = ComboBox<InsightOption>().apply {
        isEnabled = report.insights.isNotEmpty()
        addActionListener { showSelectedInsight() }
    }
    private val resumeButton = JButton("Resume Session").apply {
        addActionListener { resumeSelectedSession() }
    }
    private val copySessionIdButton = JButton("Copy Session ID").apply {
        addActionListener {
            selectedInsight?.resumeTarget?.sessionId?.let { sessionId ->
                CopyPasteManager.getInstance().setContents(StringSelection(sessionId))
            }
        }
    }

    init {
        title = "SkillOps ${report.platformName} Run Insights"
        modelOverride.model = DefaultComboBoxModel(
            arrayOf(DEFAULT_MODEL_OPTION) + report.insights.mapNotNull { it.tokenUsage?.modelName ?: it.resumeTarget?.modelName }
                .distinct()
                .toTypedArray()
        )
        modelOverride.isEditable = false
        refreshRuns()
        init()
    }

    override fun createCenterPanel(): JComponent =
        JBPanel<JBPanel<*>>(BorderLayout(0, 8)).apply {
            preferredSize = Dimension(1_000, 650)
            add(
                JBPanel<JBPanel<*>>(java.awt.GridLayout(3, 1, 0, 6)).apply {
                    add(JBPanel<JBPanel<*>>(BorderLayout(8, 0)).apply {
                        add(JBLabel("Skill:"), BorderLayout.WEST)
                        add(skillSelector, BorderLayout.CENTER)
                    })
                    add(JBPanel<JBPanel<*>>(BorderLayout(8, 0)).apply {
                        add(JBLabel("Run:"), BorderLayout.WEST)
                        add(runSelector, BorderLayout.CENTER)
                    })
                    add(JBPanel<JBPanel<*>>(BorderLayout(8, 0)).apply {
                        add(JBLabel("Model:"), BorderLayout.WEST)
                        add(modelOverride, BorderLayout.CENTER)
                    })
                },
                BorderLayout.NORTH,
            )
            add(JBPanel<JBPanel<*>>(BorderLayout(0, 8)).apply {
                add(JBScrollPane(metricTable), BorderLayout.CENTER)
                add(JBPanel<JBPanel<*>>(BorderLayout(0, 4)).apply {
                    add(tokenHelp, BorderLayout.NORTH)
                    add(JBScrollPane(textArea).apply {
                        preferredSize = Dimension(0, 140)
                    }, BorderLayout.CENTER)
                }, BorderLayout.SOUTH)
            }, BorderLayout.CENTER)
            add(JBPanel<JBPanel<*>>(java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0)).apply {
                add(copySessionIdButton)
                add(resumeButton)
            }, BorderLayout.SOUTH)
        }

    private fun showSelectedInsight() {
        val insight = (runSelector.selectedItem as? InsightOption)
            ?.index
            ?.let(report.insights::getOrNull)
        selectedInsight = insight
        modelOverride.selectedItem = insight?.resumeTarget?.modelName ?: insight?.tokenUsage?.modelName ?: DEFAULT_MODEL_OPTION
        modelOverride.isEnabled = insight?.resumeTarget != null
        val model = metricTable.model as MetricTableModel
        model.rows = insight?.let { formatter.metrics(it, report.platformName) }.orEmpty()
        model.fireTableDataChanged()
        textArea.text = insight?.let(::notesFor) ?: report.warnings.joinToString("\n")
        textArea.caretPosition = 0
        val resumable = selectedInsight?.resumeTarget != null
        resumeButton.isVisible = report.platformName == "Codex" || report.platformName == "Claude"
        copySessionIdButton.isVisible = report.platformName == "Codex" || report.platformName == "Claude"
        resumeButton.isEnabled = resumable
        copySessionIdButton.isEnabled = resumable
    }

    private fun refreshRuns() {
        val selectedSkill = skillSelector.selectedItem as? String
        val options = report.insights.mapIndexedNotNull { index, insight ->
            val hasSelectedSkill = selectedSkill == null || formatter.skillNames(insight)
                .any { it.equals(selectedSkill, ignoreCase = true) }
            if (hasSelectedSkill) InsightOption(index, formatter.runLabel(insight)) else null
        }
        runSelector.model = DefaultComboBoxModel(options.toTypedArray())
        runSelector.isEnabled = options.isNotEmpty()
        if (options.isNotEmpty()) {
            runSelector.selectedIndex = 0
        }
        showSelectedInsight()
    }

    private fun resumeSelectedSession() {
        val target = selectedInsight?.resumeTarget ?: return
        val projectRoot = project.basePath?.let(Path::of)
            ?: return NotificationPresenter.showError(project, "No project base path is available.")
        try {
            val model = (modelOverride.selectedItem as? String)?.takeUnless { it == DEFAULT_MODEL_OPTION }
            val targetWithModel = target.copy(modelName = model)
            if (report.platformName == "Claude") {
                claudeTerminalLauncher.resume(project, targetWithModel, projectRoot)
            } else {
                terminalLauncher.resume(project, targetWithModel, projectRoot)
            }
            close(OK_EXIT_CODE)
        } catch (error: Throwable) {
            NotificationPresenter.showError(
                project,
                "Could not resume ${report.platformName} session: ${error.message ?: error.javaClass.simpleName}",
            )
        }
    }

    private fun notesFor(insight: SkillRunInsight): String = buildString {
        if (insight.prompts.isNotEmpty()) {
            appendLine("Prompts")
            insight.prompts.forEachIndexed { index, prompt -> appendLine("${index + 1}. $prompt") }
        }
        if (insight.warnings.isNotEmpty()) {
            if (isNotEmpty()) appendLine()
            appendLine("Notes")
            insight.warnings.forEach { appendLine("- $it") }
        }
        if (isEmpty()) append("No additional notes.")
    }

    private class MetricTableModel(var rows: List<InsightMetric> = emptyList()) : AbstractTableModel() {
        private val columns = arrayOf("Section", "Metric", "Value", "What it means")
        override fun getRowCount(): Int = rows.size
        override fun getColumnCount(): Int = columns.size
        override fun getColumnName(column: Int): String = columns[column]
        override fun getValueAt(row: Int, column: Int): Any = rows[row].let {
            when (column) { 0 -> it.section; 1 -> it.name; 2 -> it.value; else -> it.explanation }
        }
    }

    private data class InsightOption(
        val index: Int,
        val label: String,
    ) {
        override fun toString(): String = label
    }

    companion object {
        private const val DEFAULT_MODEL_OPTION = "Use configured default"
    }
}
