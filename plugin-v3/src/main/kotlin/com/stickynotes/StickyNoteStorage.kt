package com.stickynotes

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.UUID

@State(name = "StickyNoteStorage", storages = [Storage("sticky_notes.xml")])
@Service(Service.Level.PROJECT)
class StickyNoteStorage : PersistentStateComponent<StickyNoteStorage.State> {

    data class NoteData(
        var id: String = "",
        var filePath: String = "",
        var startLine: Int = 0,
        var endLine: Int = 0,
        var text: String = "",
        var color: Int = 0
    )

    class State { var notes: MutableList<NoteData> = mutableListOf() }

    private var myState = State()
    private val listeners = mutableListOf<() -> Unit>()

    override fun getState() = myState
    override fun loadState(state: State) { XmlSerializerUtil.copyBean(state, myState) }

    fun addListener(l: () -> Unit) { listeners.add(l) }
    private fun notify() { listeners.forEach { it() } }

    fun getNotesForFile(filePath: String): List<StickyNote> =
        myState.notes.filter { it.filePath == filePath }
            .map { StickyNote(it.id, it.filePath, it.startLine, it.endLine, it.text, it.color) }
            .sortedBy { it.startLine }

    fun saveNote(note: StickyNote) {
        val ex = myState.notes.find { it.id == note.id }
        if (ex != null) { ex.startLine = note.startLine; ex.endLine = note.endLine; ex.text = note.text; ex.color = note.color }
        else myState.notes.add(NoteData(note.id, note.filePath, note.startLine, note.endLine, note.text, note.color))
        notify()
    }

    fun deleteNote(id: String) { myState.notes.removeIf { it.id == id }; notify() }

    fun findNoteAt(filePath: String, line: Int): StickyNote? =
        myState.notes.filter { it.filePath == filePath && line >= it.startLine && line <= it.endLine }
            .map { StickyNote(it.id, it.filePath, it.startLine, it.endLine, it.text, it.color) }
            .minByOrNull { it.endLine - it.startLine }

    fun updateLines(filePath: String, fromLine: Int, delta: Int) {
        myState.notes.filter { it.filePath == filePath && it.startLine > fromLine }
            .forEach { it.startLine += delta; it.endLine += delta }
        notify()
    }

    fun computeDepth(note: StickyNote): Int {
        return myState.notes.filter {
            it.filePath == note.filePath && it.id != note.id &&
            it.startLine <= note.startLine && it.endLine >= note.endLine
        }.size
    }

    companion object {
        fun getInstance(project: Project): StickyNoteStorage = project.getService(StickyNoteStorage::class.java)
        fun newId(): String = UUID.randomUUID().toString()
    }
}
