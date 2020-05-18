package com.growingspaghetti.anki.companion

import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

const val ONE_DAY = 86400000
val DATE_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd")
        .withZone(ZoneId.systemDefault())
val SIMPLE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
val MP3_PA = Pattern.compile("\\[sound:([^]]+?)]")
val IMG_PA = Pattern.compile("<img.*? src=\"([^\"]+?)\".?>")