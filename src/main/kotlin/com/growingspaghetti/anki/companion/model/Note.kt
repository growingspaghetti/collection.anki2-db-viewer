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
){
    companion object {
        fun columnIdentifiers() = arrayOf("id", "guid", "mid", "mod", "usn",
                "tags", "flds", "sfld", "csum", "flags",
                "data"
        )
    }
}

fun Note.row() = arrayOf(this.id, this.guid, this.mid, this.mod, this.usn,
        this.tags, this.flds, this.sfld, this.csum, this.flags,
        this.data)

fun Note.fldFieldList() = this.flds.split(0x1F.toChar())
