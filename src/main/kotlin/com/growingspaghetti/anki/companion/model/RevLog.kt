package com.growingspaghetti.anki.companion.model

data class RevLog(
        val id: Long,
        val cid: Long,
        val usn: Int,
        val ease: Int,
        val ivl: Int,
        val lastIvl: Int,
        val factor: Int,
        val time: Int,
        val type: Int
)
