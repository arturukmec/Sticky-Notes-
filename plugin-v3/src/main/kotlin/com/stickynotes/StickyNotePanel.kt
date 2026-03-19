package com.stickynotes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder

@Service(Service.Level.PROJECT)
class StickyNotePanel(private val project: Project) {

    val component: JComponent = buildPanel()
    private var cardsPanel = JPanel()
    private var currentEditor: Editor? = null
    private val storage get() = StickyNoteStorage.getInstance(project)

    private fun buildPanel(): JComponent {
        cardsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = EmptyBorder(8, 8, 8, 8)
            background = UIManager.getColor("Panel.background")
        }
        val scroll = JScrollPane(cardsPanel).apply {
            border = null
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        storage.addListener { ApplicationManager.getApplication().invokeLater { refresh() } }
        return scroll
    }

    fun setEditor(editor: Editor) {
        currentEditor = editor
        refresh()
    }

    fun refresh() {
        val editor = currentEditor ?: return
        val file = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
            .getFile(editor.document)?.path ?: return

        // Clear highlights
        editor.markupModel.allHighlighters
            .filter { it.layer == HighlighterLayer.SELECTION - 1 }
            .forEach { editor.markupModel.removeHighlighter(it) }

        // Clear cards
        cardsPanel.removeAll()

        val notes = storage.getNotesForFile(file)

        if (notes.isEmpty()) {
            cardsPanel.add(JLabel("No notes. Select code and press Alt+N").apply {
                foreground = Color.GRAY
                alignmentX = Component.LEFT_ALIGNMENT
            })
        }

        notes.forEach { note ->
            // Highlight code region
            addHighlight(editor, note)

            // Add card
            val card = NoteCard(note, project, storage) { getCode(editor, note) }.apply {
                alignmentX = Component.LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            }
            cardsPanel.add(card)
            cardsPanel.add(Box.createVerticalStrut(8))
        }

        cardsPanel.revalidate()
        cardsPanel.repaint()
    }

    private fun addHighlight(editor: Editor, note: StickyNote) {
        val doc = editor.document
        val depth = storage.computeDepth(note)
        val color = noteColor(depth).let {
            Color(it.red, it.green, it.blue, 60)
        }
        val attrs = TextAttributes().apply { backgroundColor = color }
        val startLine = note.startLine.coerceAtMost(doc.lineCount - 1)
        val endLine = note.endLine.coerceAtMost(doc.lineCount - 1)
        val start = doc.getLineStartOffset(startLine)
        val end = doc.getLineEndOffset(endLine)
        editor.markupModel.addRangeHighlighter(
            start, end,
            HighlighterLayer.SELECTION - 1,
            attrs,
            HighlighterTargetArea.LINES_IN_RANGE
        )
    }

    private fun getCode(editor: Editor, note: StickyNote): String {
        val doc = editor.document
        val startLine = note.startLine.coerceAtMost(doc.lineCount - 1)
        val endLine = note.endLine.coerceAtMost(doc.lineCount - 1)
        val start = doc.getLineStartOffset(startLine)
        val end = doc.getLineEndOffset(endLine)
        return doc.getText(com.intellij.openapi.util.TextRange(start, end))
    }

    companion object {
        fun getInstance(project: Project): StickyNotePanel = project.getService(StickyNotePanel::class.java)
    }
}
