package com.project.jinheng.fyp.classes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by JinHeng on 11/24/2014.
 */
public class DrawerListAdapter extends ArrayAdapter<Item> {

    private LayoutInflater inflater;

    public enum RowType {
        LIST_ITEM, HEADER_ITEM
    }

    public DrawerListAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return RowType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(inflater, convertView);
    }

}
