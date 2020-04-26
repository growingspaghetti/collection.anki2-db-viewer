package com.growingspaghetti.anki.companion.model

data class Card(
        val id: Long,
        val nid: Long,
        val did: Long,
        val ord: Int,
        val mod: Long,
        val usn: Int,
        val type: Int,
        val queue: Int,
        val due: Int,
        val ivl: Int,
        val factor: Int,
        val reps: Int,
        val lapses: Int,
        val left: Int,
        val odue: Int,
        val odid: Int,
        val flags: Int,
        val data: String) {
}
