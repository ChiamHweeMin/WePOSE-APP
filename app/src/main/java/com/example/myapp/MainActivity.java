package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.example.myapp.UtilisService.SharedPreferenceClass;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    public static FragmentManager fragmentManager;
    HomeFragment homeFragment = new HomeFragment();
    DescriptionFragment descriptionFragment = new DescriptionFragment();
    SummaryFragment summaryFragment = new SummaryFragment();
    ProfileFragment profileFragment = new ProfileFragment();
    SharedPreferenceClass sharedPreferenceClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        sharedPreferenceClass = new SharedPreferenceClass(this);

        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();
        } else {
            int value = extras.getInt("key");
            if(value == 1) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container,profileFragment).commit();
            }
            if(value == 2) {
                sharedPreferenceClass.clear();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                SharedPreferences wepose_pref = getSharedPreferences("user_wepose", MODE_PRIVATE);
                String userName = wepose_pref.getString("username", "");
                if (item.getItemId() == R.id.navigation_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();
                    Toast.makeText(MainActivity.this, userName, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (item.getItemId() == R.id.navigation_description) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,descriptionFragment).commit();
                    return true;
                }
                if (item.getItemId() == R.id.navigation_summary) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,summaryFragment).commit();
                    return true;
                }
                if (item.getItemId() == R.id.navigation_profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,profileFragment).commit();
                    return true;
                }
                return false;
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_side_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        int id = item.getItemId();

        if(id == R.id.navigation_initial) {
            Intent intent = new Intent(MainActivity.this, InitialActivity.class);
            startActivity(intent);
        }
        if(id == R.id.navigation_setting) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }
        if(id == R.id.navigation_logout) {
            sharedPreferenceClass.clear();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}