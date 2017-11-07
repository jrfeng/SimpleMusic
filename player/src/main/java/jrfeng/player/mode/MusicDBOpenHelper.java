package jrfeng.player.mode;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class MusicDBOpenHelper extends SQLiteOpenHelper {
    static final String TABLE_ALL_MUSIC = "ALL_MUSIC";
    static final String TABLE_I_LOVE = "I_LOVE";
    static final String TABLE_RECENT_PLAY = "RECENT_PLAY";
    static final String TABLE_MUSIC_LIST_INDEX = "MUSIC_SHEET_INDEX";

    private static final String DATABASE_NAME = "MUSIC_DATABASE.db";    //数据库名称
    private static final int VERSION = 1;                               //数据库版本，升级数据库时请修改该值

    private static final String CREATE_TABLE_ALL_MUSIC = "CREATE TABLE " + TABLE_ALL_MUSIC + " (\n" +
            "    id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    path     TEXT NOT NULL UNIQUE,\n" +
            "    name     TEXT NOT NULL,\n" +
            "    artist   TEXT DEFAULT \"未知\",\n" +
            "    album    TEXT DEFAULT \"未知\",\n" +
            "    year     TEXT DEFAULT \"未知\",\n" +
            "    comment  TEXT DEFAULT \"未知\"\n" +
            ");";

    private static final String CREATE_TABLE_I_LOVE = "CREATE TABLE " + TABLE_I_LOVE + " (\n" +
            "    id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    path     TEXT NOT NULL UNIQUE,\n" +
            "    name     TEXT NOT NULL,\n" +
            "    artist   TEXT DEFAULT \"未知\",\n" +
            "    album    TEXT DEFAULT \"未知\",\n" +
            "    year     TEXT DEFAULT \"未知\",\n" +
            "    comment  TEXT DEFAULT \"未知\"\n" +
            ");";

    private static final String CREATE_TABLE_RECENT_PLAY = "CREATE TABLE " + TABLE_RECENT_PLAY + " (\n" +
            "    id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    path     TEXT NOT NULL UNIQUE,\n" +
            "    name     TEXT NOT NULL,\n" +
            "    artist   TEXT DEFAULT \"未知\",\n" +
            "    album    TEXT DEFAULT \"未知\",\n" +
            "    year     TEXT DEFAULT \"未知\",\n" +
            "    comment  TEXT DEFAULT \"未知\"\n" +
            ");";

    private static final String CREATE_TABLE_MUSIC_SHEET_INDEX = "CREATE TABLE " + TABLE_MUSIC_LIST_INDEX + " (\n" +
            "    id        INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    name      TEXT NOT NULL,\n" +
            "    tableName TEXT NOT NULL\n" +
            ");";

    MusicDBOpenHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_ALL_MUSIC);
        sqLiteDatabase.execSQL(CREATE_TABLE_I_LOVE);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECENT_PLAY);
        sqLiteDatabase.execSQL(CREATE_TABLE_MUSIC_SHEET_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //数据库升级逻辑(保留，暂无需求)
        throw new UnsupportedOperationException("请实现数据库升级逻辑");
    }
}
