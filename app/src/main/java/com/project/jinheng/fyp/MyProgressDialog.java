package com.project.jinheng.fyp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by JinHeng on 12/18/2014.
 */
public class MyProgressDialog extends ProgressDialog {

    private AnimationDrawable animation;

    public MyProgressDialog(Context context) {
        super(context);
    }

    public static ProgressDialog initiate(Context context) {
        MyProgressDialog dialog = new MyProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);

        ImageView view = (ImageView) findViewById(R.id.animation);
        view.setBackgroundResource(R.drawable.dialog_animation);
        animation = (AnimationDrawable) view.getBackground();
    }

    @Override
    public void show() {
        super.show();
        animation.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        animation.stop();
    }
}
