package com.huday.thegauchonavigator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class GalleryActivity extends Activity {
    GridView grid;
    //private String[] imageName = {"Trinity Cats", "Cat", "Rotating Frog", "Example"};
    String[] imageName = {"bg0","bg1","bg2"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ImageAdapter adapter = new ImageAdapter(GalleryActivity.this);    //building grid
        grid = (GridView) findViewById(R.id.grid_view);
    }


    protected void onResume() {
        super.onResume();
        ImageAdapter adapter = new ImageAdapter(GalleryActivity.this);
        grid = (GridView) findViewById(R.id.grid_view);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(GalleryActivity.this, "Chosen: " + imageName[position] , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();

                int backContent = position+1; //send to position result

                SharedPreferences sharedPreferences = getSharedPreferences("pic_data" , MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putInt("main_pic", backContent);
                edit.commit();

                finish();
            }
        });

    }
}