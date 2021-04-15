package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.*
import javazoom.jl.player.advanced.PlaybackEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import java.io.BufferedReader
import java.io.InputStreamReader


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
                var top = model.tmpls[0].qfmt
                println(top)
                model.flds.forEach {
                    println(it.name)
                    ans = ans.replace("{{" + it.name + "}}", fields[it.ord])
                    top = top.replace("{{" + it.name + "}}", fields[it.ord])
                }
                ans = ans.replace("src=\"", "src=\"file:${collectionMediaDir.absolutePath}/")
                println(top)
                run {

                    val t = MP3_PA.matcher(top)
                    var tf : File? = null;
                    if (t.find()) {
                        tf = File("${collectionMediaDir.absolutePath}/" + t.group(1))
                    }
                    val m = MP3_PA.matcher(ans)
                    val sb = StringBuffer()
                    while (m.find()) {
                        val f = File("${collectionMediaDir.absolutePath}/" + m.group(1))
                        if (playAudioCheckBox.isSelected) {
                            //CompletableFuture.runAsync(Runnable {
                            //    Mp3Player.play(FileInputStream(f), this)
                            //})
                            CompletableFuture.runAsync(Runnable {
                                tf?.let {
                                    try {
                                        val command: MutableList<String> = ArrayList()
                                        command.add("ffplay")
                                        command.add("-nodisp")
                                        // https://unix.stackexchange.com/questions/75421/command-line-audio-player-that-exits-immediately-after-file-finished-playing-bac
                                        command.add("-autoexit")
                                        command.add(tf.getAbsolutePath())
                                        val pb = ProcessBuilder(command)
                                        pb.redirectErrorStream(true)
                                        val p = pb.start()
                                        // https://stackoverflow.com/questions/5483830/process-waitfor-never-returns
                                        val reader = BufferedReader(InputStreamReader(p.getInputStream()))
                                        var line: String
                                        while (reader.readLine().also { line = it } != null) println("$line")
                                        println(p.waitFor())
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                try {
                                    val command: MutableList<String> = ArrayList()
                                    command.add("ffplay")
                                    command.add("-nodisp")
                                    // https://unix.stackexchange.com/questions/75421/command-line-audio-player-that-exits-immediately-after-file-finished-playing-bac
                                    command.add("-autoexit")
                                    command.add(f.getAbsolutePath())
                                    val pb = ProcessBuilder(command)
                                    pb.redirectErrorStream(true)
                                    val p = pb.start()
                                    // https://stackoverflow.com/questions/5483830/process-waitfor-never-returns
                                    val reader = BufferedReader(InputStreamReader(p.getInputStream()))
                                    var line: String
                                    while (reader.readLine().also { line = it } != null) println("$line")
                                    println(p.waitFor())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }).whenComplete { result, exception ->
                                handlePlayback()
                            }
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
                    ${ccnr.card.queueEnum()}[${ccnr.card.reps}](ivl ${ccnr.card.ivlReadable()} x ${ccnr.card.factorReadable()})@${
                    ccnr.card.dueReadable(
                        Date(ccnr.col.crt * 1000)
                    )
                }
                    -- ${ccnr.revs.map { SIMPLE_DATE_FORMAT.format(Date(it.id)) }}
                """.trimIndent()
                editorPane.text =
                    "<html><div style=\"font-size:20px\">$revs<hr><table valign=\"top\">\n<tr><td>$ans</td></tr></table></div></html>"
            }

        }
    }

    override fun handlePlayback(evt: PlaybackEvent) {
        handlePlayback()
    }

    private fun handlePlayback() {
        if (!playAudioCheckBox.isSelected) {
            return
        }
        selectedIndex += 1
        ensureIndexIsVisible(selectedIndex)
    }
}
