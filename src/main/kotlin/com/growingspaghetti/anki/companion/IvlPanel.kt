package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.Card
import com.growingspaghetti.anki.companion.model.Queue
import com.growingspaghetti.anki.companion.model.queueEnum
import java.awt.FlowLayout
import java.io.File
import java.time.LocalDateTime
import java.util.*
import javax.swing.JButton
import javax.swing.JPanel

class IvlPanel : JPanel() {
    private val normalB = JButton("1")
    private val doubleB = JButton("2")
    private val tripleB = JButton("3")
    private val quadrupleB = JButton("4")
    private val quintupleB = JButton("5")
    private val sextupleB = JButton("6")
    private val duodecupleB = JButton("10")
    private var c: Card? = null

    init {
        layout = FlowLayout()
        add(normalB)
        add(doubleB)
        add(tripleB)
        add(quadrupleB)
        add(quintupleB)
        add(sextupleB)
        add(duodecupleB)

        normalB.addActionListener {
            updateQuery(1)
        }
        doubleB.addActionListener {
            updateQuery(2)
        }
        tripleB.addActionListener {
            updateQuery(3)
        }
        quadrupleB.addActionListener {
            updateQuery(4)
        }
        quintupleB.addActionListener {
            updateQuery(5)
        }
        sextupleB.addActionListener {
            updateQuery(6)
        }
        duodecupleB.addActionListener {
            val today = DATE_FORMATTER.format(LocalDateTime.now())
            val q =
                "UPDATE \"cards\" SET due=${c!!.due + c!!.ivl * 12},mod=${System.currentTimeMillis() / 1000},ivl=${c!!.ivl * 8},reps=${c!!.reps + 1} WHERE id=${c?.id};\n"
            if (c!!.queueEnum() == Queue.REVIEW && c!!.ivl > 50) {
                File("cram-sql-${today}.txt").appendText(q)
            }
        }
    }

    fun updateQuery(mult: Int) {
        val today = DATE_FORMATTER.format(LocalDateTime.now())
        val q =
            "UPDATE \"cards\" SET due=${c!!.due + c!!.ivl * mult + Random().nextInt(10 * mult * mult)},mod=${System.currentTimeMillis() / 1000},ivl=${c!!.ivl * mult},reps=${c!!.reps + 1} WHERE id=${c?.id};\n"
        if (c!!.queueEnum() == Queue.REVIEW && c!!.ivl > 50) {
            File("cram-sql-${today}.txt").appendText(q)
        }
    }

    fun setCard(colCrt: Long, card: Card) {
        this.c = card
        normalB.text = ivlText(colCrt, card, 1)
        doubleB.text = ivlText(colCrt, card, 2)
        tripleB.text = ivlText(colCrt, card, 3)
        quadrupleB.text = ivlText(colCrt, card, 4)
        quintupleB.text = ivlText(colCrt, card, 5)
        sextupleB.text = ivlText(colCrt, card, 6)
        duodecupleB.text =
            SIMPLE_DATE_FORMAT.format(colCrt * 1000 + 86400000 * (card.due + card.ivl * 12))
    }

    fun ivlText(colCrt: Long, card: Card, mult: Int): String {
        return SIMPLE_DATE_FORMAT.format(colCrt * 1000 + 86400000 * (card.due + card.ivl * mult + Random().nextInt(10 * mult * mult)))
    }
}
