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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import com.kyakujin.android.tagnotepad.Config;
import com.kyakujin.android.tagnotepad.util.FragmentUtils;
import com.kyakujin.android.tagnotepad.R;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Notes;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * ノートをリスト表示するフラグメントクラス。
 */
public class NoteListFragment extends SherlockListFragment {

    private static final String TAG = "NoteListFragment";

    /**
     * New instance.
     *
     * @return an instance of {@link NoteListFragment}
     */
    public static Fragment newInstance() {
        return new NoteListFragment();
    }

    private SimpleCursorAdapter mNoteListAdapter;
    private ListView mNoteListView;
    private LoaderManager mManager;
    private String mSearchWord;
    private Uri mCurrentNote;

    // ソートオーダー
    private String mOrder;

    // TagListFragmentから遷移してきた場合、
    // そのTagListFragmentにて選択されたタグ名が格納される。
    private String mSelectedTag;

    /*
     * (非 Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater
     * , android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_note_list, container, false);
        mNoteListView = (ListView) v.findViewById(android.R.id.list);

        mSelectedTag = getArguments() != null ? getArguments().getString(
                Config.SELECTED_TAGNAME) : null;
        if (mSelectedTag != null) {
            TextView header = (TextView) v.findViewById(R.id.notelist_header);
            header.setText(getString(R.string.description_tag) + " [ " + mSelectedTag + " ] "
                    + getString(R.string.description_appended_notes));
        }

        mSearchWord =
                getArguments() != null ? getArguments().getString(Config.SEARCH_WORD) : null;

        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
        int listValueSort = Integer.parseInt(sp.getString("list_sort_key", "3"));
        switch (listValueSort) {
            case 1: // タイトル順(昇順)
                mOrder = Notes.TITLE + " asc";
                break;
            case 2: // タイトル順(降順)
                mOrder = Notes.TITLE + " desc";
                break;
            case 3: // 変更順(新しい順)
                mOrder = Notes.MODIFIED_DATE + " desc";
                break;
            case 4: // 変更順(古い順)
                mOrder = Notes.MODIFIED_DATE + " asc";
                break;
            default:
                break;
        }

        return v;
    }

    /*
     * (非 Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = new Intent();
        intent.setData(Notes.CONTENT_URI);

        getListView().setOnCreateContextMenuListener(this);

        mNoteListAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item_note, null,
                new String[] {
                        Notes.TITLE,
                        Notes.MODIFIED_DATE
                },
                new int[] {
                        R.id._title,
                        R.id._created_time
                },
                0);

        mNoteListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == NotesQuery.MODIFIED_DATE) {
                    long modified = cursor.getLong(NotesQuery.MODIFIED_DATE);

                    Date date = new Date(modified);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String text = simpleDateFormat.format(date);
                    ((TextView) view).setText(text);
                    return true;
                }
                return false;
            }
        });

        mNoteListView.setAdapter(mNoteListAdapter);

        mManager = getLoaderManager();
        mManager.restartLoader(NotesQuery.LOADER_ID, null, mLoaderCallbacks);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.ListFragment#onListItemClick(android.widget.ListView
     * , android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        openNote(ContentUris.withAppendedId(Notes.CONTENT_URI, id));
    }

    /*
     * (非 Javadoc)
     * @see
     * com.actionbarsherlock.app.SherlockListFragment#onCreateOptionsMenu(android
     * .view.Menu, android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // アクションバーの全てのオプションメニューを一旦すべて非表示にする。
        // (以降の処理で、現在のフラグメントに適したオプションメニューを再表示する)
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        setOpiotnsMenuVisibility(menu);
    }

    /**
     * ノートリスト画面に適したオプションメニューを表示する。
     *
     * @param menu the new opiotns menu
     */
    private void setOpiotnsMenuVisibility(Menu menu) {
        menu.findItem(R.id.menu_tag).setVisible(true);
        menu.findItem(R.id.menu_add_note).setVisible(true);
        menu.findItem(R.id.menu_allnotes).setVisible(true);
        menu.findItem(R.id.menu_search).setVisible(true);
        menu.findItem(R.id.menu_preference).setVisible(true);
        menu.findItem(R.id.menu_about).setVisible(true);
    }

//    /*
//     * (非 Javadoc)
//     * @see
//     * com.actionbarsherlock.app.SherlockListFragment#onPrepareOptionsMenu(android
//     * .view.Menu)
//     */
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//    }

