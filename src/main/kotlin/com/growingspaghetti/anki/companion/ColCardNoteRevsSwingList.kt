package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.*
import javazoom.jl.player.advanced.PlaybackEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import kotlin.collections.ArrayList

class ColCardNoteRevsSwingList(
        private val editorPane: JEditorPane,
        private val collectionMediaDir: File,
        private val playAudioCheckBox: JCheckBox
) : JList<ColCardNoteRevs>(), PlaybackEventHandleable {
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
                ans = ans.replace("src=\"", "src=\"file:${collectionMediaDir.absolutePath}/")

                run {
                    val m = MP3_PA.matcher(ans)
                    val sb = StringBuffer()
                    while (m.find()) {
                        val f = File("${collectionMediaDir.absolutePath}/" + m.group(1))
                        if (playAudioCheckBox.isSelected) {
                            CompletableFuture.runAsync(Runnable {
                                Mp3Player.play(FileInputStream(f), this)
                            })
                        }
                        m.appendReplacement(sb, "")
                    }
                    m.appendTail(sb)
                    ans = sb.toString()
                }
                run {
                    val m = IMG_PA.matcher(ans)
                    val sb = StringBuffer()
                    while (m.find()) {
                        m.appendReplacement(sb, m.group())
                    }
                    sb.append("</td><td>")
                    m.appendTail(sb)
                    ans = sb.toString()
                }

                val revs = """
                    ${ccnr.card.queueEnum()}[${ccnr.card.reps}](ivl ${ccnr.card.ivlReadable()} x ${ccnr.card.factorReadable()})@${ccnr.card.dueReadable(Date(ccnr.col.crt * 1000))}
                    -- ${ccnr.revs.map { SIMPLE_DATE_FORMAT.format(Date(it.id)) }}
                """.trimIndent()
                editorPane.text = "<html><div style=\"font-size:20px\">$revs<hr><table valign=\"top\">\n<tr><td>$ans</td></tr></table></div></html>"
            }

        }
    }

    override fun handlePlayback(evt: PlaybackEvent) {
        if (!playAudioCheckBox.isSelected) {
            return
        }
        selectedIndex += 1
        ensureIndexIsVisible(selectedIndex)
    }
}
