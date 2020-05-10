package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.Deck
import com.growingspaghetti.anki.companion.model.row
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

class DeckTable : JTable() {
    private val deckList: MutableList<Deck> = ArrayList()
    private val tableModel: DefaultTableModel = DefaultTableModel()

    init {
        model = tableModel
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultEditor(Object::class.java, null)
        tableModel.setColumnIdentifiers(Deck.columnIdentifiers())
        fillsViewportHeight = true
        setAutoResizeMode( JTable.AUTO_RESIZE_OFF )
    }

    fun setDecks(decks: List<Deck>) {
        deckList.addAll(decks.sortedBy { it.id })
        decks.forEach {
            tableModel.addRow(it.row())
        }
    }
}