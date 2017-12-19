package umsl.edu.wavvesinterface.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;

/**
 * Created by Austin on 5/11/2017.
 */

public class AudioCursorWrapper extends CursorWrapper {
    public AudioCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Audio loadAudio() {
        String data = getString(getColumnIndex(AudioSchema.SongTable.Columns.DATA));
        String title = getString(getColumnIndex(AudioSchema.SongTable.Columns.TITLE));
        String album = getString(getColumnIndex(AudioSchema.SongTable.Columns.ALBUM));
        String artist = getString(getColumnIndex(AudioSchema.SongTable.Columns.ARTIST));

        return new Audio(new Song(data, title, album, artist));
    }
}
