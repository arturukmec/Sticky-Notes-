package com.stickynotes

import com.intellij.icons.AllIcons
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class NoteCard(
    private val note: StickyNote,
    private val project: Project,
    private val storage: StickyNoteStorage,
    private val getCodeForNote: (StickyNote) -> String
) : JPanel(BorderLayout()) {

    private var collapsed = false
    private val textArea = JBTextArea(note.text).apply {
        lineWrap = true
        wrapStyleWord = true
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        rows = maxOf(2, note.text.split("\n").size)
        background = noteColor(storage.computeDepth(note))
    }

    init {
        val depth = storage.computeDepth(note)
        val bg = noteColor(depth)
        val border = noteBorderColor(depth)

        background = bg
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 2, true),
            EmptyBorder(4, 6, 4, 6)
        ))

        // Header
        val header = JPanel(BorderLayout()).apply { isOpaque = false }

        val lineLabel = JLabel("Lines ${note.startLine + 1}–${note.endLine + 1}").apply {
            font = font.deriveFont(Font.BOLD, 11f)
            foreground = Color(80, 80, 80)
        }

        val btnPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 2, 0)).apply { isOpaque = false }

        // Collapse button
        val collapseBtn = JButton(if (collapsed) "▼" else "▲").apply {
            font = font.deriveFont(10f); isBorderPainted = false; isContentAreaFilled = false
            preferredSize = Dimension(22, 22); toolTipText = "Collapse/Expand"
        }
        collapseBtn.addActionListener {
            collapsed = !collapsed
            textArea.isVisible = !collapsed
            collapseBtn.text = if (collapsed) "▼" else "▲"
            revalidate(); repaint()
        }

        // Copy button
        val copyBtn = JButton(AllIcons.Actions.Copy).apply {
            isBorderPainted = false; isContentAreaFilled = false
            preferredSize = Dimension(22, 22); toolTipText = "Copy note + code"
        }
        copyBtn.addActionListener {
            val code = getCodeForNote(note)
            CopyPasteManager.getInstance().setContents(StringSelection("# ${note.text}\n\n$code"))
        }

        // Delete button
        val deleteBtn = JButton(AllIcons.Actions.Close).apply {
            isBorderPainted = false; isContentAreaFilled = false
            preferredSize = Dimension(22, 22); toolTipText = "Delete note"
        }
        deleteBtn.addActionListener {
            val confirm = JOptionPane.showConfirmDialog(this, "Delete this note?", "Sticky Notes", JOptionPane.YES_NO_OPTION)
            if (confirm == JOptionPane.YES_OPTION) storage.deleteNote(note.id)
        }

        btnPanel.add(collapseBtn); btnPanel.add(copyBtn); btnPanel.add(deleteBtn)
        header.add(lineLabel, BorderLayout.WEST)
        header.add(btnPanel, BorderLayout.EAST)
        add(header, BorderLayout.NORTH)

        // Text area
        textArea.background = bg
        textArea.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = save()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = save()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = save()
            fun save() {
                note.text = textArea.text
                storage.saveNote(note)
                val lines = textArea.text.split("\n").size
                textArea.rows = maxOf(2, lines)
                revalidate()
            }
        })

        val scroll = JBScrollPane(textArea,
            JBScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        ).apply { isOpaque = false; viewport.isOpaque = false; border = null }

        add(scroll, BorderLayout.CENTER)
    }
}
