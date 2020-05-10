package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.Model
import com.growingspaghetti.anki.companion.model.row
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

class ModelTable : JTable() {
    private val modelList: MutableList<Model> = ArrayList()
    private val tableModel: DefaultTableModel = DefaultTableModel()

    init {
        model = tableModel
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultEditor(Object::class.java, null)
        tableModel.setColumnIdentifiers(Model.columnIdentifiers())
        fillsViewportHeight = true
        setAutoResizeMode( JTable.AUTO_RESIZE_OFF )
    }

    fun setModels(models: List<Model>) {
        modelList.addAll(models.sortedBy { it.id })
        models.forEach {
            tableModel.addRow(it.row())
        }
    }
}