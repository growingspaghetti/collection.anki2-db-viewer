package com.growingspaghetti.anki.companion.model

data class Note(
        val id: Long,
        val guid: String,
        val mid: Long,
        val mod: Long,
        val usn: Int,
        val tags: String,
        val flds: String,
        val sfld: String,
        val csum: Int,
        val flags: Int,
        val data: String
)

fun Note.fldFieldList() = this.flds.split(0x1F.toChar())
