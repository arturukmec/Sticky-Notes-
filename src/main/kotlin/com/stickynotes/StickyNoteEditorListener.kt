package com.stickynotes

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ProjectManager

class StickyNoteEditorListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        val storage = StickyNoteStorage.getInstance(project)
        refreshGutterIcons(editor, file.path, storage)
    }
}
