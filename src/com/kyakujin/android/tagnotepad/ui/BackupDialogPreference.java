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

import com.kyakujin.android.tagnotepad.BackupTask;
import com.kyakujin.android.tagnotepad.R;

import android.content.Context;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ノートデータのバックアップとリストアを実行するためのプリファレンスクラス。
 */
public class BackupDialogPreference extends DialogPreference implements
        BackupTask.CompletionListener {

    /** The Constant PREF_KEY_BACKUP. */
    static final String PREF_KEY_BACKUP = "dialog_backup_key";
    static final String PREF_KEY_DELETE_BACKUP = "dialog_delete_backup_key";

    /** The m context. */
    private Context mContext;

    /**
     * Instantiates a new backup dialog preference.
     *
     * @param context the context
     * @param attrs the attrs
     */
    public BackupDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    /* (非 Javadoc)
     * @see android.preference.DialogPreference#onCreateDialogView()
     */
    @Override
    protected View onCreateDialogView() {
        this.setDialogIcon(android.R.drawable.ic_dialog_alert);

        LayoutInflater layoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.alert_dialog, null);

        if (getKey().equals(PREF_KEY_BACKUP)) {
            // ダイアログにバックアップ用のタイトルとNOTICEメッセージを設定
            this.setTitle(mContext.getString(R.string.title_backup));
            TextView tv = (TextView) v.findViewById(R.id.notice_message);
            tv.setText(mContext.getString(R.string.alert_message_backup));
        } else if(getKey().equals(PREF_KEY_DELETE_BACKUP)){
            // ダイアログにバックアップデータ削除用のタイトルとNOTICEメッセージを設定
            this.setTitle(mContext.getString(R.string.title_delete_backup));
            TextView tv = (TextView) v.findViewById(R.id.notice_message);
            tv.setText(mContext.getString(R.string.alert_message_delete_backup));
        } else {
            // ダイアログにリストア用のタイトルとNOTICEメッセージを設定
            this.setTitle(mContext.getString(R.string.title_restore));
            TextView tv = (TextView) v.findViewById(R.id.notice_message);
            tv.setText(mContext.getString(R.string.alert_message_restore));
        }

        return v;
    }

    /* (非 Javadoc)
     * @see android.preference.DialogPreference#onDialogClosed(boolean)
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        BackupTask task = new BackupTask(getContext());
        // ダイアログのYESボタンが押下されたらバックアップ(or リストア)を実行
        if (positiveResult) {
            if (getKey().equals(PREF_KEY_BACKUP)) {
                // バックアップの実行
                task.setCompletionListener(BackupDialogPreference.this);
                task.execute(BackupTask.COMMAND_BACKUP);
            } else if(getKey().equals(PREF_KEY_DELETE_BACKUP)){
                // バックアップファイル削除の実行
                task.setCompletionListener(BackupDialogPreference.this);
                task.execute(BackupTask.COMMAND_DELETE);
            } else {
                // リストアの実行
                task.setCompletionListener(BackupDialogPreference.this);
                task.execute(BackupTask.COMMAND_RESTORE);
            }
        }
        super.onDialogClosed(positiveResult);
    }


    /**
     * バックアップ成功時にコールされ、完了のToast通知を行う。
     * @see com.kyakujin.android.tagnotepad.BackupTask.CompletionListener#onBackupComplete()
     */
    @Override
    public void onBackupComplete() {
        Toast.makeText(getContext(), mContext.getString(R.string.description_backup_success),
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext().getApplicationContext(), NoteActivity.class);
        getContext().startActivity(intent);
    }

    /**
     * リストア成功時にコールされ、Toast通知を行う。
     * @see com.kyakujin.android.tagnotepad.BackupTask.CompletionListener#onRestoreComplete()
     */
    @Override
    public void onRestoreComplete() {
        Toast.makeText(getContext(), mContext.getString(R.string.description_restore_success),
                Toast.LENGTH_SHORT).show();
        //getDialog().dismiss();
        Intent intent = new Intent(getContext().getApplicationContext(), NoteActivity.class);
        getContext().startActivity(intent);
    }

    @Override
    public void onDeleteComplete() {
        Toast.makeText(getContext(), mContext.getString(R.string.description_delete_backup_success),
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext().getApplicationContext(), NoteActivity.class);
        getContext().startActivity(intent);
    }

    /**
     * バックアップ(削除)またはリストア失敗時のリスナー。
     * @see com.kyakujin.android.tagnotepad.BackupTask.CompletionListener#onError(int)
     * @param errorCode the error code
     */
    @Override
    public void onError(int errorCode) {
        if (errorCode == BackupTask.RESTORE_NOFILEERROR ||
                errorCode == BackupTask.DELETE_NOFILEERROR) {
            Toast.makeText(getContext(), mContext.getString(R.string.description_backup_notfound),
                    Toast.LENGTH_SHORT).show();
        } else if (true) {
            Toast.makeText(getContext(),
                    mContext.getString(R.string.description_backup_error) + errorCode,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
