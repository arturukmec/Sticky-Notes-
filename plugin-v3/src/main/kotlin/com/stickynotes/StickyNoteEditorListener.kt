package com.stickynotes

import com.intellij.openapi.editor.event.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ProjectManager

class StickyNoteEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return

        val panel = StickyNotePanel.getInstance(project)
        val storage = StickyNoteStorage.getInstance(project)

        // Refresh panel when this editor gets focus
        editor.addFocusListener(object : com.intellij.openapi.editor.event.FocusChangeListener {
            override fun focusGained(e: Editor) { panel.setEditor(e) }
        })

        // Track document changes to shift note lines
        editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val doc = editor.document
                val changedLine = doc.getLineNumber(event.offset)
                val oldLineCount = doc.lineCount - (event.newFragment.count { it == '\n' } - event.oldFragment.count { it == '\n' })
                val delta = doc.lineCount - oldLineCount
                if (delta != 0) {
                    storage.updateLines(file.path, changedLine, delta)
                }
                panel.refresh()
            }
        })
    }
}
