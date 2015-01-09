package com.project.jinheng.fyp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.project.jinheng.fyp.classes.APIUtils;

/**
 * Created by JinHeng on 1/9/2015.
 */
public class SettingMain extends ActionBarActivity {

    private static int IMAGE_PICK = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);

        //setting toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ListView listView = (ListView) findViewById(R.id.settings_listview);

        String[] strings = getResources().getStringArray(R.array.setting_array);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.settings_item_adapter, strings));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, IMAGE_PICK);
                        break;
                    case 1:
                        Intent intentAbout = new Intent(SettingMain.this, SettingAbout.class);
                        startActivity(intentAbout);
                        break;
                }
            }
        });
//
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_to_left_in, R.anim.fade_out);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_to_left_in, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            SharedPreferences.Editor editor = getSharedPreferences(SplashScreen.PREFS_NAME, 0).edit();
            editor.putString("userImage", APIUtils.toJson(uri)).apply();
        }
    }
}
