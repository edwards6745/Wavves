package umsl.edu.wavvesinterface;

import android.content.Context;

import java.util.ArrayList;

import umsl.edu.wavvesinterface.database.Audio;
import umsl.edu.wavvesinterface.database.Song;

/**
 * Created by Austin on 5/10/2017.
 */

public class SongModel {
    private ArrayList<Song> mAudio;
    private SongInfoPersistence mPersistence;

    public SongModel(Context context) {
        mPersistence = SongInfoPersistence.sharedInstance(context);
    }

    public String getSongName(int pos) {
        return mAudio.get(pos).getTitle();
    }

    public ArrayList<Song> getAudio() {
        return mPersistence.getAudio();
    }

}
