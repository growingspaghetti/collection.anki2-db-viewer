package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.ColCardNoteRevs
import com.growingspaghetti.anki.companion.model.fldFieldList
import com.growingspaghetti.anki.companion.model.modelListLazy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture
import javax.swing.DefaultListModel
import javax.swing.JEditorPane
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent


class ColCardNoteRevsSwingList(private val editorPane: JEditorPane) : JList<ColCardNoteRevs>() {
    private val list: MutableList<ColCardNoteRevs> = ArrayList()
    private val model: DefaultListModel<ColCardNoteRevs> = DefaultListModel()
    private var job: Job? = null

    init {
        setModel(model)
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = ListCellRenderableLabel()
        keyListeners.forEach { removeKeyListener(it) }
        applySelectionListener()
    }

    fun selectNext() {
        this.selectedIndex =  this.selectedIndex + 1
        this.ensureIndexIsVisible(this.selectedIndex)
    }

    fun setList(colCardNoteRevs: List<ColCardNoteRevs>) {
        job?.cancel()
        list.clear()
        model.clear()
        job = GlobalScope.launch(Dispatchers.Main) {
            val channel = Channel<ColCardNoteRevs>()
            GlobalScope.launch {
                colCardNoteRevs.forEach {
                    channel.send(it)
                }
                channel.close()
            }
            for (y in channel) {
                if (isActive) {
                    model.addElement(y)
                }
            }
        }
        list.addAll(colCardNoteRevs)
    }

    private fun applySelectionListener() {
        addListSelectionListener { e: ListSelectionEvent ->
            if (e.valueIsAdjusting) {
                return@addListSelectionListener
            }
            val ccnr: ColCardNoteRevs? = selectedValue
            ccnr?.let {
                val modelMap = ccnr.col.modelListLazy().map { it.id to it }.toMap()
                val model = modelMap[ccnr.note.mid] ?: error("")
                model.flds.map { it.ord to it.name }
                val fields = ccnr.note.fldFieldList()
                var ans = model.tmpls[0].afmt
                model.flds.forEach {
                    ans = ans.replace("{{" + it.name + "}}\n", fields[it.ord])
                }
                ans = ans.replace("src=\"", "src=\"file:/media/local/share/Anki2/User 1/collection.media/")
//                val m = MP3_PA.matcher(ans)
//                val sb = StringBuffer()
//                if (m.find()) {
//                    CompletableFuture.runAsync(Runnable { Mp3Player().testPlay(File("/media/local/share/Anki2/User 1/collection.media/" + m.group(1))) })
//                }
//                m.appendTail(sb)

                val m = MP3_PA.matcher(ans)
                val sb = StringBuffer()
                while (m.find()) {
                    val f = File("/media/local/share/Anki2/User 1/collection.media/" + m.group(1))
                    CompletableFuture.runAsync(Runnable { Mp3Player(this).play(FileInputStream(f)) }  )
                    //Mp3Player().testPlay(f)
                    m.appendReplacement(sb, "")
                }
                m.appendTail(sb)
                editorPane.text = "<html><div style=\"font-size:20px\">$sb"

            }
        }
    }
}
