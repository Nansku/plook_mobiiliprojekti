package co.plook;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

// Parent class for all of the other activities to check if user is logged in on every onCreate()
// also loads in the left navigation drawer
public class ParentActivity extends AppCompatActivity
{
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    protected NavigationView navigationView;

    protected FirebaseAuth auth;
    protected ViewGroup contentGroup;
    protected Activity currentActivity;

    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_layout);

        contentGroup = findViewById(R.id.contentFrame);

        // TOOLBAR & DRAWER
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // LOG IN AUTHORIZATION
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
        {
            // send user to login page if not logged in
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    protected void loadNavUserData()
    {
        TextView userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        ImageView profileImageView = navigationView.getHeaderView(0).findViewById(R.id.nav_header_profilePic);
        Glide.with(this)
                .load(auth.getCurrentUser().getPhotoUrl()).into(profileImageView);

        userNameTextView.setText(auth.getCurrentUser().getDisplayName());
    }

    // MENU OPEN & CLOSE
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // MENU ITEM ACTIVITIES
    public void logOut(MenuItem item)
    {
        drawerLayout.closeDrawer(GravityCompat.START);
        auth.signOut();
        intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    public void openFeed(MenuItem item)
    {
        Log.d("LOG", "currentActivity: " + currentActivity);
        drawerLayout.closeDrawer(GravityCompat.START);
        if (!(currentActivity instanceof FeedActivity))
        {
            intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        }
    }

    public void openProfile(MenuItem item)
    {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (!(currentActivity instanceof ProfileActivity))
        {
            intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    public void openCreatePost(MenuItem item)
    {
        drawerLayout.closeDrawer(GravityCompat.START);
        intent = new Intent(this, ImageUploadActivity.class);
        startActivity(intent);
    }

    public void openChannels(MenuItem item)
    {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (!(currentActivity instanceof ChannelBrowseActivity))
        {
          intent = new Intent(this, ChannelBrowseActivity.class);
          startActivity(intent);
        }
    }

    public void openSettings(MenuItem item)
    {
        // no need to check if current activity is "settingsActivity" because there's no drawer in settings
        drawerLayout.closeDrawer(GravityCompat.START);

        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
