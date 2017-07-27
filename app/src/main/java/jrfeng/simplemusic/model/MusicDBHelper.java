package jrfeng.simplemusic.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "music_list.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_MUSIC_LIST = "musicList";

    public static final String COLUMN_PATH    = "songPath";
    public static final String COLUMN_NAME    = "songName";
    public static final String COLUMN_ARTIST  = "songArtist";
    public static final String COLUMN_ALBUM   = "songAlbum";
    public static final String COLUMN_YEAR    = "songYear";
    public static final String COLUMN_COMMENT = "songComment";
    public static final String COLUMN_IMAGE   = "songImage";

    private static final String CREATE_TABLE_musicList = "CREATE TABLE musicList (\n" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "songPath    TEXT NOT NULL UNIQUE,\n" +
            "songName    TEXT NOT NULL,\n" +
            "songArtist  TEXT DEFAULT \"未知\",\n" +
            "songAlbum   TEXT DEFAULT \"未知\",\n" +
            "songYear    TEXT DEFAULT \"未知\",\n" +
            "songComment TEXT DEFAULT \"未知\",\n" +
            "songImage   BLOB DEFAULT NULL\n" +
            ");";

    public MusicDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_musicList);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //升级数据库
    }
}
