package com.growingspaghetti.anki.companion.service;

import com.growingspaghetti.anki.companion.Loggable
import com.growingspaghetti.anki.companion.SqliteDbResolvable
import com.growingspaghetti.anki.companion.SqliteRepository
import com.growingspaghetti.anki.companion.model.*

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
        cards.forEach { println(it) }
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
