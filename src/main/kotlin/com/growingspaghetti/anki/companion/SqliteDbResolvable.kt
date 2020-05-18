package com.growingspaghetti.anki.companion

import java.io.File

interface SqliteDbResolvable {
    fun sqliteDb(): File
    fun collectionMediaDir() =
            sqliteDb().parentFile.listFiles()!!
                    .find { it.name == "collection.media" }!!
}
