package com.project.jinheng.fyp.classes.adapters;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.jinheng.fyp.R;

import org.w3c.dom.Text;

/**
 * Created by JinHeng on 11/24/2014.
 */
public class ListItem implements Item {

    private final int img;
    private final String text;

    public ListItem(int img, String text) {
        this.img = img;
        this.text = text;
    }

    @Override
    public int getViewType() {
        return DrawerListAdapter.RowType.LIST_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View converView) {
        View view;
        if (converView == null) {
            view = (View) inflater.inflate(R.layout.fragment_listview, null);
            //initializations
        } else {
            view = converView;
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.list_icon);
        TextView textView = (TextView) view.findViewById(R.id.list_item);
        imageView.setImageResource(img);
        textView.setText(text);

        return view;
    }
}
