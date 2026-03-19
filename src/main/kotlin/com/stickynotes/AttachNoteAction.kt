package com.stickynotes

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class AttachNoteAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) {
            Messages.showInfoMessage(project, "Выдели участок кода сначала", "Sticky Notes")
            return
        }

        val startLine = editor.document.getLineNumber(selectionModel.selectionStart)
        val endLine = editor.document.getLineNumber(selectionModel.selectionEnd)
        val storage = StickyNoteStorage.getInstance(project)

        // Если уже есть заметка на этом диапазоне — открыть её для редактирования
        val existing = storage.findNoteAt(file.path, startLine)

        val dialog = NoteDialog(existing?.text ?: "")
        if (dialog.showAndGet()) {
            val text = dialog.getNoteText()
            if (text.isBlank()) {
                existing?.let { storage.deleteNote(it.id) }
            } else {
                val note = StickyNote(
                    id = existing?.id ?: StickyNoteStorage.newId(),
                    filePath = file.path,
                    startLine = startLine,
                    endLine = endLine,
                    text = text
                )
                storage.saveNote(note)
            }
            // Перерисовать gutter
            editor.component.repaint()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
    }
}

class NoteDialog(initialText: String) : DialogWrapper(true) {

    private val textArea = JBTextArea(initialText, 8, 40).apply {
        lineWrap = true
        wrapStyleWord = true
    }

    init {
        title = "Sticky Note"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val scroll = JBScrollPane(textArea).apply {
            preferredSize = Dimension(400, 200)
        }
        panel.add(scroll, BorderLayout.CENTER)
        return panel
    }

    override fun getPreferredFocusedComponent() = textArea

    fun getNoteText(): String = textArea.text.trim()
}
