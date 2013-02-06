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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;

import com.kyakujin.android.tagnotepad.Config;
import com.kyakujin.android.tagnotepad.R;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Mapping;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Notes;
import com.kyakujin.android.tagnotepad.util.FragmentUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * ノート編集のフラグメントクラス。 <br>
 * 他フラグメントから遷移してきたとき、下記各々のアクションによってフラグメント起動時の表示を変える。<br>
 * 新規作成の場合は{@link Config#ACTION_CREATE}、<br>
 * 既存データの編集の場合は{@link Config#ACTION_OPEN}、<br>
 * 他コンポーネントからデータが送信されたときは{@link Config#ACTION_SENDED}。<br>
 * また、ノートの編集中状態を{@link #STATE_EDIT}、閲覧中状態を{@link #STATE_VIEW}と設定し、
 * その状態によって編集可否とオプションメニュー表示を切り替える.
 */
public class NoteEditFragment extends SherlockFragment {

    private static final String TAG = "NoteEditFragment";

    /**
     * New instance.
     *
     * @return an instance of {@link NoteEditFragment}
     */
    public static Fragment newInstance() {
        return new NoteEditFragment();
    }

    private Menu mMenu = null;

    // ノートのタイトル
    private EditText mTitleText;

    // ノートの本文
    // [注釈]
    // 編集状態の時はmBodyTextを表示し、
    // 閲覧状態の時はmBodyTextの内容がコピーされたmBodyTextViewModeを表示する。
    // ノートデータ中のリンク(webアドレスなど)のクリック可否がEditTextのみでは正常に制御ができないため、
    // 同じ内容のデータをEditText(リンク不可)とTextView(リンク可)で分離した。
    private EditText mBodyText;
    private TextView mBodyTextViewMode;

    private TextView mCharCountText;
    private Uri mNoteUri;

    // 他フラグメント(コンポーネント)からの起動方法(新規、編集、送信)を示す
    private int mAction = 0;
    // ノートの状態(編集状態、閲覧状態)を示す
    private int mEditorState = 0;

    /** ノート編集状態 */
    private static final int STATE_VIEW = 0;
    /** ノート閲覧状態 */
    private static final int STATE_EDIT = 1;

    // リストプリファレンス(文字数カウンタ)のentryvalue
    private int mPrefValue_Counter;

    /*
     * (非 Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getSherlockActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
     * android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_note_edit, container, false);

        mTitleText = (EditText) v.findViewById(R.id.note_title);
        mBodyText = (EditText) v.findViewById(R.id.note_body);
        mBodyTextViewMode = (TextView) v.findViewById(R.id.note_body_viewmode);

        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        int listValueFont = Integer.parseInt(sp.getString("list_fontsize_key", "3"));

        // フォントサイズの設定
        float fontSize;
        switch (listValueFont) {
            case 1:
                fontSize = 26.0f;
                break;
            case 2:
                fontSize = 22.0f;
                break;
            case 3:
                fontSize = 18.0f;
                break;
            case 4:
                fontSize = 14.0f;
                break;
            case 5:
                fontSize = 10.0f;
                break;
            default:
                fontSize = 24.0f;
                break;
        }
        mTitleText.setTextSize(fontSize);
        mBodyText.setTextSize(fontSize);
        mBodyTextViewMode.setTextSize(fontSize);

        // 文字数カウンタの設定
        mPrefValue_Counter = Integer.parseInt(sp.getString("list_charcounter_key", "2"));
        mCharCountText = (TextView) v.findViewById(R.id.charCounter);
        if (mPrefValue_Counter == 3) {
            mCharCountText.setText("");
        } else {
            mBodyText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mCharCountText != null) {
                        if (mPrefValue_Counter == 1) {
                            mCharCountText.setText(getString(R.string.description_char_count)
                                    + String.valueOf(s.toString().replaceAll("\n", "").length()));
                        } else if (mPrefValue_Counter == 2) {
                            mCharCountText.setText(getString(R.string.description_char_count)
                                    + String.valueOf(s.toString().length()));
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
            });
        }

        switch (getArguments().getInt("action")) {
            case Config.ACTION_CREATE:
                mAction = Config.ACTION_CREATE;
                setEditorState(STATE_EDIT);
                break;
            case Config.ACTION_OPEN:
                mAction = Config.ACTION_OPEN;
                setEditorState(STATE_VIEW);
                mNoteUri = Notes.buildUri(getArguments().getString(Config.SELECTED_NOTEID));
                extractNoteData();
                break;
            case Config.ACTION_SENDED:
                mAction = Config.ACTION_SENDED;
                setEditorState(STATE_EDIT);
                portingNoteData();
                break;
            default:
                Log.e(TAG, "Unknown action, existing");
                getActivity().finish();
                return v;
        }

        return v;
    }

    /*
     * (非 Javadoc)
     * @see
     * com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(android
     * .view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // アクションバーの全てのオプションメニューを一旦すべて非表示にする.
        // (以降の処理で、現在のフラグメントに適したオプションメニューを再表示する)
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        setOpiotnsMenuVisibility(menu);
    }

    /**
     * Sets the opiotns menu visibility. <br>
     * {@link #mAction}の値に応じて{@link NoteEditFragment}で表示するオプションメニューを切り替える。
     * @param menu the new opiotns menu
     */
    private void setOpiotnsMenuVisibility(Menu menu) {
        switch (mAction) {
            case Config.ACTION_CREATE:
            case Config.ACTION_SENDED:
                menu.findItem(R.id.menu_save).setVisible(true);
                break;
            case Config.ACTION_OPEN:
                menu.findItem(R.id.menu_edit).setVisible(true);
                menu.findItem(R.id.menu_append_tag).setVisible(true);
                menu.findItem(R.id.menu_allnotes).setVisible(true);
                menu.findItem(R.id.menu_share).setVisible(true);
                break;
            default:
                break;
        }
        mMenu = menu;
    }

    /*
     * (非 Javadoc)
     * @see
     * com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(android
     * .view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;

        switch (item.getItemId()) {
            case R.id.menu_save:
                setEditorState(STATE_VIEW);
                saveNoteData();
                break;
            case R.id.menu_edit:
                setEditorState(STATE_EDIT);
                break;
            case R.id.menu_append_tag:
                Bundle bundle = new Bundle();
                bundle.putString("selectedNoteUri", mNoteUri.toString());
                FragmentManager manager = getActivity().getSupportFragmentManager();
                TagDialogListFragment fragment = TagDialogListFragment.newInstance();
                fragment.setArguments(bundle);
                fragment.show(manager, Config.TAG_TAGDIALOGLIST_FRAGM);
                break;
            case R.id.menu_allnotes:
                FragmentManager manager2 = getSherlockActivity().getSupportFragmentManager();
                FragmentUtils.replaceFragment(manager2, NoteListFragment.newInstance(),
                        null, Config.TAG_NOTELIST_FRAGM);
                break;
            case R.id.menu_share:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mTitleText.getText().toString());
                intent.putExtra(Intent.EXTRA_TEXT, mBodyText.getText().toString());
                startActivity(intent);
                break;
            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }
        return ret;
    }

    /*
     * (非 Javadoc)
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mEditorState == STATE_EDIT) {
            saveNoteData();
        }
    }

    /**
     * 現在編集中のノートを保存。
     */
    private void saveNoteData() {
        String title = mTitleText.getText().toString();

        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.description_untitled_note);
        }

        // ノートを更新(新規ノートの場合はinsert、既存ノートの場合はupdate)
        ContentValues values = new ContentValues();
        values.put(Notes.TITLE, title);
        values.put(Notes.BODY, mBodyText.getText().toString());
        values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
        if (mNoteUri != null) {
            getActivity().getContentResolver().update(mNoteUri, values, null, null);
        } else {
            mNoteUri = getActivity().getContentResolver().insert( Notes.CONTENT_URI, values);
        }

        // タグとノートを紐付けるmappingテーブルの更新
        int tagid = getArguments().getInt(Config.SELECTED_TAGID);
        if (tagid != 0) {
            ContentValues mappingValues = new ContentValues();
            mappingValues.put(Mapping.NOTEID, Notes.getId(mNoteUri));
            mappingValues.put(Mapping.TAGID, tagid);
            getActivity().getContentResolver().insert( Mapping.CONTENT_URI, mappingValues);
        }
    }

    /**
     * ノートデータをデータベースから抽出。<br>
     * {@link NoteListFragment}}で選択されたノートのデータをデータベースから抽出し、<br>
     * {@link NoteEditFragment}に反映する。
     */
    private void extractNoteData() {
        Activity context = getActivity();
        if (context != null) {
            if (mNoteUri != null) {
                Cursor c = null;
                try {
                    c = context.getContentResolver().query(mNoteUri,
                            NotesQuery.PROJECTION, null, null, null);
                    if (c.moveToFirst()) {
                        mTitleText.setText(c.getString(NotesQuery.TITLE));
                        mBodyText.setText(c.getString(NotesQuery.BODY));
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }

        mBodyTextViewMode.setText(mBodyText.getText());
    }

    /**
     * 共有データの反映。<br>
     * 他コンポーネントから共有されたデータを{@link NoteEditFragment}に反映する.
     */
    private void portingNoteData() {
        String strTitle = getArguments().getString(Config.SHARED_TITLE);
        if (strTitle != null) {
            mTitleText.setText(strTitle);
        }
        String strBody = getArguments().getString(Config.SHARED_BODY);
        if (strBody != null) {
            mBodyText.setText(strBody);
        }
    }

    /**
     * エディター状態の設定。
     *
     * @param state the new editor state
     */
    private void setEditorState(int state) {
        switch (state) {
            // 閲覧状態に設定
            case STATE_VIEW:
                if (mMenu != null) {
                    mMenu.findItem(R.id.menu_save).setVisible(false);
                    mMenu.findItem(R.id.menu_edit).setVisible(true);
                    mMenu.findItem(R.id.menu_append_tag).setVisible(true);
                    mMenu.findItem(R.id.menu_allnotes).setVisible(true);
                    mMenu.findItem(R.id.menu_share).setVisible(true);
                }

                // Title
                mTitleText.setBackgroundResource(R.drawable.shape_note_title_view);
                mTitleText.setFocusable(false);
                mTitleText.setFocusableInTouchMode(false);
                mTitleText.setEnabled(false);

                if (mBodyText.getText().length() != 0) {
                    mBodyTextViewMode.setText(mBodyText.getText());
                }

                // Body
                mBodyText.setVisibility(View.INVISIBLE);
                mBodyText.setFocusable(false);
                mBodyText.setFocusableInTouchMode(false);
                mBodyText.setEnabled(false);

                // Body(View Mode)
                mBodyTextViewMode.setVisibility(View.VISIBLE);
                mBodyTextViewMode.setFocusable(true);
                mBodyTextViewMode.setFocusableInTouchMode(true);
                mBodyTextViewMode.setEnabled(true);

                break;

            // 編集状態に設定
            case STATE_EDIT:
                if (mMenu != null) {
                    mMenu.findItem(R.id.menu_save).setVisible(true);
                    mMenu.findItem(R.id.menu_edit).setVisible(false);
                    mMenu.findItem(R.id.menu_append_tag).setVisible(false);
                    mMenu.findItem(R.id.menu_allnotes).setVisible(false);
                    mMenu.findItem(R.id.menu_share).setVisible(false);
                }

                // Title
                mTitleText.setBackgroundResource(R.drawable.shape_note_title_edit);
                mTitleText.setFocusable(true);
                mTitleText.setFocusableInTouchMode(true);
                mTitleText.setEnabled(true);

                // Body(View Mode)
                mBodyTextViewMode.setFocusable(false);
                mBodyTextViewMode.setFocusableInTouchMode(false);
                mBodyTextViewMode.setEnabled(false);
                mBodyTextViewMode.setVisibility(View.INVISIBLE);

                // Body
                mBodyText.setVisibility(View.VISIBLE);
                mBodyText.setFocusable(true);
                mBodyText.setFocusableInTouchMode(true);
                mBodyText.setEnabled(true);

                if (mBodyText.length() != 0) {
                    mBodyText.requestFocus();
                    mBodyText.setSelection(mBodyText.getText().length());
                }
                break;
            default:
                break;
        }

        mEditorState = state;
    }

    /**
     * Clear.
     * (現在未使用)
     */
    protected void clear() {
        mTitleText.setText(null);
        mBodyText.setText(null);
        mBodyTextViewMode.setText(null);
        mNoteUri = null;
    }

    /**
     * The Interface NotesQuery.
     */
    public interface NotesQuery {

        String[] PROJECTION = {
                Notes._ID,
                Notes.TITLE,
                Notes.BODY,
                Notes.CREATED_DATE,
                Notes.MODIFIED_DATE,
        };

        int NOTES_ID = 0;
        int TITLE = 1;
        int BODY = 2;
        int CREATED_DATE = 3;
        int MODIFIED_DATE = 4;
    }
}
