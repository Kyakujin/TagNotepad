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

package com.kyakujin.android.tagnotepad.ui;

import com.kyakujin.android.tagnotepad.Config;
import com.kyakujin.android.tagnotepad.R;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Mapping;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Notes;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Tags;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * ノートにタグを付加するためのダイアログクラス。
 */
public class TagDialogListFragment extends DialogFragment implements LoaderCallbacks<Cursor> {

    /**
     * New instance.
     *
     * @return an instance of {@link TagDialogListFragment}
     */
    public static TagDialogListFragment newInstance() {
        return new TagDialogListFragment();
    }

    private String mText;
    private EditText mNewTagname;
    private Button mCreateNewTag;
    private static Uri mCurrentNote;
    private SimpleCursorAdapter mTagListAdapter;
    private ListView mTagListView;
    private LoaderManager mManager;

    /* (非 Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_tag_dialog_list, null, false);

        if (getArguments() != null) {
            mCurrentNote = Uri.parse(getArguments().getString(Config.SELECTED_NOTE_URI));
        }

        mTagListView = (ListView) view.findViewById(R.id.taglist);
        mNewTagname = (EditText) view.findViewById(R.id.tagname);
        mCreateNewTag = (Button) view.findViewById(R.id.button_createtag);

        mCreateNewTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //
                // step1: 現在チェックがONになっているタグを紐付ける
                //
                updateMappingTable();

                //
                // step2: 新規に作成したタグを紐付ける
                //
                mText = mNewTagname.getText().toString();
                if (mText.length() == 0) {
                    mText = getString(R.string.description_unnamed_tag);
                }

                // tagsテーブルへ新規データ作成
                ContentValues cv1 = new ContentValues();
                cv1.put(Tags.TAGNAME, mText);
                Uri uri = getActivity().getContentResolver().insert(Tags.CONTENT_URI, cv1);

                // mappingテーブルへ新規データ作成
                // (選択したノートと作成したタグを紐付ける)
                if (uri != null) {
                    String nid = Notes.getId(mCurrentNote);
                    String tid = Tags.getId(uri);
                    ContentValues cv2 = new ContentValues();
                    cv2.put(Mapping.NOTEID, nid);
                    cv2.put(Mapping.TAGID, tid);
                    getActivity().getContentResolver().insert(Mapping.CONTENT_URI, cv2);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_apend_tag);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                updateMappingTable();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.setView(view);

        // LoaderManager を取得
        mManager = getLoaderManager();
        //LoaderManager.enableDebugLogging(true);

        fillTagList();

        return builder.create();
    }

    /**
     * データベースから抽出したタグ名一覧をリストに反映。
     */
    private void fillTagList() {
        mTagListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.checkedtextview, null,
                new String[] {
                        Tags.TAGNAME
                },
                new int[] {
                        R.id.checkedtextview
                },
                0);

        mTagListView.setAdapter(mTagListAdapter);
        mTagListView.setItemsCanFocus(false);
        mTagListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mManager.restartLoader(TagsQuery.LOADER_ID, null, this);
    }

    /**
     * Sets the check.
     */
//    private void setCheck() {
//        mManager.restartLoader(MappingQuery.LOADER_ID, null, this);
//    }

    /**
     * MappingTableの更新。
     * 選択したノートのIDに紐付けられているmappingテーブルのデータを一旦削除。
     * 次に、そのノートIDと、チェックボックスでONされたタグを紐付ける。
     */
    private void updateMappingTable() {
        // 選択したノートのIDに該当する行を削除
        String noteId = Notes.getId(mCurrentNote);
        String where = Mapping.NOTEID + " = " + noteId;
        getActivity().getContentResolver().delete(Mapping.CONTENT_URI, where, null);

        // チェックしたタグとノートのマッピングデータを新たにインサート
        SparseBooleanArray checkedArray = mTagListView.getCheckedItemPositions();
        ContentValues values = new ContentValues();
        Cursor c;
        for (int i = 0; i < mTagListView.getCount(); i++) {
            if (checkedArray.get(i)) {
                c = (Cursor) mTagListView.getItemAtPosition(i);
                values.put(Mapping.NOTEID, noteId);
                values.put(Mapping.TAGID, c.getString(TagsQuery.TAGS_ID));
                getActivity().getContentResolver().insert(Mapping.CONTENT_URI, values);
            }
        }
        checkedArray.clear();
    }

    /* (非 Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg) {
        switch (id) {
            case TagsQuery.LOADER_ID:
                return new CursorLoader(getActivity(), Tags.CONTENT_URI,
                        TagsQuery.PROJECTION, null, null, Tags.TAGNAME);

            case MappingQuery.LOADER_ID:
                String select = Mapping.NOTEID + " = " + Notes.getId(mCurrentNote);
                return new CursorLoader(getActivity(), Mapping.CONTENT_URI,
                        MappingQuery.PROJECTION, select, null, null);
            default:
                break;
        }
        return null;
    }

    /* (非 Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case TagsQuery.LOADER_ID:
                mTagListAdapter.swapCursor(data);
                //setCheck();
                mManager.restartLoader(MappingQuery.LOADER_ID, null, this);
                break;

            case MappingQuery.LOADER_ID:
                // 選択されたノートに紐付けられているタグ名のチェックボックスをONにする
                if (data.getCount() != 0) {
                    Cursor tCur;
                    for (int i = 0; i < mTagListView.getCount(); i++) {
                        tCur = (Cursor) mTagListView.getItemAtPosition(i);
                        data.moveToFirst();
                        do {
                            if (tCur.getInt(TagsQuery.TAGS_ID) == data
                                    .getInt(MappingQuery.TAGID)) {
                                mTagListView.setItemChecked(tCur.getPosition(), true);
                            }
                        } while (data.moveToNext());
                    }
                }
                break;
            default:
                break;
        }
    }

    /* (非 Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TagsQuery.LOADER_ID:
                mTagListAdapter.swapCursor(null);
                break;
//            case MappingQuery.LOADER_ID:
//                mTagListAdapter.swapCursor(null);
//                break;
            default:
                return;
        }
    }

    /**
     * The Interface TagsQuery.
     */
    private interface TagsQuery {

        int LOADER_ID = 0;

        String[] PROJECTION = {
                Tags._ID,
                Tags.TAGNAME,
        };

        int TAGS_ID = 0;
        int TAGNAME = 1;
    }

    /**
     * The Interface MappingQuery.
     */
    private interface MappingQuery {

        int LOADER_ID = 1;

        String[] PROJECTION = {
                Mapping._ID,
                Mapping.NOTEID,
                Mapping.TAGID,
        };

        int MAPPING_ID = 0;
        int NOTEID = 1;
        int TAGID = 2;
    }
}
