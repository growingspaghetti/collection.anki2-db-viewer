package com.growingspaghetti.anki.companion.model

import com.growingspaghetti.anki.companion.SIMPLE_DATE_FORMAT
import java.util.*

data class ColCardNoteRevs(
        val col: Col,
        val templates: List<Template>,
        val fields: List<Field>,
        val card: Card,
        val note: Note,
        val revs: List<RevLog>
) {
    override fun toString(): String = """
        ${this.note.sfld}<br>
        <font color='#6f9f9f'>
            ${this.card.queueEnum()}[${this.card.reps}] (ivl ${this.card.ivlReadable()}) 
            -- ${this.revs.map { SIMPLE_DATE_FORMAT.format(Date(it.id))}}
        </font>
    """.trimIndent()
}