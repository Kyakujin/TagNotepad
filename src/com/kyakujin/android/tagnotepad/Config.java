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

package com.kyakujin.android.tagnotepad;

/**
 * Configuration data.
 */
public class Config {

    public static final String SEARCH_WORD = "searchWord";
    public static final String SELECTED_NOTEID = "selectedNoteId";
    public static final String SELECTED_NOTE_URI = "selectedNoteUri";
    public static final String SELECTED_TAGID = "selectedTagId";
    public static final String SELECTED_TAGNAME = "selectedTagName";
    public static final String SELECTED_TAG_URI = "selectedTagUri";
    public static final String SHARED_TITLE = "sharedTitle";
    public static final String SHARED_BODY = "sharedBody";
    public static final String TAG_NOTELIST_FRAGM = "Tag_NoteListFragment";
    public static final String TAG_NOTEEDIT_FRAGM = "Tag_NoteEditFragment";
    public static final String TAG_TAGLIST_FRAGM = "Tag_TagListFragment";
    public static final String TAG_TAGDIALOGEDIT_FRAGM = "Tag_TagDialogEditFragment";
    public static final String TAG_TAGDIALOGLIST_FRAGM = "Tag_TagDialogListFragment";
    public static final String TAG_ABOUT_FRAGM = "Tag_AboutFragment";

    // NoteEditFragment}起動時の動作種別
    // ノートの新規作成
    public static final int ACTION_CREATE = 0;
    // ノートのオープン
    public static final int ACTION_OPEN = 1;
    // ノートの共有
    public static final int ACTION_SENDED = 2;
}
