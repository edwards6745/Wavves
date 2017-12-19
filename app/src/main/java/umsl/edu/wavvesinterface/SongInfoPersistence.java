package umsl.edu.wavvesinterface;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

import umsl.edu.wavvesinterface.database.Audio;
import umsl.edu.wavvesinterface.database.AudioCursorWrapper;
import umsl.edu.wavvesinterface.database.AudioDbHelper;
import umsl.edu.wavvesinterface.database.Song;

/**
 * Created by Austin on 5/10/2017.
 */

public class SongInfoPersistence extends AppCompatActivity{

    private SQLiteDatabase mDatabase;
    private Context mContext;
    public static SongInfoPersistence sPersistence;

    public static SongInfoPersistence sharedInstance(Context context) {
        if(sPersistence == null) {
            sPersistence = new SongInfoPersistence(context);
        }
        return sPersistence;
    }

    private SongInfoPersistence(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new AudioDbHelper(context).getWritableDatabase();
    }

    public ArrayList<Song> getAudio() {
        ContentResolver contentResolver = getContentResolver();
        ArrayList<Song> song = null;

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                song.add(new Song(data, title, album, artist));
            }
        }
        cursor.close();
        Log.e("Num Songs", "" + song.size());
        return song;
    }
}
