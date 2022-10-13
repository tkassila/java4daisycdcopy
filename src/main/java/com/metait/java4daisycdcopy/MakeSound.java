package com.metait.java4daisycdcopy;

import java.io.File;
import java.io.IOException;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MakeSound {
    private File mediaFile = new File("assets/mistle_w.wav");
    private Media media = null;
    private MediaPlayer mp;
    private boolean isPlaying = false;

    public boolean play() {
        if (!isPlaying)
            return false;
        return startPlaying();
    }

    public boolean stop() {
        return stopPlaying();
    }

    /**
     * @param filename the name of the file that is going to be played
     */
    public boolean playSound(String filename){

        String strFilename = filename;

        try {
            mediaFile = new File(strFilename);
            String strResource = strFilename; // mediaFile.toURI().toString();
            media = new Media(strResource);
            mp = new MediaPlayer(media);
            mp.setAutoPlay(true);
            // mp.play();
            isPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            isPlaying = false;
        }
        return isPlaying;
    }

    private boolean startPlaying()
    {
        if(mp != null && !(mp.getStatus() == MediaPlayer.Status.PLAYING))
            mp.play();
        return isPlaying;
    }

    private boolean stopPlaying()
    {
      //  if (isPlaying) {
        if(mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING)
            mp.stop();
      //  }
        mp = null;
        isPlaying = false;
        return isPlaying;
    }
}