package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

                final EditText input = new EditText(SettingMain.this);
                SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                boolean facebookLog = settings.getBoolean("facebookLog", false);
                switch (position) {
                    case 0:
                        if (facebookLog) {
                            Toast.makeText(SettingMain.this, "Facebook users' display picture will follow Facebook profile picture.", Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder changeName = new AlertDialog.Builder(SettingMain.this);
                            changeName.setTitle("Change user name");
                            changeName.setView(input);
                            changeName.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!input.getText().toString().equals("")) {
                                        SharedPreferences.Editor editor = getSharedPreferences(SplashScreen.PREFS_NAME, 0).edit();
                                        editor.putString("name", input.getText().toString().trim()).apply();
                                        InputMethodManager imm = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                                        changedNameReturnHome();
                                    }
                                }
                            });
                            changeName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //canceled
                                }
                            });
                            changeName.show();
                        }
                        break;
                    case 1:
                        if (facebookLog) {
                            Toast.makeText(SettingMain.this, "Facebook users are not allowed to change password.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(SettingMain.this, ChangePasswordActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.bottom_to_top_in, R.anim.fade_out);
                        }
                        break;
                    case 2:
                        Intent intentAbout = new Intent(SettingMain.this, SettingAbout.class);
                        startActivity(intentAbout);
                        break;
                }
            }
        });
    }

    private void changedNameReturnHome() {
        AlertDialog.Builder confirmation = new AlertDialog.Builder(SettingMain.this);
        confirmation.setTitle("Success");
        confirmation.setCancelable(false);
        confirmation.setMessage("Name changed successfully\n returning to home page");
        confirmation.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SettingMain.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        confirmation.show();
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
