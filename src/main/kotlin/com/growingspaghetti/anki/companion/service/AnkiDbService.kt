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

    private fun col(): Col {
        return sqliteRepository.fetchCol()
    }

    fun fetchCol(): String {
        val col: Col by lazy {
            col()
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
        """.trimIndent()
    }
}
