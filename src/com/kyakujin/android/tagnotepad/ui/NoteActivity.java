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

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kyakujin.android.tagnotepad.Config;
import com.kyakujin.android.tagnotepad.R;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * 最初に起動されるActivity。<br>
 * このActivityの配下に、{@link NoteListFragment}、{@link NoteEditFragment}、
 * {@link TagListFragment}の各フラグメントが紐付けられる。
 */
public class NoteActivity extends SherlockFragmentActivity {

    /*
     * (非 Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String action = intent.getAction();
        // DBG
        //  FragmentManager.enableDebugLogging(true);
        //  Log.d("debug", "ActivityTest: onCreate start"); // DBG

        // 他のアプリからデータが送信されてた場合、NoteEditFragmentにそのデータを渡たす
        if (Intent.ACTION_SEND.equals(action)) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                String extBody = extras.getString(Intent.EXTRA_TEXT);
                String extTitle = extras.getString(Intent.EXTRA_SUBJECT);
                Bundle bundle = new Bundle();
                if (extBody != null) {
                    bundle.putString(Config.SHARED_BODY, extBody);
                }
                if (extTitle != null) {
                    bundle.putString(Config.SHARED_TITLE, extTitle);
                }

                bundle.putInt("action", Config.ACTION_SENDED);
                Fragment fg = NoteEditFragment.newInstance();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                fg.setArguments(bundle);
                transaction.add(android.R.id.content, fg, Config.TAG_NOTEEDIT_FRAGM);
                transaction.commit();
            }
        } else {
            Fragment fg = NoteListFragment.newInstance();
            if (!fg.isAdded()) {
                // NoteListFragment開始する前にバックスタックを破棄する
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                    fm.popBackStack();
                }

                getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fg, Config.TAG_NOTELIST_FRAGM).commit();
            }
        }

        handleIntent(getIntent());
    }

    /*
     * (非 Javadoc)
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /**
     * 検索ボックス表示処理。
     *
     * @param intent the intent
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            String tagname = null;
            final Bundle bundle = intent.getBundleExtra(SearchManager.APP_DATA);
            if (bundle != null) {
                // タグ付きのノート検索の場合は、検索条件にそのタグ名を含める。
                // 例えば"Tag_A"というタグ名が付いたノートの集合から、
                // 任意の検索ワードを含んだノートを検索する場合、
                // "Tag_A"をクエリのwhere条件に含める。
                tagname = bundle.getString(Config.SELECTED_TAGNAME);
            }

            doSearch(query, tagname);
        }
    }

    /**
     * 検索実行処理。
     *
     * @param queryStr クエリ文字列
     * @param tag 検索条件に含めるタグ名
     */
    private void doSearch(String queryStr, String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(Config.SEARCH_WORD, queryStr);
        if (tag != null) {
            bundle.putString(Config.SELECTED_TAGNAME, tag);
        }

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = NoteListFragment.newInstance();

        fragment.setArguments(bundle);
        transaction.replace(android.R.id.content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
}
