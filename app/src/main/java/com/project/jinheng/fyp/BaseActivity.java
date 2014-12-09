package com.project.jinheng.fyp;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.Session;
import com.melnykov.fab.FloatingActionButton;
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
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ListView drawerList;
    protected Menu menu;
    private android.support.v7.widget.SearchView searchView;
    private MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setting navigation drawer
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
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

        getMenuInflater().inflate(R.menu.my, menu);

        searchItem = menu.findItem(R.id.search);
        searchView = (android.support.v7.widget.SearchView) searchItem.getActionView();
        setupSearchView(searchItem);
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
            List<SearchableInfo> searchables = searchManager.getSearchablesInGlobalSearch();

            SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
            for (SearchableInfo inf : searchables) {
                if (inf.getSuggestAuthority() != null
                        && inf.getSuggestAuthority().startsWith("applications")) {
                    info = inf;
                }
            }
            searchView.setSearchableInfo(info);
        }

        searchView.setOnQueryTextListener(this);

        //close searchview when back pressed
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    searchItem.collapseActionView();
                    searchView.setQuery("",false);
                }
            }
        });
    }

    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    protected boolean isAlwaysExpanded() {
        return false;
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

        List<Item> items = new ArrayList<Item>();
        //hardcoded name TODO
        items.add(new Header(R.drawable.ic_user_dp, "JinHeng Chong", "chongjinheng@gmail.com"));
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
                        //go settings
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

