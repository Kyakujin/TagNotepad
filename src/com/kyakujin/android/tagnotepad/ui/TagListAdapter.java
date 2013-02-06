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

import com.kyakujin.android.tagnotepad.R;
import com.kyakujin.android.tagnotepad.provider.TagNoteContract.Tags;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * タグリスト表示用のカーソルアダプタ。
 */
public class TagListAdapter extends SimpleCursorAdapter {

    private class ViewHolder {
        private ImageView imageView;
        private TextView textView;
    }

    /**
     * Instantiates a new TagListAdapter.
     *
     * @param context the context
     * @param layout the layout
     * @param c the cursor
     * @param from the from
     * @param to the to
     * @param flags the flags
     */
    public TagListAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    /* (非 Javadoc)
     * @see android.support.v4.widget.ResourceCursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_item_tag,null,true);

        ViewHolder holder = new ViewHolder();

        holder.textView = (TextView) rowView.findViewById(R.id.tag_item);
        holder.imageView = (ImageView) rowView.findViewById(R.id.icon_tag_list);

        rowView.setTag(holder);

        return rowView;
    }

    /* (非 Javadoc)
     * @see android.support.v4.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String s = cursor.getString(cursor.getColumnIndex(Tags.TAGNAME));
        holder.textView.setText(s);
        holder.imageView.setImageResource(R.drawable.ic_action_tag);
    }
}
