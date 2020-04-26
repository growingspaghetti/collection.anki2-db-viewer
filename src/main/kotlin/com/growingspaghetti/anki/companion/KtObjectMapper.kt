@file:JvmName("KtObjectMapper")
package com.growingspaghetti.anki.companion

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

object KtObjectMapper {
    @kotlin.jvm.JvmField
    val mapper = ObjectMapper().registerModule(KotlinModule())
}