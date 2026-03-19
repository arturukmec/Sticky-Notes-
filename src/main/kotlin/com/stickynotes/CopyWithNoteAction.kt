package com.stickynotes

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection

class CopyWithNoteAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val selectionModel = editor.selectionModel
        val selectedText = if (selectionModel.hasSelection()) {
            selectionModel.selectedText ?: return
        } else {
            // Если нет выделения — берём строку под курсором
            val line = editor.caretModel.logicalPosition.line
            val start = editor.document.getLineStartOffset(line)
            val end = editor.document.getLineEndOffset(line)
            editor.document.getText(com.intellij.openapi.util.TextRange(start, end))
        }

        val caretLine = editor.caretModel.logicalPosition.line
        val storage = StickyNoteStorage.getInstance(project)
        val note = storage.findNoteAt(file.path, caretLine)

        val result = if (note != null) {
            "# ${note.text}\n\n$selectedText"
        } else {
            selectedText
        }

        CopyPasteManager.getInstance().setContents(StringSelection(result))
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
    }
}
