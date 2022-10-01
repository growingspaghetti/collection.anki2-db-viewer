package com.growingspaghetti.anki.companion.service;

import com.growingspaghetti.anki.companion.SqliteDbResolvable
import com.growingspaghetti.anki.companion.SqliteRepository
import com.growingspaghetti.anki.companion.model.*
import java.util.*
import java.util.concurrent.TimeUnit


class AnkiDbService(private val sqliteDbResolver: SqliteDbResolvable) {
    private val sqliteRepository = SqliteRepository(sqliteDbResolver)

    private fun col() = sqliteRepository.fetchCol()
    private fun templates() = sqliteRepository.fetchAll("templates", Template::class.java)
    private fun fields() = sqliteRepository.fetchAll("fields", Field::class.java)
    private fun decks() = sqliteRepository.fetchAll("decks", Deck50::class.java)
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

    fun decksLazy(): List<Deck50> {
        val decks: List<Deck50> by lazy {
            decks()!!
        }
        return decks
    }

    fun modelList50Lazy(): List<Template> {
        val models: List<Template> by lazy {
            templates()
        }
        return models
    }

    fun fieldList50Lazy(): List<Field> {
        val fields: List<Field> by lazy {
            fields()
        }
        return fields
    }

    fun models(): List<Model> {
        val col: Col by lazy {
            col()!!
        }
        return col.modelList()
    }

    fun queues(deckName: String, targetDate: Date): List<ColCardNoteRevs> {
        val col = colLazy()
        val cards = ArrayList<Card>()
        decks().filter { it.nameStr() == deckName || it.nameStr().startsWith("${deckName}::") }.forEach {
            val deckId = it.id
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            val diff: Long = targetDate.time - calendar.time.time
            val dateOffset = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            cards.addAll(sqliteRepository.queueReview(deckId, col, dateOffset))
            cards.addAll(sqliteRepository.queueLearning(deckId, dateOffset))
            cards.addAll(sqliteRepository.queueDayRelearn(deckId, col, dateOffset))
        }
        val notes = sqliteRepository.findByIds(cards.map { it.nid }, "notes", Note::class.java)
        val noteMap = notes.map { it.id to it }.toMap()
        val revLogLists = sqliteRepository.findRevLogsByCids(cards.map { it.id })
        val templates = modelList50Lazy()
        val fields = fieldList50Lazy()
        return cards.map { c ->
            ColCardNoteRevs(
                col,
                templates,
                fields,
                c,
                noteMap[c.nid] ?: error(""),
                revLogLists.filter { it.cid == c.id })
        }
    }
}
