package com.stickynotes

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.UUID

@State(
    name = "StickyNoteStorage",
    storages = [Storage("sticky_notes.xml")]
)
@Service(Service.Level.PROJECT)
class StickyNoteStorage : PersistentStateComponent<StickyNoteStorage.State> {

    data class NoteData(
        var id: String = "",
        var filePath: String = "",
        var startLine: Int = 0,
        var endLine: Int = 0,
        var text: String = ""
    )

    class State {
        var notes: MutableList<NoteData> = mutableListOf()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    fun getNotesForFile(filePath: String): List<StickyNote> {
        return state.notes
            .filter { it.filePath == filePath }
            .map { StickyNote(it.id, it.filePath, it.startLine, it.endLine, it.text) }
    }

    fun saveNote(note: StickyNote) {
        val existing = state.notes.find { it.id == note.id }
        if (existing != null) {
            existing.startLine = note.startLine
            existing.endLine = note.endLine
            existing.text = note.text
        } else {
            state.notes.add(NoteData(note.id, note.filePath, note.startLine, note.endLine, note.text))
        }
    }

    fun deleteNote(id: String) {
        state.notes.removeIf { it.id == id }
    }

    fun findNoteAt(filePath: String, line: Int): StickyNote? {
        return state.notes
            .filter { it.filePath == filePath && line >= it.startLine && line <= it.endLine }
            .map { StickyNote(it.id, it.filePath, it.startLine, it.endLine, it.text) }
            .firstOrNull()
    }

    fun updateLines(filePath: String, fromLine: Int, delta: Int) {
        state.notes
            .filter { it.filePath == filePath && it.startLine >= fromLine }
            .forEach {
                it.startLine += delta
                it.endLine += delta
            }
    }

    companion object {
        fun getInstance(project: Project): StickyNoteStorage =
            project.getService(StickyNoteStorage::class.java)

        fun newId(): String = UUID.randomUUID().toString()
    }
}
