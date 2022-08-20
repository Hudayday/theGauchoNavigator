package com.huday.thegauchonavigator;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.Collections;

public class ScheduleFragment extends Fragment implements LocationListener {
    private static final int TAB_MON = 0;
    private static final int TAB_TUES = 1;
    private static final int TAB_WED = 2;
    private static final int TAB_THUR = 3;
    private static final int TAB_FRI = 4;
    private static final double UCSB_UPPER_LATITUDE = 34.418;
    private static final double UCSB_UPPER_LONGTITUDE = -119.853;
    private static final double UCSB_LOWER_LATITUDE = 34.407;
    private static final double UCSB_LOWER_LONGTITUDE = -119.840;

    private static final double IV_LOWER_LATITUDE = 34.409;
    private static final double IV_LOWER_LONGTITUDE = -119.873;

    private static final int TYPE_INFO = 0;
    private static final int TYPE_CLASS = 1;
    private static final int TYPE_CURRENT = 2;

    private ListView listView;
    private ArrayList<CourseItemModel> courseArrayList;
    private ArrayAdapter<CourseItemModel> adapter;
    //location service
    private LocationManager mLocationManager;
    private int currentDay;

    private double currentLongtitude;
    private double currentLatitude;
    private int weatherCode; //800 for clear， 801 for cloud


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_schedule, container, false);
        Bundle bundle = this.getArguments();
        currentDay = 0;
        weatherCode = 800;
        getWeather();
        if(weatherCode!=800&&weatherCode!=801){
            Toast.makeText(getContext(), "Weather is not clear now. Please depart early", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getContext(), "Hello clear Santa Barbara!", Toast.LENGTH_SHORT).show();
        }
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        currentLatitude = location.getLatitude();
        currentLongtitude = location.getLongitude();

        int daySelected = bundle.getInt(ScheduleActivity.DAY_KEY, TAB_MON);
        listView = root.findViewById(R.id.listView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                courseArrayList = getData(daySelected);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Collections.sort(courseArrayList,new classSort());
        }
        adapter = new CourseItemAdapter(getActivity(), courseArrayList);
        listView.setAdapter(adapter);
        //click to Google Map
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int posID = (int) id;
                //If this is class, go to schedule
                if(courseArrayList.get(posID).blockType==TYPE_CLASS)
                    {CourseItemModel building = courseArrayList.get(posID);
                    displayDirections(building.courseLocation);}
                //If not, see whether it is current time or info. If info, go to alarm
                else if(courseArrayList.get(posID).blockType==TYPE_INFO){
                    CourseItemModel course = courseArrayList.get(posID);
                    createAlarm(course.courseName,Integer.valueOf(course.courseTime.substring(0,2)),Integer.valueOf(course.courseTime.substring(3,5)));
                }

            }
        });

        return root;
    }

    // API stuff
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<CourseItemModel> getData(int daySelected) throws JSONException {
        SharedPreferences sharedClass = getActivity().getApplicationContext().getSharedPreferences("allCourse", MODE_PRIVATE);
        ArrayList<CourseItemModel> data = new ArrayList<>();
        int total = sharedClass.getInt("numcourse", 0);
        currentDay = daySelected;
        for (int k = 1; k <= total; k++) {

            String id = sharedClass.getString("id"+k,"error");
            String building = sharedClass.getString("building"+k,"error");
            String room = sharedClass.getString("room"+k,"error");
            int days = sharedClass.getInt("days"+k,-1);
            String startTime = sharedClass.getString("st"+k,"error");
            String endTime = sharedClass.getString("et"+k,"error");
            int tempUse = 5;

            //depart to class
            int arriveTime = Integer.valueOf(startTime.replace(":",""));
            //Log.e("time",String.valueOf(arriveTime));
            //Ideally arrive 3 min before class
            arriveTime -= 3;

            if(arriveTime%100 > 60)
                arriveTime -= 40;
            /*This preserve for future use
            String currentLocation = "garden+court+apartment+Goleta";
            String desLocation = "UCSB+" + building;

            int departTime = arriveTime - getTravelTime(currentLocation,desLocation,0);
            */

            int departTime = arriveTime - calculateEstimatedTime() ;

            if(departTime%100 > 60)
                departTime -= 40;
            String aTime = String.valueOf(arriveTime);
            String dTime = String.valueOf(departTime);
            Log.e("atime",aTime);
            Log.e("dtime",dTime);
            if(aTime.length()<4)
                aTime = "0" + aTime;
            if(dTime.length()<4)
                dTime = "0" + dTime;

            CourseItemModel cim = new CourseItemModel(id, startTime + " - " + endTime, building +" "+ room, TYPE_CLASS);
            CourseItemModel tocim = new CourseItemModel("Go to " + id,  dTime.substring(0,2) + ":" + dTime.substring(2)+ " - " + aTime.substring(0,2) + ":" + aTime.substring(2), "From your location to "+building,TYPE_INFO);

            switch (daySelected) {
                case TAB_MON:
                    if(days%10>=1) {
                        data.add(cim);
                        data.add(tocim);
                    }
                    break;
                case TAB_TUES:
                    if(days%100>=10) {
                        data.add(cim);
                        data.add(tocim);
                    }
                    break;
                case TAB_WED:
                    if(days%1000>=100) {
                        data.add(cim);
                        data.add(tocim);
                    }
                    break;
                case TAB_THUR:
                    if(days%10000>=1000) {
                        data.add(cim);
                        data.add(tocim);
                    }
                    break;
                case TAB_FRI:
                    if(days/10000==1) {
                        data.add(cim);
                        data.add(tocim);
                    }
                    break;
            }

        }
        //add current time block

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        Log.e("day",String.valueOf(day));
        int hrs = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        String currentTime = String.valueOf(hrs)+":"+String.valueOf(min);
        if(daySelected==(day-2)){
            CourseItemModel current = new CourseItemModel("Current Time", currentTime,"",TYPE_CURRENT);
            data.add(current);
        }

        return data;
    }

    //This method grabs student's class building and redirects to Google Maps for directions
