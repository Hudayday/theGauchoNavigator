package com.huday.thegauchonavigator;
//Tianrui Hu & Jonathon Sun 2021
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    final int REQUSET = 810;
    final private static int MY_PERMISSION_ACCESS_COURSE_LOCATION = 114;
    final private static int MY_PERMISSION_ACCESS_FINE_LOCATION = 514;
    final private static int MY_PERMISSION_ACCESS_BACKGROUND_LOCATION = 1919;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageView scheduleImage = (ImageView) this.findViewById(R.id.scheduleView);
        final ImageView weatherImage = (ImageView) this.findViewById(R.id.weatherView);
        final ImageView settingImage = (ImageView) this.findViewById(R.id.settingView);

        //Ensure the map premission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_PERMISSION_ACCESS_BACKGROUND_LOCATION);
        }

        scheduleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
                startActivity(intent);
            }
        });

        weatherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        settingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        switchPicture();
    }

    public void switchPicture(){  //use to switch picture
        final ImageView bgView = (ImageView) this.findViewById(R.id.imageView);
        SharedPreferences sharedPreferences = getSharedPreferences("pic_data" , MODE_PRIVATE);
        int currentPic = sharedPreferences.getInt("main_pic",0);
        String[] imageName = {"bg0","bg1","bg2"};
        switch (currentPic) {
            case 1:
                bgView.setImageResource(R.mipmap.bg3);
                break;
            case 2:
                bgView.setImageResource(R.mipmap.bg);
                break;
            case 3:
                bgView.setImageResource(R.mipmap.bg2);
                break;
            default:
                bgView.setImageResource(R.mipmap.bg3);
                break;
        }
    }
}