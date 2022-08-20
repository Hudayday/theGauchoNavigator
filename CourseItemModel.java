package com.huday.thegauchonavigator;

public class CourseItemModel {
    public String courseName;
    public String courseTime;
    public String courseLocation;
    public int blockType; //0 for information, 1 for class, 2 for current time

    public CourseItemModel(String courseName, String courseTime, String courseLocation, int blockType) {
        this.courseName = courseName;
        this.courseTime = courseTime;
        this.courseLocation = courseLocation;
        this.blockType = blockType;
    }
}
