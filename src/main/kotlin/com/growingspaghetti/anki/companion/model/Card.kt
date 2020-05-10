package com.growingspaghetti.anki.companion.model

import java.util.*

data class Card(
        val id: Long,
        val nid: Long,
        val did: Long,
        val ord: Int,
        val mod: Long,
        val usn: Int,
        val type: Int,
        val queue: Int,
        val due: Long,
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

fun Card.dueReadable(collectionCreationTime: Long) : String {
//    return when (this.type) {
//        0 /*new*/ -> "new\t${this.due}"
//        1 /*leaning*/ -> "learning\t${Date(this.due)}"
//        2 /*due*/ -> "due\t${Date(collectionCreationTime + 86400000 * this.due)}"
//        3 /*relearning*/ -> "relearning\t${Date(collectionCreationTime + 86400000 * this.due)}"
//        else -> ""
//    }
    return when (this.queue) {
        -3 -> "user buried(In scheduler 2)"
        -2 -> "sched buried (In scheduler 2)/buried(In scheduler 1)"
        -1 -> "suspended"
        0 -> "new\t${this.due}"
        1 -> "learning\t${Date(this.due * 1000)}"
        2 -> "due\t${Date(collectionCreationTime + 86400000 * this.due)}"
        3 -> "learning \t${Date(collectionCreationTime + 86400000 * this.due)} next rev in at least a day after the previous review"
        else -> ""
    }
}

fun Card.queueReadable() {
    when (this.queue) {
        -3 -> println("user buried(In scheduler 2)")
        -2 -> println("sched buried (In scheduler 2)/buried(In scheduler 1)")
        -1 -> println("suspended")
        0 -> println("new")
        1 -> println("learning (1)")
        2 -> println("due")
        3 -> println("in learning, next rev in at least a day after the previous review")
    }
}

fun Card.ivlReadable() : String {
    return if (this.ivl < 0) {
        "${-1 * this.ivl} seconds"
    } else {
        "${this.ivl} days"
    }
}
