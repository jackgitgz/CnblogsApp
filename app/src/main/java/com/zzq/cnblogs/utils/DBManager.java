package com.zzq.cnblogs.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zzq on 2015/10/21.
 */
public class DBManager extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static DBManager dbManager;

    public DBManager(Context context) {
        super(context, "articleDb", null, DATABASE_VERSION);
    }

    public static DBManager getSQLiteInstance(Context context){
        if(dbManager != null){
            dbManager = new DBManager(context);
        }

        return dbManager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE `article` (" +
                "  `linkmd5id` text PRIMARY KEY," +
                "  `title` text ," +
                "  `desc` text ," +
                "  `link` text ," +
                "  `listUrl` text ," +
                "  `view` INTEGER ," +
                "  `comment` INTEGER ," +
                "  `diggnum` INTEGER ," +
                " `is_favorite` INTEGER" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
