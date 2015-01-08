package com.project.jinheng.fyp;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by JinHeng on 1/7/2015.
 */
public class TouchWrapper extends FrameLayout {
    private long lastTouched = 0;
    private static final long SCROLL_TIME = 110L;

    public TouchWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouched = SystemClock.uptimeMillis();
                HomeActivity.mapIsTouched = false;
                break;
            case MotionEvent.ACTION_UP:
                final long now = SystemClock.uptimeMillis();
                if (now - lastTouched > SCROLL_TIME) {
                    // Update the map
                    HomeActivity.mapIsTouched = true;
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }
}
