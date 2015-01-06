package com.project.jinheng.fyp;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Created by JinHeng on 10/28/2014.
 */
public class SplashScreen extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        //kill pending service
        stopService(new Intent(SplashScreen.this, MonitorLocationService.class));
        Log.d("SplashScreen", "Pending services killed");

        //Animation of the entire splash screen
        float scale = getResources().getDisplayMetrics().density;
        View splashImage = findViewById(R.id.splash_splashImage);

        final int[] splashImagePos = new int[2];

        splashImage.getLocationOnScreen(splashImagePos);

        int splashY = splashImagePos[1];

        ObjectAnimator oa1 = ObjectAnimator.ofFloat(splashImage, "translationY", splashY * scale, (splashY - 170.0f) * scale);

        AnimatorSet set = new AnimatorSet();
        set.play(oa1).after(1000);
        set.setDuration(700);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();

        Handler taglinehandler = new Handler();
        taglinehandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //show the tagline and title after a delay
                TextView title = (TextView) findViewById(R.id.splash_splashTitle);
                TextView tagline = (TextView) findViewById(R.id.splash_tagline);
                title.setVisibility(View.VISIBLE);
                tagline.setVisibility(View.VISIBLE);

                //set fade animation
                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                title.setAnimation(fadeIn);
                tagline.setAnimation(fadeIn);
            }
        }, 700);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //intent to login or home activity
                //check if user logged in or not
                SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                boolean loggedIn = settings.getBoolean("LoggedIn", false);
                if (loggedIn) {
                    Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(intent);
                }

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 1700);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

}
