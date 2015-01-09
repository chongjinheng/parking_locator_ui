package com.project.jinheng.fyp.classes.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.jinheng.fyp.R;
import com.project.jinheng.fyp.SplashScreen;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.net.URI;

/**
 * Created by JinHeng on 11/24/2014.
 */
public class Header implements Item {

    private final String name;
    private final String email;
    private final String displayPictureLink;
    private final int resource;

    Context context;

    public Header(String displayPictureLink, String name, String email) {
        this.name = name;
        this.displayPictureLink = displayPictureLink;
        this.email = email;
        this.resource = 0;
    }

    public Header(int resource, String name, String email) {
        this.name = name;
        this.resource = resource;
        this.email = email;
        this.displayPictureLink = null;
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

        final ImageView imageView = (ImageView) view.findViewById(R.id.user_dp);
        TextView textView = (TextView) view.findViewById(R.id.user_name);
        TextView emailText = (TextView) view.findViewById(R.id.user_email);

        Transformation transformation = new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                int size = Math.min(source.getWidth(), source.getHeight());
                int x = (source.getWidth() - size) / 2;
                int y = (source.getHeight() - size) / 2;

                Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
                if (squaredBitmap != source) {
                    source.recycle();
                }

                Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
                paint.setShader(shader);
                paint.setAntiAlias(true);

                float r = size / 2f;
                canvas.drawCircle(r, r, r, paint);

                squaredBitmap.recycle();
                return bitmap;
            }

            @Override
            public String key() {
                return "circle";
            }
        };
        SharedPreferences settings = context.getSharedPreferences(SplashScreen.PREFS_NAME, 0);
        String uriString = settings.getString("userImage", null);
        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            Picasso.with(context).load(uri).transform(transformation).fit().into(imageView);
        } else {
            if (displayPictureLink != null) {
                Picasso.with(context).load(displayPictureLink).transform(transformation).fit().into(imageView);
                textView.setText(name);
                emailText.setText(email);
            } else if (resource != 0) {
                imageView.setBackgroundResource(resource);
            }
        }
        textView.setText(name);
        emailText.setText(email);
        return view;
    }
}

