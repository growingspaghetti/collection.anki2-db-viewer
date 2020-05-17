package com.growingspaghetti.anki.companion;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.InputStream;

import static java.lang.System.out;

public class Mp3Player {
  private final ColCardNoteRevsSwingList colCardNoteRevsSwingList;

  public Mp3Player(ColCardNoteRevsSwingList colCardNoteRevsSwingList) {
    this.colCardNoteRevsSwingList = colCardNoteRevsSwingList;
  }

  public void play(InputStream mp3file) throws JavaLayerException {

    AudioDevice device = FactoryRegistry.systemRegistry().createAudioDevice();
    // create an MP3 player
    AdvancedPlayer player = new AdvancedPlayer(mp3file, device);
    player.setPlayBackListener(new PlaybackListener() {
      @Override
      public void playbackStarted(PlaybackEvent evt) {
        out.println("[Playback] started.");
      }
      @Override
      public void playbackFinished(PlaybackEvent evt) {
        out.println("[Playback] finished.");
        colCardNoteRevsSwingList.selectNext();
      }
    });
    // play it!
    player.play();
  }

}