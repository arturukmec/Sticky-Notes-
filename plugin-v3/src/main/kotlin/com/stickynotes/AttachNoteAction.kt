package com.stickynotes

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class AttachNoteAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val sel = editor.selectionModel
        if (!sel.hasSelection()) return

        val startLine = editor.document.getLineNumber(sel.selectionStart)
        val endLine = editor.document.getLineNumber(sel.selectionEnd)
        val storage = StickyNoteStorage.getInstance(project)
        val existing = storage.findNoteAt(file.path, startLine)

        val note = existing ?: StickyNote(
            id = StickyNoteStorage.newId(),
            filePath = file.path,
            startLine = startLine,
            endLine = endLine,
            text = ""
        ).also {
            it.startLine = startLine
            it.endLine = endLine
        }

        // Update range if re-selecting
        if (existing != null) {
            existing.startLine = startLine
            existing.endLine = endLine
        }

        storage.saveNote(note)

        // Open tool window and focus
        val tw = ToolWindowManager.getInstance(project).getToolWindow("Sticky Notes")
        tw?.show {
            StickyNotePanel.getInstance(project).setEditor(editor)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(CommonDataKeys.EDITOR) != null
    }
}
