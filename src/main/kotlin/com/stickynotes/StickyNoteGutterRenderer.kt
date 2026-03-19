package com.stickynotes

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class StickyNoteGutterRenderer(
    private val note: StickyNote,
    private val onClick: () -> Unit
) : GutterIconRenderer() {

    override fun getIcon(): Icon = IconLoader.getIcon("/icons/note.svg", StickyNoteGutterRenderer::class.java)
        ?: com.intellij.icons.AllIcons.General.Note

    override fun getTooltipText(): String = note.text

    override fun isNavigateAction() = true

    override fun getClickAction() = com.intellij.openapi.actionSystem.AnAction.EMPTY_ACTION

    override fun equals(other: Any?) = other is StickyNoteGutterRenderer && other.note.id == note.id
    override fun hashCode() = note.id.hashCode()
}

fun refreshGutterIcons(editor: Editor, filePath: String, storage: StickyNoteStorage) {
    val markupModel: MarkupModel = editor.markupModel
    // Убираем старые
    markupModel.allHighlighters
        .filter { it.gutterIconRenderer is StickyNoteGutterRenderer }
        .forEach { markupModel.removeHighlighter(it) }

    // Добавляем актуальные
    storage.getNotesForFile(filePath).forEach { note ->
        val lineStart = editor.document.getLineStartOffset(
            note.startLine.coerceAtMost(editor.document.lineCount - 1)
        )
        val highlighter = markupModel.addRangeHighlighter(
            lineStart, lineStart,
            HighlighterLayer.LAST,
            null,
            HighlighterTargetArea.LINES_IN_RANGE
        )
        highlighter.gutterIconRenderer = StickyNoteGutterRenderer(note) {}
    }
}
