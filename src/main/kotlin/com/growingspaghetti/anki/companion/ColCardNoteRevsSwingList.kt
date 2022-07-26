package com.growingspaghetti.anki.companion

import com.growingspaghetti.anki.companion.model.*
import javazoom.jl.player.advanced.PlaybackEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.event.ListSelectionEvent


class ColCardNoteRevsSwingList(
    private val editorPane: JEditorPane,
    private val collectionMediaDir: File,
    private val playAudioCheckBox: JCheckBox,
    private val ivlPanel: IvlPanel,
) : JList<ColCardNoteRevs>(), PlaybackEventHandleable {
    private val list: MutableList<ColCardNoteRevs> = ArrayList()
    private val model: DefaultListModel<ColCardNoteRevs> = DefaultListModel()
    private var job: Optional<Job> = Optional.empty()

    init {
        setModel(model)
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = ListCellRenderableLabel()
        keyListeners.forEach { removeKeyListener(it) }
        applySelectionListener()
    }

    fun setList(colCardNoteRevs: List<ColCardNoteRevs>) {
        job.ifPresent { j -> j.cancel() }
        list.clear()
        model.clear()
        job = Optional.of(GlobalScope.launch(Dispatchers.Main) {
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
        })
        list.addAll(colCardNoteRevs)
    }

    fun exportMp3() {
        val sb = StringBuffer()
        for (e in model.elements()) {
            val modelMap = e.col.modelListLazy().associateBy { it.id }
            val model = modelMap[e.note.mid] ?: error("")
            model.flds.map { it.ord to it.name }
            val fields = e.note.fldFieldList()
            var ans = model.tmpls[0].afmt
            var top = model.tmpls[0].qfmt
            model.flds.forEach {
                ans = ans.replace("{{" + it.name + "}}", fields[it.ord])
                top = top.replace("{{" + it.name + "}}", fields[it.ord])
            }
            run {
                val t = MP3_PA.matcher(top)
                var tf: File? = null;
                if (t.find()) {
                    sb.append("file '${collectionMediaDir.absolutePath}/${t.group(1)}'\n")
                }
                val m = MP3_PA.matcher(ans)
                while (m.find()) {
                    sb.append("file '${collectionMediaDir.absolutePath}/${m.group(1)}'\n")
                }
            }
        }
        File("mp3s.txt").writeText(sb.toString())
    }

    private fun applySelectionListener() {
        addListSelectionListener { e: ListSelectionEvent ->
            if (e.valueIsAdjusting) {
                return@addListSelectionListener
            }
            val ccnr: ColCardNoteRevs? = selectedValue
            ccnr?.let {
                val startTime = System.currentTimeMillis()
                val modelMap = ccnr.col.modelListLazy().associateBy { it.id }
                val model = modelMap[ccnr.note.mid] ?: error("")
                model.flds.map { it.ord to it.name }
                val fields = ccnr.note.fldFieldList()
                var ans = model.tmpls[0].afmt
                var top = model.tmpls[0].qfmt
                model.flds.forEach {
                    ans = ans.replace("{{" + it.name + "}}", fields[it.ord])
                    top = top.replace("{{" + it.name + "}}", fields[it.ord])
                }
                ans = ans.replace("src=\"", "src=\"file:${collectionMediaDir.absolutePath}/")
                run {

                    val t = MP3_PA.matcher(top)
                    var tf: File? = null;
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
                                        command.add(tf.absolutePath)
                                        val pb = ProcessBuilder(command)
                                        pb.redirectErrorStream(true)
                                        val p = pb.start()
                                        // https://stackoverflow.com/questions/5483830/process-waitfor-never-returns
                                        val reader = BufferedReader(InputStreamReader(p.getInputStream()))
                                        var line: String
                                        while (reader.readLine().also { line = it } != null) {
                                        }
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
                                    command.add(f.absolutePath)
                                    val pb = ProcessBuilder(command)
                                    pb.redirectErrorStream(true)
                                    val p = pb.start()
                                    // https://stackoverflow.com/questions/5483830/process-waitfor-never-returns
                                    val reader = BufferedReader(InputStreamReader(p.getInputStream()))
                                    var line: String
                                    while (reader.readLine().also { line = it } != null) {
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }).whenComplete { _, _ ->
                                val time = when (val t = System.currentTimeMillis() - startTime) {
                                    in 0..60000 -> t
                                    else -> 60000
                                }
                                // usn -1=upload
                                // type 3=cram
                                // ease 2=ok (cram mode)
                                val revLast = it.revs.last();
                                val query =
                                    "INSERT INTO \"revlog\" (id, cid, usn, ease, ivl, lastIvl, factor, time, type) " +
                                            "VALUES(${startTime}, ${it.card.id}, -1, 2, ${revLast.ivl}, ${revLast.lastIvl}, ${revLast.factor}, ${time}, 3);\n"
                                val today = DATE_FORMATTER.format(LocalDateTime.now())
                                File("cram-sql-${today}.txt").appendText(query)
                                handlePlayback()
                            }
                        }
                        m.appendReplacement(sb, "")
                    }
                    if (!MP3_PA.matcher(ans).find()) {
                        CompletableFuture.runAsync(Runnable {
                            TimeUnit.SECONDS.sleep(2L)
                        }).whenComplete { _, _ ->
                            handlePlayback()
                        }
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
                    ans = sb.toString().replace("img", "img width='400'")
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
                ivlPanel.setCard(ccnr.col.crt, ccnr.card)
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
