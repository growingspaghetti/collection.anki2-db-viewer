package com.growingspaghetti.anki.companion.service;

import com.growingspaghetti.anki.companion.SqliteDbResolvable
import com.growingspaghetti.anki.companion.SqliteRepository
import com.growingspaghetti.anki.companion.model.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AnkiDbService(private val sqliteDbResolver: SqliteDbResolvable) {
    private val sqliteRepository = SqliteRepository(sqliteDbResolver)

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

    fun colLazy(): Col {
        val col: Col by lazy {
            col()!!
        }
        return col
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
        return cards.map { c ->
            ColCardNoteRevs(col, c, noteMap[c.nid] ?: error(""), revLogLists.filter { it.cid == c.id })
        }
    }
}
