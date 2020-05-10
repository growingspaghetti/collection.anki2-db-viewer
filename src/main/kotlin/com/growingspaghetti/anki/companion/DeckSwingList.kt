package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.Deck
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.ListSelectionModel

class DeckSwingList : JList<Deck>() {
    private val deckList: MutableList<Deck> = ArrayList()
    private val model: DefaultListModel<Deck> = DefaultListModel()

    init {
        setModel(model)
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = ListCellRenderableLabel()
        keyListeners.forEach { removeKeyListener(it) }
    }

    fun setDecks(decks: List<Deck>) {
        deckList.addAll(decks.sortedBy { it.id })
        model.addAll(deckList)
    }
}
