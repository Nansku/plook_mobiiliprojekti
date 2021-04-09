package co.plook;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

//Parent class for all of the other activities to check if user is logged in on every onCreate()
public class ParentActivity extends AppCompatActivity
{
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;


    protected FirebaseAuth auth;
    protected ViewGroup contentGroup;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_layout);

        contentGroup = findViewById(R.id.contentFrame);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) ;
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
        {
            //send user to login page if not logged in
            //startSplashScreenActivity();
            intent = new Intent(this, MainActivity.class);

            //comment out this line if you want to ignore login check
            //startActivity(intent);
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    public void logOut(MenuItem item)
    {
        auth.signOut();
        intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    public void openFeed(MenuItem item) {
        intent = new Intent(this, FeedActivity.class);
        startActivity(intent);

    }

    public void openProfile(MenuItem item) {
        intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openChannels(MenuItem item) {
    }

    public void openSettings(MenuItem item) {
    }
}
