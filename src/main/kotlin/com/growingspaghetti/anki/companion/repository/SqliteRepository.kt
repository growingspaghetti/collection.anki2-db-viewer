package com.growingspaghetti.anki.companion.repository

import com.growingspaghetti.anki.companion.KtObjectMapper
import com.growingspaghetti.anki.companion.SqliteDbResolvable
import com.growingspaghetti.anki.companion.model.Col
import org.apache.commons.dbutils.QueryRunner
import org.apache.commons.dbutils.handlers.MapHandler
import org.apache.commons.dbutils.handlers.MapListHandler
import java.sql.Connection
import java.sql.DriverManager

class SqliteRepository(val sqliteDbResolvable: SqliteDbResolvable) {
    private val runner = QueryRunner()

    fun <T> fetchAll(tableName: String, clazz: Class<T>): List<T>? {
        return getConnection(true).use { conn: Connection ->
            val mapList = runner
                    .query(conn, "SELECT * FROM $tableName", MapListHandler());
            return@use mapList.map {
                KtObjectMapper.mapper.convertValue(it, clazz)
            }
        }
    }

    fun fetchCol(): Col? {
        return getConnection(true).use { conn ->
            val map = runner
                    .query(conn, "SELECT * FROM col", MapHandler())
            return@use KtObjectMapper.mapper.convertValue(map, Col::class.java)
        }
    }

    private fun getConnection(autoCommit: Boolean): Connection {
        Class.forName("org.sqlite.JDBC")
        val dbPath = sqliteDbResolvable.getSqliteDb().getAbsolutePath()
        val conn = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        conn.autoCommit = autoCommit
        return conn
    }
}

// https://github.com/freewind-demos/kotlin-sql2o-postgres-demo/blob/master/src/main/kotlin/example/Hello.kt
fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        try {
            this?.close()
        } catch (e: Exception) {
            println(e.toString())
        }
    }
}
