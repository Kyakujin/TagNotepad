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

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.kyakujin.android.tagnotepad.R;

/**
 * 設定画面のアクティビティ。
 */
public class TagNotePreferenceActivity extends SherlockPreferenceActivity {

    // フォントサイズの設定用
    private ListPreference mListPrefFont;

    // 文字数カウンタ表示の設定用
    private ListPreference mListPrefCounter;

    // ノートリストソート順の設定用
    private ListPreference mListPrefSort;

    private BackupDialogPreference mBackup;
    private BackupDialogPreference mDelete;
    private BackupDialogPreference mRestore;

    static final String PREF_KEY_FONTSIZE = "list_fontsize_key";
    static final String PREF_KEY_COUNTER = "list_charcounter_key";
    static final String PREF_KEY_SORT = "list_sort_key";
    static final String PREF_KEY_BACKUP = "dialog_backup_key";
    static final String PREF_KEY_DELETE_BACKUP = "dialog_delete_backup_key";
    static final String PREF_KEY_RESTORE = "dialog_restore_key";


    /* (非 Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        mListPrefFont = (ListPreference) getPreferenceScreen().findPreference(PREF_KEY_FONTSIZE);
        mListPrefCounter = (ListPreference) getPreferenceScreen().findPreference(PREF_KEY_COUNTER);
        mListPrefSort = (ListPreference) getPreferenceScreen().findPreference(PREF_KEY_SORT);
        mBackup = (BackupDialogPreference) getPreferenceScreen().findPreference(PREF_KEY_BACKUP);
        mDelete = (BackupDialogPreference) getPreferenceScreen().findPreference(PREF_KEY_DELETE_BACKUP);
        mRestore = (BackupDialogPreference) getPreferenceScreen().findPreference(PREF_KEY_RESTORE);

        // Get a reference to the database
        File dbFile = getBaseContext().getDatabasePath("tagnotepad.db");
        // Get a reference to the directory location for the backup
        File exportDir = new File(Environment.getExternalStorageDirectory(),
                "data/" + getBaseContext().getPackageName() + "/backup");
        File backup = new File(exportDir, dbFile.getName());
        if(!backup.exists()) {
            mDelete.setEnabled(false);
            mRestore.setEnabled(false);
        }

    }

    /* (非 Javadoc)
     * @see android.app.Activity#onResume()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        mListPrefFont.setSummary(mListPrefFont.getEntry());
        mListPrefCounter.setSummary(mListPrefCounter.getEntry());
        mListPrefSort.setSummary(mListPrefSort.getEntry());

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mListener);
    }

    /* (非 Javadoc)
     * @see com.actionbarsherlock.app.SherlockPreferenceActivity#onPause()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mListener);
    }

    /**
     * 設定した内容をサマリへ動的に表示。
     */
    private SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {

                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                        String key) {
                    if (key.equals(PREF_KEY_FONTSIZE)) {
                        //Log.d("debug", "list_fontsize_key selected");
                        mListPrefFont.setSummary(mListPrefFont.getEntry());
                    }
                    if (key.equals(PREF_KEY_COUNTER)) {
                        mListPrefCounter.setSummary(mListPrefCounter.getEntry());
                    }
                    if (key.equals(PREF_KEY_SORT)) {
                        mListPrefSort.setSummary(mListPrefSort.getEntry());

                        Intent intent = new Intent(TagNotePreferenceActivity.this,
                                NoteActivity.class);
                        startActivity(intent);
                    }
                }
            };
}
