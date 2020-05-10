package com.growingspaghetti.anki.companion

import java.io.File

interface SqliteDbResolvable {
    fun sqliteDb(): File
}
