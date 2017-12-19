package umsl.edu.wavvesinterface.database;

/**
 * Created by Austin on 5/11/2017.
 */

public class AudioSchema {
    public static final class SongTable {
        public static final String NAME = "song";

        public static final class Columns {
            public static final String DATA = "data";
            public static final String TITLE = "title";
            public static final String ALBUM = "album";
            public static final String ARTIST = "artist";
        }
    }

}
