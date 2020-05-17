package com.growingspaghetti.anki.companion.service;

import com.growingspaghetti.anki.companion.Loggable
import com.growingspaghetti.anki.companion.SqliteDbResolvable
import com.growingspaghetti.anki.companion.SqliteRepository
import com.growingspaghetti.anki.companion.model.*
import com.growingspaghetti.anki.companion.model.Queue
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AnkiDbService(
        sqliteDbResolver: SqliteDbResolvable,
        private val log: Loggable
) {
    val sqliteRepository = SqliteRepository(sqliteDbResolver)

    private fun col() = sqliteRepository.fetchCol()
    private fun cards() = sqliteRepository.fetchAll("cards", Card::class.java)
    private fun notes() = sqliteRepository.fetchAll("notes", Note::class.java)
    private fun revlogs() = sqliteRepository.fetchAll("revlog", RevLog::class.java)
    private fun graves() = sqliteRepository.fetchAll("graves", Grave::class.java)

    fun notesLazy(): List<Note> {
        val notes: List<Note> by lazy {
            notes()!!
        }
        return notes
    }

    fun cardsLazy(): List<Card> {
        val cards: List<Card> by lazy {
            cards()!!
        }
        return cards;
    }

    fun decks(): List<Deck> {
        val col: Col by lazy {
            col()!!
        }
        return col.deckList()
    }

    fun models(): List<Model> {
        val col: Col by lazy {
            col()!!
        }
        return col.modelList()
    }

    fun queue() {
        val collectionCreationTime = colLazy().crtCreationDate()
        decks().map { it.id }.forEach { id ->
            val day = 86400000
//            val cards = sqliteRepository.queue(id, Queue.DUE, (Date().time - collectionCreationTime.time) / day)
//            cards.forEach { c ->
//                println(Date(c.due * day + collectionCreationTime.time))
//            }
            val cards = sqliteRepository.queue(id, Queue.RELEARN, Date().time / 1000)
            cards.forEach { c ->
                println(Date(c.due * 1000))
            }
            val notes = sqliteRepository.findByIds(cards.map { it.nid }, "notes", Note::class.java)
            notes.forEach { n ->
                println(n)
            }
        }
    }

    fun queues(deckName: String, targetDate: Date): List<ColCardNoteRevs> {
        val col = colLazy()
        val deckId = decks().find { it.name == deckName }!!.id
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        val diff: Long =  targetDate.time - calendar.time.time
        val dateOffset = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        val cards = ArrayList<Card>()
        cards.addAll(sqliteRepository.queueReview(deckId, col, dateOffset))
        cards.addAll(sqliteRepository.queueLearning(deckId, dateOffset))
        cards.addAll(sqliteRepository.queueDayRelearn(deckId, col, dateOffset))
        val notes = sqliteRepository.findByIds(cards.map { it.nid }, "notes", Note::class.java)
        val noteMap = notes.map { it.id to it }.toMap()
        val revLogLists = sqliteRepository.findRevLogsByCids(cards.map { it.id })
        val colCardNoteRevs = cards.map { c ->
            ColCardNoteRevs(col, c, noteMap[c.nid] ?: error(""), revLogLists.filter { it.cid == c.id })
        }
        return colCardNoteRevs
    }

    fun colLazy(): Col {
        val col: Col by lazy {
            col()!!
        }
        return col
    }

    fun fetchCol(): String {
        val col: Col by lazy {
            col()!!
        }
        println(col)
        log.log(col.toString().replace(" ", "\n"))

        println(col.modelList())
        val deckHtml = col.deckList()
                .map { it.html() }.joinToString("<hr>");
        println(col.deckList())
        val modelHtml = col.modelList()
                .map { it.html() }.joinToString("<hr>");
        println(col.confObject())
        println(col.dconfList())
        val dconfHtml = col.dconfList()
                .map { it.html() }.joinToString("<hr>");
        return """
            <hr><h2> Collection </h2>
            ${col.html()}
            <hr><h2> Decks </h2>
            $deckHtml
            <hr><h2> Models </h2>
            $modelHtml
            <hr><h2> Conf </h2>
            ${col.confObject().html(col.deckList())}
            <hr><h2> DConf </h2>
            $dconfHtml
            <hr><h2> Tags </h2>
            ${col.tagsObject()}
        """.trimIndent()
    }

    fun fetchCards(): String {
        val cards: List<Card> by lazy {
            cards()!!
        }
        val col: Col by lazy {
            col()!!
        }
        val list = cards.map { it.toString() + "\n" + it.dueReadable(col.crt * 1000) + "\n" + it.ivlReadable() }
        File("carddue.txt").writeText(list.joinToString(separator = "\n"))
        //System.exit(0)
        return "a"
    }

    fun fetchNotes(): String {
        val notes: List<Note> by lazy {
            notes()!!
        }
        //notes.forEach { println(it) }
        notes.forEach { it.fldFieldList().forEach { s -> println(s) } }
        return "a"
    }

    fun fetchRevLogs(): String {
        val revlogs: List<RevLog> by lazy {
            revlogs()!!
        }
        revlogs.forEach { println(it) }
        return "a"
    }

    fun fetchGraves(): String {
        val graves: List<Grave> by lazy {
            graves()!!
        }
        graves.forEach { println(it) }
        return "a"
    }
}
