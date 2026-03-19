package com.stickynotes

import java.awt.Color

data class StickyNote(
    val id: String,
    val filePath: String,
    var startLine: Int,
    var endLine: Int,
    var text: String,
    var color: Int = 0  // depth level for color
)

val NOTE_COLORS = listOf(
    Color(255, 252, 180), // yellow
    Color(180, 220, 255), // blue
    Color(180, 255, 200), // green
    Color(255, 200, 180), // orange
    Color(220, 180, 255), // purple
)

val NOTE_BORDER_COLORS = listOf(
    Color(200, 185, 80),
    Color(80, 140, 200),
    Color(80, 180, 100),
    Color(200, 120, 80),
    Color(140, 80, 200),
)

fun noteColor(depth: Int) = NOTE_COLORS[depth % NOTE_COLORS.size]
fun noteBorderColor(depth: Int) = NOTE_BORDER_COLORS[depth % NOTE_BORDER_COLORS.size]
