package com.huday.thegauchonavigator;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CourseItemAdapter extends ArrayAdapter<CourseItemModel> {
    public CourseItemAdapter(Context context, ArrayList<CourseItemModel> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        CourseItemModel course = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_class, parent, false);
        }
        // Lookup view for data population
        TextView tvCourseName = (TextView) convertView.findViewById(R.id.courseName);
        TextView tvCourseTime = (TextView) convertView.findViewById(R.id.courseTime);
        TextView tvCourseLocation = (TextView) convertView.findViewById(R.id.courseLocation);
        ImageView tvClassIcon = (ImageView) convertView.findViewById(R.id.classIcon);
        // Populate the data into the template view using the data object
        tvCourseName.setText(course.courseName);
        tvCourseTime.setText(course.courseTime);
        tvCourseLocation.setText(course.courseLocation);
        if(course.blockType!=1){
            tvClassIcon.setVisibility(View.INVISIBLE);}
        if(course.blockType==2){
            tvCourseName.setTextColor(Color.rgb(176,196,222));
        }else{
            tvCourseName.setTextColor(Color.rgb(0,0,0));
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
