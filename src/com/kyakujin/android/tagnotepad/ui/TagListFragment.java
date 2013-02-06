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

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import com.kyakujin.android.tagnotepad.Config;
import com.kyakujin.android.tagnotepad.R;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Mapping;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Tags;
import com.kyakujin.android.tagnotepad.util.FragmentUtils;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * タグリスト表示用のフラグメントクラス。
 */
public class TagListFragment extends SherlockListFragment implements LoaderCallbacks<Cursor> {

    /** The Constant TAG. */
    private static final String TAG = "TagListFragment";

    /**
     * New instance.
     *
     * @return an instance of {@link TagListFragment}
     */
    public static Fragment newInstance() {
        return new TagListFragment();
    }

    private Uri mCurrentTag = null;
    private String mTagName = null;

    private TagListAdapter mTagListAdapter;
    private ListView mTagListView;

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

        View v = inflater.inflate(R.layout.fragment_tag_list, container, false);

        mTagListView = (ListView) v.findViewById(android.R.id.list);

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
        intent.setData(Tags.CONTENT_URI);

        getListView().setOnCreateContextMenuListener(this);

        mTagListAdapter = new TagListAdapter(getActivity(),
                R.layout.list_item_tag, null,
                new String[] {
                        Tags.TAGNAME,
                },
                new int[] {
                        R.id.tag_item,
                },
                0);

        mTagListView.setAdapter(mTagListAdapter);

        LoaderManager manager = getLoaderManager();
        manager.initLoader(TagsQuery.LOADER_ID, null, this);
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

        Cursor c = (Cursor) mTagListView.getItemAtPosition(position);
        if (c == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Config.SELECTED_TAGID, c.getInt(TagsQuery.TAGS_ID));
        bundle.putString(Config.SELECTED_TAGNAME, c.getString(TagsQuery.TAGNAME));
        FragmentManager manager = getSherlockActivity().getSupportFragmentManager();
        FragmentUtils.replaceFragment(manager, NoteListFragment.newInstance(),
                bundle, Config.TAG_NOTELIST_FRAGM);
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
     * Sets the opiotns menu visibility.
     *
     * @param menu the new opiotns menu visibility
     */
    private void setOpiotnsMenuVisibility(Menu menu) {
        menu.findItem(R.id.menu_add_tag).setVisible(true);
        menu.findItem(R.id.menu_allnotes).setVisible(true);
    }

    /*
     * (非 Javadoc)
     * @see
     * com.actionbarsherlock.app.SherlockListFragment#onPrepareOptionsMenu(android
     * .view.Menu)
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

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
            // タグの新規追加
            case R.id.menu_add_tag:
                FragmentManager manager = getActivity().getSupportFragmentManager();
                TagDialogEditFragment fragment = TagDialogEditFragment.newInstance();
                fragment.setArguments(null);
                TagDialogEditFragment.newInstance().show(manager,
                        Config.TAG_TAGDIALOGEDIT_FRAGM);
                break;
            // 全ノートリストの表示
            case R.id.menu_allnotes:
                FragmentManager manager2 = getSherlockActivity().getSupportFragmentManager();
                FragmentUtils.replaceFragment(manager2, NoteListFragment.newInstance(),
                        null, Config.TAG_NOTELIST_FRAGM);
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

        Cursor c = (Cursor) mTagListView.getAdapter().getItem(info.position);
        if (c == null) {
            return;
        }

        android.view.MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_tag_list, menu);

        mTagName = c.getString(TagsQuery.TAGNAME);
        menu.setHeaderTitle(mTagName);

        Intent intent = new Intent(null, ContentUris.withAppendedId(Tags.CONTENT_URI,
                (int) info.id));

        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(getActivity(), TagListFragment.class), null, intent, 0, null);
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

        mCurrentTag = ContentUris.withAppendedId(Tags.CONTENT_URI, info.id);

        switch (item.getItemId()) {
            case R.id.context_delete:
                AlertDialog dlg = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.alert_title_delete)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.alert_message_delete_tag)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().getContentResolver().delete(mCurrentTag, null, null);
                                // mappingテーブルの該当データを削除
                                String where = Mapping.TAGID + " = " + Tags.getId(mCurrentTag);
                                getActivity().getContentResolver().delete(Mapping.CONTENT_URI,
                                        where, null);
                            }
                        })
                        .setNegativeButton("NO", null)
                        .setInverseBackgroundForced(true)
                        .create();
                dlg.show();
                return true;
            case R.id.context_edit:
                Bundle bundle = new Bundle();
                bundle.putString("selectedTagName", mTagName);
                bundle.putString("selectedTagUri", mCurrentTag.toString());

                FragmentManager manager = getActivity().getSupportFragmentManager();
                TagDialogEditFragment fragment = TagDialogEditFragment.newInstance();
                fragment.setArguments(bundle);
                fragment.show(manager, Config.TAG_TAGDIALOGEDIT_FRAGM);

                mCurrentTag = null;
                mTagName = null;
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /*
     * (非 Javadoc)
     * @see android.support.v4.app.Fragment#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(getActivity(), Tags.CONTENT_URI, TagsQuery.PROJECTION,
                null, null, Tags.TAGNAME);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
        mTagListAdapter.swapCursor(data);
    }

    /*
     * (非 Javadoc)
     * @see
     * android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        mTagListAdapter.swapCursor(null);
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
}
