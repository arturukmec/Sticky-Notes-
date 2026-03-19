package com.stickynotes

data class StickyNote(
    val id: String,
    val filePath: String,
    var startLine: Int,
    var endLine: Int,
    var text: String
)
