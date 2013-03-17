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

import com.kyakujin.android.tagnotepad.provider.TagNoteDatabase.Tables;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for interacting with {@link TagNoteProvider}.
 */
public class TagNoteContract {

    /**
     * notesテーブルのカラム
     */
    interface NotesColumns {
        /** ノートのタイトル名 */
        public static final String TITLE = "title";

        /** ノートの本文 */
        public static final String BODY = "body";

        /** ノート作成日 */
        public static final String CREATED_DATE = "created";

        /** ノートの更新日 */
        public static final String MODIFIED_DATE = "modified";
    }

    /**
     * tagsテーブルのカラム
     */
    interface TagsColumns {
        /** タグ名 */
        public static final String TAGNAME = "tagname";
    }

    /**
     * mappingテーブルのカラム
     * (ノートとタグを各_idで紐付けるためのテーブル)
     */
    interface MappingColumns {
        /** notesテーブルの_id */
        public static final String NOTEID = "noteid";

        /** tagsテーブルの_id */
        public static final String TAGID = "tagid";
    }

    public static final String CONTENT_AUTHORITY = "com.kyakujin.android.tagnotepad";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_NOTES = "notes";
    private static final String PATH_TAGS = "tags";
    private static final String PATH_MAPPING = "mapping";

    /**
     * ノートのクラス
     */
    public static class Notes implements NotesColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.tagnotepad.note";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.tagnotepad.note";

        /** クエリ時のデフォルトソート */
        public static final String DEFAULT_SORT = NotesColumns.MODIFIED_DATE + " DESC";

        /** 任意のタグに紐付けられたノートを表示する時のwhere条件 */
        public static final String TAG_SELECTION =
                Tables.TAGS + "." + Tags.TAGNAME + " =?";

        /** 検索ワードによるノート検索時のwhere条件 */
        public static final String SEARCH_SELECTION =
                Tables.NOTES + "." + Notes.TITLE + "  LIKE '%' || ? || '%'"
                 + " ESCAPE '$'"
                + " OR " + Tables.NOTES + "." + Notes.BODY + "  LIKE '%' || ? || '%'"
                + " ESCAPE '$'" ;

        /**
         * 指定したノートID(_idの値)からURIを生成
         * @param noteId the note id
         * @return the uri
         */
        public static Uri buildUri(String noteId) { // TODO このメソッド必要か？以下同様のメソッドも
            return CONTENT_URI.buildUpon().appendPath(noteId).build();
        }

        /**
         * 指定したURIからノートID(_idの値)を返却
         * @param uri the uri
         * @return the id
         */
        public static String getId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * タグのクラス
     */
    public static class Tags implements TagsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.tagnotepad.tag";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.tagnotepad.tag";

        /** クエリ時のデフォルトソート */
        public static final String DEFAULT_SORT = Tags.TAGNAME + " ASC";

        /**
         * 指定したタグID(_idの値)からURIを生成
         * @param tagId the tag id
         * @return the uri
         */
        public static Uri buildUri(String tagId) {
            return CONTENT_URI.buildUpon().appendPath(tagId).build();
        }

        /**
         * 指定したURIからタグID(_idの値)を返却
         * @param uri the uri
         * @return the id
         */
        public static String getId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * ノートとタグの紐付けを保存するmappingテーブルのクラス
     */
    public static class Mapping implements MappingColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MAPPING).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.tagnotepad.mapping";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.tagnotepad.mapping";

        /** クエリ時のデフォルトソート */
        public static final String DEFAULT_SORT = MappingColumns.NOTEID + " ASC";

        /**
         * 指定したマッピングID(_idの値)からURIを生成
         * @param mappingId the mapping id
         * @return the uri
         */
        public static Uri buildUri(String mappingId) {
            return CONTENT_URI.buildUpon().appendPath(mappingId).build();
        }

        /**
         * 指定したURIからマッピングID(_idの値)を返却
         * @param uri the uri
         * @return the id
         */
        public static String getId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    private TagNoteContract() {
    }
}
