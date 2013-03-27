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

import java.util.Arrays;

import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Mapping;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Notes;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Tags;
import com.kyakujin.android.tagnotepad.provider.TagNoteDatabase.Tables;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provider that stores {@link TagNoteContract} data.
 */
public class TagNoteProvider extends ContentProvider {

    private static final String TAG = "TagNoteProvider";

    private TagNoteDatabase mOpenHelper;

    private static final int NOTES = 100;
    private static final int NOTES_ID = 101;
    private static final int TAGS = 200;
    private static final int TAGS_ID = 201;
    private static final int MAPPING = 300;
    private static final int MAPPING_ID = 301;

    private static final UriMatcher sUriMatcher;
    static {
        final String authority = TagNoteContract.CONTENT_AUTHORITY;
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(authority, Tables.NOTES, NOTES);
        sUriMatcher.addURI(authority, Tables.NOTES + "/#", NOTES_ID);
        sUriMatcher.addURI(authority, Tables.TAGS, TAGS);
        sUriMatcher.addURI(authority, Tables.TAGS + "/#", TAGS_ID);
        sUriMatcher.addURI(authority, Tables.MAPPING, MAPPING);
        sUriMatcher.addURI(authority, Tables.MAPPING + "/#", MAPPING_ID);
    }

    /*
     * (非 Javadoc)
     * @see android.content.ContentProvider#onCreate()
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new TagNoteDatabase(getContext());
        return true;
    }

    /*
     * (非 Javadoc)
     * @see android.content.ContentProvider#getType(android.net.Uri)
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                return Notes.CONTENT_TYPE;
            case NOTES_ID:
                return Notes.CONTENT_ITEM_TYPE;
            case TAGS:
                return Tags.CONTENT_TYPE;
            case TAGS_ID:
                return Tags.CONTENT_ITEM_TYPE;
            case MAPPING:
                return Mapping.CONTENT_TYPE;
            case MAPPING_ID:
                return Mapping.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }
    }

    /*
     * (非 Javadoc)
     * @see android.content.ContentProvider#query(android.net.Uri,
     * java.lang.String[], java.lang.String, java.lang.String[],
     * java.lang.String)
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
//        Log.d(TAG,
//                "query: uri=" + uri + "," + "projection=" + Arrays.toString(projection)
//                        + "," + "selection=" + selection + "," + "selectionArgs="
//                        + Arrays.toString(selectionArgs));

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                if (selection != null && selection.contains(Tables.TAGS)) {
                    String order = " ";
                    if (sortOrder != null) {
                        order = " ORDER BY " + sortOrder;
                    }
                    String sql = "SELECT " + Tables.NOTES + "." + Notes._ID + ", "
                            + Tables.NOTES + "." + Notes.TITLE + ", "
                            + Tables.NOTES + "." + Notes.BODY + ", "
                            + Tables.NOTES + "." + Notes.CREATED_DATE + ", "
                            + Tables.NOTES + "." + Notes.MODIFIED_DATE
                            + " FROM " + Tables.NOTES
                            + " JOIN " + Tables.MAPPING
                            + " ON " + Tables.MAPPING + "." + Mapping.NOTEID + " = "
                            + Tables.NOTES + "." + Notes._ID
                            + " JOIN " + Tables.TAGS
                            + " ON " + Tables.MAPPING + "." + Mapping.TAGID + " = "
                            + Tables.TAGS + "." + Tags._ID
                            + selection + " " + order + ";";

                    Cursor c = db.rawQuery(sql, selectionArgs);
                    c.setNotificationUri(getContext().getContentResolver(), uri);
                    return c;
                } else {
                    qb.setTables(Tables.NOTES);
                }
                break;
            case NOTES_ID:
                qb.setTables(Tables.NOTES);
                qb.appendWhere(Notes._ID + "=" + Notes.getId(uri));
                break;
            case TAGS:
                qb.setTables(Tables.TAGS);
                // sortOrder = Tags.DEFAULT_SORT;
                break;
            case TAGS_ID:
                qb.setTables(Tables.TAGS);
                qb.appendWhere(Tags._ID + "=" + Tags.getId(uri));
                break;
            case MAPPING:
            case MAPPING_ID:
                qb.setTables(Tables.MAPPING);
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /*
     * (非 Javadoc)
     * @see android.content.ContentProvider#insert(android.net.Uri,
     * android.content.ContentValues)
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

//        Log.d(TAG, "insert: uri=" + uri + "," + "values=" + values);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        String hackString;
        Uri contentUri;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                tableName = Tables.NOTES;
                hackString = Notes.BODY;
                contentUri = Notes.CONTENT_URI;

                Long now = Long.valueOf(System.currentTimeMillis());

                if (!values.containsKey(Notes.CREATED_DATE)) {
                    values.put(Notes.CREATED_DATE, now);
                }
                if (!values.containsKey(Notes.MODIFIED_DATE)) {
                    values.put(Notes.MODIFIED_DATE, now);
                }
                if (!values.containsKey(Notes.TITLE)) {
                    values.put(Notes.TITLE, "");
                }
                if (!values.containsKey(Notes.BODY)) {
                    values.put(Notes.BODY, "");
                }
                break;
            case TAGS:
                tableName = Tables.TAGS;
                hackString = Tags.TAGNAME;
                contentUri = Tags.CONTENT_URI;
                break;
            case MAPPING:
                tableName = Tables.MAPPING;
                hackString = Mapping.NOTEID;
                contentUri = Mapping.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        // SDKバージョンが異なると、insertWithOnConflict()でCONFLICT_IGNOREを指定した場合の
        // 一意性違反発生時の戻り値が以下のように異なる。
        // [SDKバージョン → rowId]
        // Android 2.3.4以下 → 0
        // Android 3.1以上 → -1
        // したがって以下のような処理を記述した。
        long rowId = 0;
        try {
            rowId = db.insertWithOnConflict(tableName, hackString,
                    values, SQLiteDatabase.CONFLICT_IGNORE);
        } catch (SQLException e) {
            Log.e(TAG, "Failed to insert: " + rowId, e);
            return null;
        }

        if (rowId > 0) {
            Uri insertUri = ContentUris.withAppendedId(contentUri, rowId);
            getContext().getContentResolver().notifyChange(insertUri, null);
            return insertUri;
        }
//        Log.d("debug", "insert: rowId = " + rowId);
        return null;
    }

    /*
     * (非 Javadoc)
     * @see android.content.ContentProvider#delete(android.net.Uri,
     * java.lang.String, java.lang.String[])
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        //Log.d(TAG, "delete: uri=" + uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        String whereClause = where;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                tableName = Tables.NOTES;
                break;
            case NOTES_ID:
                tableName = Tables.NOTES;
                whereClause = Notes._ID + " == " + Notes.getId(uri)
                        + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : "");
                break;
            case TAGS:
                tableName = Tables.TAGS;
                break;
            case TAGS_ID:
                tableName = Tables.TAGS;
                whereClause = Tags._ID + " == " + Tags.getId(uri)
                        + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : "");
                break;
            case MAPPING:
                tableName = Tables.MAPPING;
                break;
            case MAPPING_ID:
                tableName = Tables.MAPPING;
                whereClause = Mapping._ID + " == " + Mapping.getId(uri)
                        + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        int count = db.delete(tableName, whereClause, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    /*
     * (非 Javadoc)
     * @see android.content.ContentProvider#update(android.net.Uri,
     * android.content.ContentValues, java.lang.String, java.lang.String[])
     */
    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
//        Log.d(TAG, "update:uri=" + uri + "," + "values=" + values.toString());

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String tableName;
        String whereClause = where;
        switch (sUriMatcher.match(uri)) {
            case NOTES:
                tableName = Tables.NOTES;
                break;
            case NOTES_ID:
                tableName = Tables.NOTES;
                whereClause = Notes._ID + "=" + Notes.getId(uri)
                        + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : "");
                break;
            case TAGS:
                tableName = Tables.TAGS;
                break;
            case TAGS_ID:
                tableName = Tables.TAGS;
                whereClause = Tags._ID + "=" + Tags.getId(uri)
                        + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : "");
                break;
            case MAPPING:
                tableName = Tables.MAPPING;
                break;
            case MAPPING_ID:
                tableName = Tables.MAPPING;
                whereClause = Mapping._ID + "=" + Mapping.getId(uri)
                        + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        int count = db.updateWithOnConflict(tableName, values, whereClause, whereArgs,
                SQLiteDatabase.CONFLICT_REPLACE);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    public void deleteDatabase() {
        mOpenHelper.close();
        Context context = getContext();
        TagNoteDatabase.deleteDatabase(context);
        mOpenHelper = new TagNoteDatabase(getContext());
    }
}
