package com.kyakujin.android.tagnotepad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

/**
 * データベースファイルのバックアップ及びリストア実行のTaskクラス。
 */
public class BackupTask extends AsyncTask<String, Void, Integer> {

    /**
     * バックアップ及びリストア実行後のリスナーインターフェース。
     */
    public interface CompletionListener {

        /**
         * バックアップ完了時。
         */
        void onBackupComplete();

        /**
         * リストア完了時。
         */
        void onRestoreComplete();

        /**
         * バックアップファイル削除完了時。
         */
        void onDeleteComplete();

        /**
         * エラー発生時。
         *
         * @param errorCode エラーコード
         */
        void onError(int errorCode);
    }

    /** The Constant BACKUP_SUCCESS. */
    public static final int BACKUP_SUCCESS = 100;

    /** The Constant DELETE_SUCCESS. */
    public static final int DELETE_SUCCESS = 101;

    /** The Constant RESTORE_SUCCESS. */
    public static final int RESTORE_SUCCESS = 102;

    /** The Constant BACKUP_ERROR. */
    public static final int BACKUP_ERROR = 201;

    /** The Constant DELETE_ERROR. */
    public static final int DELETE_NOFILEERROR = 202;

    /** The Constant DELETE_ERROR. */
    public static final int DELETE_ERROR = 203;

    /** The Constant RESTORE_NOFILEERROR. */
    public static final int RESTORE_NOFILEERROR = 204;

    /** The Constant RESTORE_ERROR. */
    public static final int RESTORE_ERROR = 205;

    /** The Constant RESTORE_ERROR. */
    public static final int OTHER_ERROR = 206;

    /** The Constant COMMAND_BACKUP. */
    public static final String COMMAND_BACKUP = "backupDatabase";

    /** The Constant COMMAND_DELETE_BACKUP. */
    public static final String COMMAND_DELETE = "deleteDatabase";

    /** The Constant COMMAND_RESTORE. */
    public static final String COMMAND_RESTORE = "restoreDatabase";

    private Context mContext;

    private CompletionListener mListener;

    /**
     * Instantiates a new backup task.
     *
     * @param context the context
     */
    public BackupTask(Context context) {
        super();
        mContext = context;
    }

    /**
     * リスナーの登録。
     *
     * @param aListener the new completion listener
     */
    public void setCompletionListener(CompletionListener aListener) {
        mListener = aListener;
    }

    /*
     * (非 Javadoc)
     * @see android.os.AsyncTask#doInBackground(Params[])
     */
    @Override
    protected Integer doInBackground(String... params) {
        // データベース名及び格納場所を設定
        File dbFile = mContext.getDatabasePath("tagnotepad.db");
        File exportDir = new File(Environment.getExternalStorageDirectory(),
                "data/" + mContext.getPackageName() + "/backup");
        File backup = new File(exportDir, dbFile.getName());

        String command = params[0];
        if (command.equals(COMMAND_BACKUP)) {
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            try {
                backup.createNewFile();
                fileCopy(dbFile, backup);
                return BACKUP_SUCCESS;
            } catch (IOException e) {
                return BACKUP_ERROR;
            }

        } else if (command.equals(COMMAND_DELETE)) {
            File packageDir = new File(Environment.getExternalStorageDirectory(),
                    "data/" + mContext.getPackageName());

            if (!packageDir.exists()) {
                return DELETE_NOFILEERROR;
            }

            if (delete(packageDir)) {
                return DELETE_SUCCESS;
            } else {
                return DELETE_ERROR;
            }

        } else if (command.equals(COMMAND_RESTORE)) {
            try {
                if (!backup.exists()) {
                    return RESTORE_NOFILEERROR;
                }
                dbFile.createNewFile();
                fileCopy(backup, dbFile);
                return RESTORE_SUCCESS;
            } catch (IOException e) {
                return RESTORE_ERROR;
            }

        } else {
            return BACKUP_ERROR;
        }
    }

    /*
     * (非 Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(Integer result) {

        switch (result) {
            case BACKUP_SUCCESS:
                if (mListener != null) {
                    mListener.onBackupComplete();
                }
                break;
            case DELETE_SUCCESS:
                if (mListener != null) {
                    mListener.onDeleteComplete();
                }
                break;
            case DELETE_NOFILEERROR:
                if (mListener != null) {
                    mListener.onError(DELETE_NOFILEERROR);
                }
                break;
            case RESTORE_SUCCESS:
                if (mListener != null) {
                    mListener.onRestoreComplete();
                }
                break;
            case RESTORE_NOFILEERROR:
                if (mListener != null) {
                    mListener.onError(RESTORE_NOFILEERROR);
                }
                break;
            default:
                if (mListener != null) {
                    mListener.onError(OTHER_ERROR);
                }
        }
    }

    /**
     * ファイルコピーの実行。
     *
     * @param source コピー元
     * @param dest コピー先
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void fileCopy(File source, File dest) throws IOException {
        FileChannel inChannel = new FileInputStream(source).getChannel();
        FileChannel outChannel = new FileOutputStream(dest).getChannel();

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    private boolean delete(File f) {

        if (f.isFile()) {
            if (f.delete()) {
                return true;
            } else {
                return false;
            }
        }

        // ディレクトリと配下のファイルを再帰的に削除
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }
            if (f.delete()) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }
}
