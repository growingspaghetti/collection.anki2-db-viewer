package com.growingspaghetti.anki.companion

import javazoom.jl.decoder.JavaLayerException
import javazoom.jl.player.FactoryRegistry
import javazoom.jl.player.advanced.AdvancedPlayer
import javazoom.jl.player.advanced.PlaybackEvent
import javazoom.jl.player.advanced.PlaybackListener
import java.io.InputStream

object Mp3Player {
    private var player: AdvancedPlayer? = null

    @Throws(JavaLayerException::class)
    fun play(mp3file: InputStream, handler: PlaybackEventHandleable) {
        this.player?.close()
        val device = FactoryRegistry.systemRegistry().createAudioDevice()
        val player = AdvancedPlayer(mp3file, device)
        player.playBackListener = object : PlaybackListener() {
            override fun playbackStarted(evt: PlaybackEvent) {
            }

            override fun playbackFinished(evt: PlaybackEvent) {
                handler.handlePlayback(evt)
            }
        }
        this.player = player
        player.play()
    }

    fun stop() {
        this.player?.close()
    }
}
