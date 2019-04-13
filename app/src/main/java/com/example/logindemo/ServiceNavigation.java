package com.example.logindemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

public class ServiceNavigation extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SessionUtil session = new SessionUtil(getApplicationContext());

        View hView =  navigationView.getHeaderView(0);
        TextView usernameView = (TextView) hView.findViewById(R.id.username_tv);
        usernameView.setText("Hello, " + session.getUsername());

        displayHome();
    }

    public void displayHome(){
        getSupportFragmentManager().beginTransaction().replace(R.id.containerID, RequestFragment_Service.newInstance()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
                super.onBackPressed();
        }
    }
 @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*

            //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
           */
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.history) {
            Toast.makeText(ServiceNavigation.this, "HISTORY", Toast.LENGTH_SHORT).show();
            ServiceNavigation.this.getSupportFragmentManager().beginTransaction().replace(R.id.containerID, HistoryFragment_Service.newInstance()).commit();

        } else if (id == R.id.request) {
            ServiceNavigation.this.getSupportFragmentManager().beginTransaction().replace(R.id.containerID, RequestFragment_Service.newInstance()).commit();

        } else if (id == R.id.logout) {

            SessionUtil session = new SessionUtil(getApplicationContext());
            session.logoutUser();
            Intent i = new Intent(ServiceNavigation.this, MainActivity.class);
            startActivity(i);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
