package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.adapters.DrawerListAdapter;
import com.project.jinheng.fyp.classes.adapters.Header;
import com.project.jinheng.fyp.classes.adapters.Item;
import com.project.jinheng.fyp.classes.adapters.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * every time an activity is created, child activity will extend this activity to build drawer and toolbar
 */
public abstract class BaseActivity extends ActionBarActivity implements android.support.v7.widget.SearchView.OnQueryTextListener {

    //declared global as need to use in multiple methods
    public static boolean drawerOpen = false;
    public static boolean needSearch = false;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ListView drawerList;
    protected Menu menu;
    private android.support.v7.widget.SearchView searchView;
    private MenuItem searchItem;
    private AlertDialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.setting_toolbar);

        //setting navigation drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerOpen = true;
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerOpen = false;
                invalidateOptionsMenu();
                syncState();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //setting toolbar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        actionBarDrawerToggle.syncState();

        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        }
        initializeDrawerItem();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        if (needSearch) {
            getMenuInflater().inflate(R.menu.my, menu);
            searchItem = menu.findItem(R.id.search);
            searchView = (android.support.v7.widget.SearchView) searchItem.getActionView();
            setupSearchView(searchItem);
        }
        return true;
    }

    protected void setupSearchView(final MenuItem searchItem) {
        if (isAlwaysExpanded()) {
            searchView.setIconifiedByDefault(false);
        } else {
            searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            searchView.setSearchableInfo(info);
        }
        searchView.setOnQueryTextListener(this);

        //close search view when back pressed
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return false;
    }

    protected boolean isAlwaysExpanded() {
        return false;
    }

    private void showErrorDialog() {
        Log.i("called", "showErrorDialog");
        //initialize error dialog
        if (errorDialog == null) {
            errorDialog = new AlertDialog.Builder(this).create();
            errorDialog.setTitle("Error");
            errorDialog.setMessage("Something went wrong, please try again later");
            errorDialog.setInverseBackgroundForced(true);
            errorDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

        }

        if (!errorDialog.isShowing()) {
            errorDialog.show();
        } else {
            errorDialog.dismiss();
        }
    }

    public void logoutButtonClicked(Context context) {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (!session.isClosed()) {
                session.closeAndClearTokenInformation();
            }
        } else {
            session = new Session(context);
            Session.setActiveSession(session);
            session.closeAndClearTokenInformation();
        }

        SharedPreferences setting = getSharedPreferences(SplashScreen.PREFS_NAME, Context.MODE_PRIVATE);
        setting.edit().remove("LoggedIn").clear().apply();
        finish();

        Intent intent = new Intent(this, SplashScreen.class);
        startActivity(intent);
    }

    public void initializeDrawerItem() {

        List<Item> items = new ArrayList<>();
        //hardcoded name TODO
        SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
        boolean loggedInWithFacebook = settings.getBoolean("facebookLog", false);
        if (loggedInWithFacebook) {
            String facebookUID = settings.getString("facebookUID", null);
            String facebookUserName = settings.getString("name", null);
            String facebookEmail = settings.getString("email", null);
            if (facebookUID != null || facebookUserName != null || facebookEmail != null) {
                String profilePictureLink = "https://graph.facebook.com/" + facebookUID + "/picture?height=100&width=100";
                items.add(new Header(profilePictureLink, facebookUserName, facebookEmail));
            } else {
                showErrorDialog();

            }
        } else {
            String userName = settings.getString("name", null);
            String userEmail = settings.getString("email", null);
            if (userName != null || userEmail != null) {

                items.add(new Header(R.drawable.ic_user_dp, userName, userEmail));
            }
        }

        items.add(new ListItem(R.drawable.ic_settings, "Settings"));
        items.add(new ListItem(R.drawable.ic_logout, "Logout"));

        DrawerListAdapter adapter = new DrawerListAdapter(this, items);
        drawerList = (ListView) findViewById(R.id.navigation_drawer);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        Intent intent = new Intent(BaseActivity.this, SettingMain.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.left_to_right_in, R.anim.fade_out);
                        break;
                    case 2:
                        logoutButtonClicked(getApplicationContext());
                        break;
                }

            }
        });
        drawerList.setAdapter(adapter);

    }

}