    /*
     * (非 Javadoc)
     * @see
     * com.actionbarsherlock.app.SherlockListFragment#onOptionsItemSelected(
     * android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
            case R.id.menu_add_note:
                createNote();
                break;
            case R.id.menu_allnotes:
                allNoteList();
                break;
            case R.id.menu_tag:
                openTagList();
                break;
            case R.id.menu_search:
                showSearchDialog();
                break;
            case R.id.menu_preference:
                Intent intent = new Intent(getSherlockActivity(), TagNotePreferenceActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_about:
                showAboutDialog();
                break;
            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }
        return ret;
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
     * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad AdapterContextMenuInfo", e);
            return;
        }

        Cursor c = (Cursor) mNoteListView.getAdapter().getItem(info.position);
        if (c == null) {
            return;
        }

        android.view.MenuInflater inflater = getSherlockActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_note_list, menu);

        menu.setHeaderTitle(c.getString(NotesQuery.TITLE));

        Intent intent = new Intent(null, ContentUris.withAppendedId(Notes.CONTENT_URI,
                (int) info.id));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(getSherlockActivity(), NoteListFragment.class), null, intent, 0, null);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem
     * )
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad AdapterContextMenuInfo", e);
            return false;
        }

        mCurrentNote = ContentUris.withAppendedId(Notes.CONTENT_URI, info.id);
        switch (item.getItemId()) {
            case R.id.context_open:
                openNote(mCurrentNote);
                return true;
            case R.id.context_delete:
                // 削除確認ダイアログを表示
                AlertDialog dlg = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_title_delete)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.alert_message_delete_note)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().getContentResolver().delete(mCurrentNote, null, null);
                            }
                        })
                        .setNegativeButton("NO", null)
                        .setInverseBackgroundForced(true)
                        .create();
                dlg.show();
                return true;
            case R.id.context_tag:
                Bundle bundle = new Bundle();
                bundle.putString(Config.SELECTED_NOTE_URI, mCurrentNote.toString());
                FragmentManager manager = getActivity().getSupportFragmentManager();
                TagDialogListFragment fragment = TagDialogListFragment.newInstance();
                fragment.setArguments(bundle);
                fragment.show(manager, Config.TAG_TAGDIALOGLIST_FRAGM);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * 新規ノートの作成。
     */
    private void createNote() {
        Bundle bundle = new Bundle();
        bundle.putInt("action", Config.ACTION_CREATE);
        int tagid = getArguments() != null ? getArguments().getInt(Config.SELECTED_TAGID) : 0;
        if (tagid != 0) {
            bundle.putInt(Config.SELECTED_TAGID, tagid);
        }
        FragmentManager manager = getSherlockActivity().getSupportFragmentManager();
        FragmentUtils.replaceFragment(manager, NoteEditFragment.newInstance(),
                bundle, Config.TAG_NOTEEDIT_FRAGM);
    }

    /**
     * リストでタップしたノートを表示。
     *
     * @param uri The uri of selected note.
     */
    private void openNote(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(Config.SELECTED_NOTEID, Notes.getId(uri));
        bundle.putInt("action", Config.ACTION_OPEN);
        FragmentManager manager = getSherlockActivity().getSupportFragmentManager();
        FragmentUtils.replaceFragment(manager, NoteEditFragment.newInstance(),
                bundle, Config.TAG_NOTEEDIT_FRAGM);
    }

    /**
     * 全ノートタイトルをリスト表示。
     */
    private void allNoteList() {
        FragmentManager manager = getSherlockActivity().getSupportFragmentManager();
        FragmentUtils.replaceFragment(manager, NoteListFragment.newInstance(),
                null, Config.TAG_NOTELIST_FRAGM);
    }

    /**
     * 全タグ名をリスト表示。
     */
    private void openTagList() {
        FragmentManager manager = getSherlockActivity().getSupportFragmentManager();
        FragmentUtils.replaceFragment(manager, TagListFragment.newInstance(),
                null, Config.TAG_TAGLIST_FRAGM);
    }

    /**
     * 検索ダイアログを表示。
     */
    private void showSearchDialog() {
        Bundle bundle = new Bundle();
        if (mSelectedTag != null) {
            bundle.putString(Config.SELECTED_TAGNAME, mSelectedTag);
        }
        getSherlockActivity().startSearch(null, false, bundle, false);
    }

    /**
     * Aboutダイアログを表示。
     */
    private void showAboutDialog() {
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }

        SpannableStringBuilder aboutBody = new SpannableStringBuilder();

        SpannableString mailAddress = new SpannableString(getString(R.string.mailto));
        mailAddress.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(getString(R.string.description_mailto)));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.description_mail_subject));
                startActivity(intent);
            }
        }, 0, mailAddress.length(), 0);

        aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));
        aboutBody.append("\n");
        aboutBody.append(mailAddress);

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        TextView aboutBodyView = (TextView) layoutInflater.inflate(R.layout.fragment_about_dialog,
                null);
        aboutBodyView.setText(aboutBody);
        aboutBodyView.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog dlg = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.alert_title_about)
                .setView(aboutBodyView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dlg.show();
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
                    String selection = null;
                    List<String> argList = new ArrayList<String>();

                    // 検索ワードを指定した場合
                    if (mSearchWord != null) {
                        mSearchWord = mSearchWord.trim();
                        selection = TagNoteContract.Notes.SEARCH_SELECTION;
                        argList.add(new String("%" + mSearchWord + "%"));
                        argList.add(new String("%" + mSearchWord + "%"));
                    }
                    // タグリストから遷移してきた場合(タグ名をwhere条件に含める)
                    if (mSelectedTag != null) {
                        String tagSelection = " WHERE " + TagNoteContract.Notes.TAG_SELECTION;
                        argList.add(0, new String(mSelectedTag));
                        if (selection != null) {
                            selection = tagSelection + " AND " + "( " + selection + ") ";
                        } else {
                            selection = tagSelection;
                        }
                    }
                    String[] selectionArgs = (String[]) argList.toArray(new String[0]);
                    return new CursorLoader(getActivity(), Notes.CONTENT_URI,
                            NotesQuery.PROJECTION,
                            selection, selectionArgs, mOrder);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    mNoteListAdapter.swapCursor(data);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    mNoteListAdapter.swapCursor(null);
                }
            };

    /**
     * The Interface NotesQuery.
     */
    private interface NotesQuery {

        int LOADER_ID = 0;

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
