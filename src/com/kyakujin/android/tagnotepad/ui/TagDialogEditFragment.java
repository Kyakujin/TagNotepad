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
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Tags;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * タグ名の新規作成、編集ダイアログ。
 */
public class TagDialogEditFragment extends DialogFragment {

    private String mText;
    private EditText mNewTagname;

    // リスト選択したタグのURIを格納
    private static Uri mCurrentTag = null;

    /**
     * New instance.
     *
     * @return an instance of {@link TagDialogEditFragment}
     */
    public static TagDialogEditFragment newInstance() {
        return new TagDialogEditFragment();
    }

    /* (非 Javadoc)
     * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_tag_dialog_edit, null, false);

        mNewTagname = (EditText) view.findViewById(R.id.tagname);

        if (getArguments() != null) {
            String tagname = getArguments().getString(Config.SELECTED_TAGNAME);
            mNewTagname.setText(getArguments().getString(Config.SELECTED_TAGNAME));
            mNewTagname.setSelection(tagname.length());
            mCurrentTag = Uri.parse(getArguments().getString(Config.SELECTED_TAG_URI));
        } else {
            mNewTagname.setText("");
            mCurrentTag = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (mCurrentTag != null) {
            // 選択したタグ名を変更
            builder.setTitle(R.string.description_edit_tagname);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mText = mNewTagname.getText().toString();
                    if (mText.length() == 0) {
                        mText = getString(R.string.description_unnamed_tag);
                    }

                    // tagsテーブルの更新
                    ContentValues values = new ContentValues();
                    values.put(Tags.TAGNAME, mText);
                    getActivity().getContentResolver().update(mCurrentTag, values, null, null);
                    mCurrentTag = null;
                }
            });
        } else {
            // タグを新規作成
            builder.setTitle(R.string.description_add_tag);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                                mText = mNewTagname.getText().toString();
                                if (mText.length() == 0) {
                                    mText = getString(R.string.description_unnamed_tag);
                                }

                            // tagsテーブルへ新規データ作成
                            ContentValues values = new ContentValues();
                            values.put(Tags.TAGNAME, mText);
                            getActivity().getContentResolver().insert(Tags.CONTENT_URI, values);
                        }
                    });
        }
        builder.setNegativeButton("Cancel", null);
        builder.setView(view);

        return builder.create();
    }
}
