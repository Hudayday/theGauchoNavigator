package com.huday.thegauchonavigator;

import android.util.Log;

import java.util.Comparator;
// use to sort the class based on time
public class classSort implements Comparator<CourseItemModel>{
    @Override
    public int compare(CourseItemModel o1, CourseItemModel o2) {
        int startTime1 = Integer.valueOf(o1.courseTime.substring(0,5).replace(":",""));
        int startTime2 = Integer.valueOf(o2.courseTime.substring(0,5).replace(":",""));
        //Log.e("compare",String.valueOf(startTime1)+" "+String.valueOf(startTime2));
        return startTime1 - startTime2;
    }
}

