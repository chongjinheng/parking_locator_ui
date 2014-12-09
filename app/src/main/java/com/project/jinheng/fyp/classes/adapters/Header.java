package com.project.jinheng.fyp.classes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.project.jinheng.fyp.R;

/**
 * Created by JinHeng on 11/24/2014.
 */
public class Header implements Item {

    private final String name;
    private final String email;
    private final int displayPicture;

    public Header(int displayPicture, String name, String email) {
        this.name = name;
        this.displayPicture = displayPicture;
        this.email = email;
    }

    @Override
    public int getViewType() {
        return DrawerListAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View converView) {
        View view;
        if (converView == null) {
            view = (View) inflater.inflate(R.layout.header_view, null);
            //initializations
        } else {
            view = converView;
        }

        RoundedImageView imageView = (RoundedImageView) view.findViewById(R.id.user_dp);
        TextView textView = (TextView) view.findViewById(R.id.user_name);
        TextView emailText = (TextView) view.findViewById(R.id.user_email);
        imageView.setImageResource(displayPicture);
        textView.setText(name);
        emailText.setText(email);

        return view;

    }
}
