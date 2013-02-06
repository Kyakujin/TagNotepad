/*
 * Copyright 2013 Yoshihiro Miyama
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kyakujin.android.tagnotepad.provider;

import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Mapping;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Notes;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Tags;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Helper for managing {@link SQLiteDatabase} that stores data for
 * {@link TagNoteProvider}.
 */
public class TagNoteDatabase extends SQLiteOpenHelper {

    private static final String TAG = "TagNoteDatabase";

    private static final String DATABASE_NAME = "tagnotepad.db";

    private static final int DATABASE_VERSION = 1;

    interface Tables {
        String NOTES = "notes";
        String TAGS = "tags";
        String MAPPING = "mapping";
    }

    /** ノート削除時に、タグとの紐付けを削除するトリガー */
    interface Triggers {
        String MAPPING_DELETE = "mapping_delete";
    }

    public TagNoteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* (非 Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.NOTES + " ("
                + Notes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Notes.TITLE + " TEXT, "
                + Notes.BODY + " TEXT, "
                + Notes.CREATED_DATE + " INTEGER, "
                + Notes.MODIFIED_DATE + " INTEGER"
                + ");");

        db.execSQL("CREATE TABLE " + Tables.TAGS + " ("
                + Tags._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Tags.TAGNAME + " TEXT, "
                + "UNIQUE(" + Tags.TAGNAME + ")"
                + ");");

        db.execSQL("CREATE TABLE " + Tables.MAPPING + " ("
                + Mapping._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Mapping.NOTEID + " INTEGER, "
                + Mapping.TAGID + " INTEGER, "
                + "UNIQUE(" + Mapping.NOTEID + ", " + Mapping.TAGID + ")"
                + ");");

        db.execSQL("CREATE TRIGGER " + Triggers.MAPPING_DELETE
                + " AFTER DELETE ON " + Tables.NOTES
                + " BEGIN DELETE FROM " + Tables.MAPPING + " "
                + " WHERE " + Mapping.NOTEID + " =old." + Notes._ID
                + ";" + " END;");
    }

    /* (非 Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "upgrading database from version " + oldVersion + " to"
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.TAGS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.MAPPING);

        onCreate(db);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
