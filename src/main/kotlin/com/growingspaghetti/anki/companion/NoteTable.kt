package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.Note
import com.growingspaghetti.anki.companion.model.row
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

class NoteTable : JTable() {
    private val noteList: MutableList<Note> = ArrayList()
    private val tableModel: DefaultTableModel = DefaultTableModel()

    init {
        model = tableModel
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultEditor(Object::class.java, null)
        tableModel.setColumnIdentifiers(Note.columnIdentifiers())
        fillsViewportHeight = true
        setAutoResizeMode( JTable.AUTO_RESIZE_OFF )
    }

    fun setNotes(cards: List<Note>) {
        noteList.addAll(cards.sortedBy { it.id })
        cards.forEach {
            tableModel.addRow(it.row())
        }
    }
}