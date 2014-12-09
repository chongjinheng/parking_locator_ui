package com.project.jinheng.fyp.classes.adapters;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by JinHeng on 11/24/2014.
 */
public interface Item {
    public int getViewType();
    public View getView (LayoutInflater inflater, View converView);
}
