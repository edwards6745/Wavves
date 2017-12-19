package umsl.edu.wavvesinterface.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Austin on 5/11/2017.
 */

public class AudioDbHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "edu.umsl.audio";

    public AudioDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final String AudioTable = "CREATE TABLE " +
            AudioSchema.SongTable.NAME + " (" +
            AudioSchema.SongTable.Columns.DATA + " TEXT PRIMARY KEY, " +
            AudioSchema.SongTable.Columns.TITLE + " TEXT, " +
            AudioSchema.SongTable.Columns.ALBUM + " TEXT, " +
            AudioSchema.SongTable.Columns.ARTIST + " TEXT " +
            ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AudioTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AudioSchema.SongTable.NAME);

        onCreate(db);
    }
}
