package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.Card
import com.growingspaghetti.anki.companion.model.row
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

class CardTable : JTable() {
    private val cardList: MutableList<Card> = ArrayList()
    private val tableModel: DefaultTableModel = DefaultTableModel()

    init {
        model = tableModel
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultEditor(Object::class.java, null)
        tableModel.setColumnIdentifiers(Card.columnIdentifiers())
        fillsViewportHeight = true
        setAutoResizeMode( JTable.AUTO_RESIZE_OFF )
    }

    fun setCards(cards: List<Card>) {
        cardList.addAll(cards.sortedBy { it.id })
        cards.forEach {
            tableModel.addRow(it.row())
        }
    }
}