//If student's device does not have Google Maps installed, he or she will be prompted to install it
    public void displayDirections(String Building) {
        try {
            //If Google Maps is installed, initialize this uri
            int cutString = Building.indexOf(" ");
            String building = Building.substring(0,cutString);
            String currentLocation = String.valueOf(currentLatitude) + "," + String.valueOf(currentLongtitude);
            Uri uri = Uri.parse("https://www.google.com/maps/dir/"+currentLocation+"/UCSB " + building);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            //Initialize intent with action view
            intent.setPackage("com.google.android.apps.maps");
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Start activity
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //If Google Maps is not installed, initialize this uri
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            //Initialize intent with action view
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        }
    }

    public int getTravelTime(String currentLocation, String arriveLocation, int method) throws JSONException {
        String mapApi = getString(R.string.google_maps_key);
        String input = "https://maps.googleapis.com/maps/api/directions/json?origin=" + currentLocation + "&destination=" + arriveLocation + "&key=" + mapApi;
        Log.e("urltest", input);
        routeFind rf = new routeFind(getActivity().getApplicationContext(), input, method);
        rf.execute();
        String rs ="";
        try {
            rs = rf.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JSONObject jObject = new JSONObject(rs);

        int duration = jObject.getInt("duration");

        int minuteDuration = duration / 60;
        Log.e("timeDuration",String.valueOf(minuteDuration));
        return minuteDuration;
    }

    public void getWeather() { //get weather information of UCSB
        weatherFind wf = new weatherFind(getActivity().getApplicationContext());
        wf.execute();
        String ws ="";
        try {
            ws = wf.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
        JSONObject jObject = new JSONObject(ws);
        String weatherInfo = jObject.getString("weather");
        weatherInfo = weatherInfo.substring(weatherInfo.indexOf("id")+4,weatherInfo.indexOf(",\"main"));
            Log.e("Weather warning",weatherInfo);
        weatherCode = Integer.valueOf(weatherInfo);
            Log.e("Weather warning",String.valueOf(weatherCode));}
        catch (JSONException e){
            Log.e("Weather warning","fail");
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLatitude = location.getLatitude();
        currentLongtitude = location.getLongitude();

    }

    private int calculateEstimatedTime(){
        double weatherAjust = 1;
        if(weatherCode!=800&&weatherCode!=801) //bad weather ajust
            {
                weatherAjust = 1.2;
            }

        Log.e("Location",String.valueOf(currentLatitude)+" "+String.valueOf(currentLongtitude));
        if(currentLatitude>=UCSB_LOWER_LATITUDE&& currentLongtitude <=UCSB_UPPER_LATITUDE
        && currentLongtitude <=UCSB_LOWER_LONGTITUDE&& currentLongtitude >=UCSB_UPPER_LONGTITUDE) //inside campus
        {
            Log.e("Location","UCSB");
            return (int)(10*weatherAjust);
        }
        if(currentLatitude>=IV_LOWER_LATITUDE&&currentLatitude<=UCSB_UPPER_LATITUDE
        &&currentLongtitude<=UCSB_UPPER_LONGTITUDE&& currentLongtitude >=IV_LOWER_LONGTITUDE)  //inside IV
        {
            Log.e("Location","IV");
            return (int)(25*weatherAjust);
        }
        Log.e("Location","Other");
     return (int)(45*weatherAjust); //other place
    }

    private void createAlarm(String message, int hour, int minutes) {
        //create alarm for class
        String packageName = getActivity().getApplication().getPackageName();
        ArrayList<Integer> alarmDays= new ArrayList<Integer>();
        alarmDays.add((currentDay+2));

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                .putExtra(AlarmClock.EXTRA_DAYS,alarmDays)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message)
                .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                .putExtra(AlarmClock.EXTRA_SKIP_UI, false);

        startActivity(intent);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}