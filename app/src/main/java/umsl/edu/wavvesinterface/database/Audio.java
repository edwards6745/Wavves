package umsl.edu.wavvesinterface.database;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Austin Edwards on 5/2/2017.
 */

public class Audio implements Serializable {
    private ArrayList<Song> audioSongs;

    public Audio() {
        this.audioSongs = new ArrayList<>();
    }

    public Audio(Song songData) {
        this.audioSongs = new ArrayList<>();
        this.audioSongs.add(songData);
    }

    public ArrayList<Song> getAudioSongs() {
        return audioSongs;
    }

    public void setAudioSongs(Song songData) {
        this.audioSongs.add(songData);
    }
}
