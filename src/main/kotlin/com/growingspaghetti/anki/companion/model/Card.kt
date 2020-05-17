package com.growingspaghetti.anki.companion.model

//# Cards
//##########################################################################
//# Type: 0=new, 1=learning, 2=due
//# Queue: same as above, and:
//#        -1=suspended, -2=user buried, -3=sched buried
//# Due is used differently for different queues.
//# - new queue: note id or random int
//# - rev queue: integer day
//# - lrn queue: integer timestamp
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
        val reps: Int, /*repeated*/
        val lapses: Int, /*correct->incorrect*/
        val left: Int, /*1001,2002*/
        val odue: Int, /*0*/
        val odid: Int,
        val flags: Int,
        val data: String /*not used*/
) {
    companion object {
        fun columnIdentifiers() = arrayOf("id", "nid", "did", "ord", "mod",
                "usn", "type", "queue", "due", "ivl", "factor",
                "reps", "lapses", "left", "odue", "odid",
                "flags", "data"
        )
    }
}

fun Card.row() = arrayOf(this.id, this.nid, this.did, this.ord, this.mod,
        this.usn, this.type, this.queue, this.due, this.ivl, this.factor,
        this.reps, this.lapses, this.left, this.odue, this.odid,
        this.flags, this.data)

enum class Queue(val v: Int) {
    MANUALLY_BURIED(-3),
    SIBLING_BURIED(-2),
    SUSPENDED(-1),
    PREVIEW(4),
    DAY_RELEARN(3),
    REVIEW(2),
    RELEARN(1),
    NEW(0)
}

fun Card.queueEnum(): Queue {
    Queue.values().iterator().forEach {
        if (it.v == this.queue) {
            return it
        }
    }
    throw IllegalStateException()
}

// https://github.com/ankitects/anki/blob/85b28f13d2472813cdb66151dcf0407b39b3d5c3/pylib/anki/consts.py#L18
fun Card.dueReadable(collectionCreationTime: Long): String {
    // https://github.com/ankitects/anki/blob/85b28f13d2472813cdb66151dcf0407b39b3d5c3/pylib/anki/schedv2.py#L24
//    return when (this.type) {
//        0 /*new*/ -> "new\t${this.due}"
//        1 /*leaning*/ -> "learning\t${Date(this.due)}"
//        2 /*due*/ -> "due\t${Date(collectionCreationTime + 86400000 * this.due)}"
//        3 /*relearning*/ -> "relearning\t${Date(collectionCreationTime + 86400000 * this.due)}"
//        else -> ""
//    }
//    return when (this.queue) {
//        Queue.SCHED_BURIED.v -> "user buried(In scheduler 2)"
//        Queue.USER_BURIED.v -> "sched buried (In scheduler 2)/buried(In scheduler 1)"
//        -1 -> "suspended"
//        0 -> "new\t${this.due}"
//        1 -> "learning\t${Date(this.due * 1000)}"
//        2 -> "due\t${Date(collectionCreationTime + 86400000 * this.due)}"
//        3 -> "learning \t${Date(collectionCreationTime + 86400000 * this.due)} next rev in at least a day after the previous review"
//        else -> ""
//    }
    return ""
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

fun Card.ivlReadable(): String {
    return if (this.ivl < 0) {
        "${-1 * this.ivl} seconds"
    } else {
        "${this.ivl} days"
    }
}

fun Card.factorReadable(): String {
    return "When Good is pressed next time, next ivl = current ivl * ${this.factor / 1000}"
}
