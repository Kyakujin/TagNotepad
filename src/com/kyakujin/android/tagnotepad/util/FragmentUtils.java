package com.kyakujin.android.tagnotepad.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * フラグメント関連のユーティリティクラス。
 */
public class FragmentUtils {

    /**
     * フラグメントを置き換えるユーティリティ。
     *
     * @param manager フラグメントマネージャ
     * @param fragment 遷移先のフラグメントクラス
     * @param bundle 遷移先のフラグメントへ渡すデータセット用
     */
    public static void replaceFragment(FragmentManager manager, Fragment fragment, Bundle bundle, String Tag) {
        FragmentTransaction transaction = manager.beginTransaction();

        // 遷移先のフラグメントに渡す値をセット
        if (bundle != null) {
            fragment.setArguments(bundle);
        }

        transaction.replace(android.R.id.content, fragment, Tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
