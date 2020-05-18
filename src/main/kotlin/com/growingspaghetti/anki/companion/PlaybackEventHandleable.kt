package com.growingspaghetti.anki.companion

import javazoom.jl.player.advanced.PlaybackEvent

interface PlaybackEventHandleable {
    fun handlePlayback(evt: PlaybackEvent)
}