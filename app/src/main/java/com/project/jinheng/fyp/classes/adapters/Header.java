package com.project.jinheng.fyp.classes.adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.makeramen.RoundedTransformationBuilder;
import com.project.jinheng.fyp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Created by JinHeng on 11/24/2014.
 */
public class Header implements Item {

    private final String name;
    private final String email;
    private final Bitmap displayPicture;
    private final int resource;

    Context context;

    public Header(Bitmap displayPicture, String name, String email) {
        this.name = name;
        this.displayPicture = displayPicture;
        this.email = email;
        this.resource = 0;
    }

    public Header(int resource, String name, String email) {
        this.name = name;
        this.resource = resource;
        this.email = email;
        this.displayPicture = null;
    }

    @Override
    public int getViewType() {
        return DrawerListAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View converView) {
        View view;
        if (converView == null) {
            view = inflater.inflate(R.layout.header_view, null);
            //initializations
        } else {
            view = converView;
        }
        context = view.getContext();

        final RoundedImageView imageView = (RoundedImageView) view.findViewById(R.id.user_dp);
        TextView textView = (TextView) view.findViewById(R.id.user_name);
        TextView emailText = (TextView) view.findViewById(R.id.user_email);

        Transformation transformation = new RoundedTransformationBuilder().scaleType(ImageView.ScaleType.FIT_XY).build();
//        if (displayPicture != null) {
//            Uri uri = Uri.parse(getImagePath(context, displayPicture));
//            Picasso.with(context).load(uri).fit().transform(transformation).into(imageView);
//        } else if (resource != 0) {
            imageView.setBackgroundResource(resource);
//        }
        textView.setText(name);
        emailText.setText(email);

        return view;

    }

//    public String getImagePath(Context inContext, Bitmap image) {
//
//        ContextWrapper cw = new ContextWrapper(inContext);
//        //get path to app_data/image
//        File directory = cw.getDir("images", Context.MODE_PRIVATE);
//        File mypath = new File(directory, "profile.jpg");
//        try {
//            FileOutputStream fos = new FileOutputStream(mypath);
//            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            fos.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.e("hahahahahhababababababba12345676", directory.getAbsolutePath());
//        return directory.getAbsolutePath() + "/profile.jpg";
//    }
}
