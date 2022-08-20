package com.huday.thegauchonavigator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class ScheduleActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private FrameLayout frameLayout;

    private static final int TAB_MON = 0;
    public static final String DAY_KEY = "selectedDay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        tabLayout = findViewById(R.id.tabLayout);
        frameLayout = findViewById(R.id.frameLayout);

        // Starts app with Monday
        tabSelect(TAB_MON);
        Bundle args = new Bundle();
        ScheduleFragment fragment = new ScheduleFragment();
        args.putInt(DAY_KEY, TAB_MON);
        fragment.setArguments(args);
        replaceFrameLayout(fragment);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int daySelected = tab.getPosition();
                tabSelect(daySelected);
                Bundle args = new Bundle();
                ScheduleFragment fragment = new ScheduleFragment();
                args.putInt(DAY_KEY, daySelected);
                fragment.setArguments(args);
                replaceFrameLayout(fragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void tabSelect(int daySelected) {
        TabLayout.Tab tab = tabLayout.getTabAt(daySelected);
        tab.select();
    }

    public void replaceFrameLayout(ScheduleFragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, fragment);
        ft.commit();
    }


}