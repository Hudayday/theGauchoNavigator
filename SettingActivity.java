package com.huday.thegauchonavigator;

import static java.lang.String.valueOf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener{

    final private String ucsbUrl = "https://api.ucsb.edu/academics/curriculums/v1/classes/search?";
    private String quarter;
    private String enrollcode;
    //private courseFind cf;
    private ArrayList<String> classArray;
    private ArrayAdapter adapter;
    private ListView classList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //initialize department
        quarter = "20222";
        enrollcode = "-1";

        Button button = findViewById(R.id.buttonSearch);
        Button buttonBg = findViewById(R.id.buttonChangeBg);

        classArray = new ArrayList<>();
        classList = findViewById(R.id.cList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classArray);
        classList.setAdapter(adapter);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* try {
                    enrollcode = addCourse.getText().toString();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(SettingActivity.this, "Wrong enroll code format", Toast.LENGTH_SHORT).show();
                }
                if(enrollcode!="")
                Log.e("results",enrollcode);
                if(!enrollcode.equals("-1"))
                    searchCourse();*/
                showNoticeDialog();

            }
        });

        buttonBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,GalleryActivity.class);
                startActivity(intent);
                //startActivity(intent);
            }
        });

        //see class detail

        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                position = position + 1;
                SharedPreferences sharedClass = getSharedPreferences("allCourse" , MODE_PRIVATE);

                builder.setTitle(sharedClass.getString("id"+valueOf(position),"Error"));
                builder.setMessage("Location: "+sharedClass.getString("building"+valueOf(position),"Error")+" "+sharedClass.getString("room"+valueOf(position),"Error")
                        +"\nDays: "+translateDays(sharedClass.getInt("days"+valueOf(position),-1))
                        +"\nTime: "+sharedClass.getString("st"+valueOf(position),"Error")
                        +" - "+sharedClass.getString("et"+valueOf(position),"Error"));

                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        //remove from class list

        classList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = (int) id;
                Toast.makeText(SettingActivity.this, "Remove class "+classArray.get(posID) , Toast.LENGTH_SHORT).show();
                //String removeCardInfo = cardArray.get(posID);
                SharedPreferences sharedOverall = getSharedPreferences("allCourse" , MODE_PRIVATE);
                int oldNum = sharedOverall.getInt("numcourse",0);
                int temp = oldNum;
                oldNum--;
                SharedPreferences.Editor editus = sharedOverall.edit();
                editus.putInt("numcourse", oldNum);
                editus.commit();
                //Update this card

                SharedPreferences sharedClass = getSharedPreferences("allCourse" , MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedClass.edit();
                for(int i = posID+1;i<temp;i++){
                    String s = String.valueOf(i);
                    String k = String.valueOf(i+1);
                    edit.putString("id"+s,sharedClass.getString("id"+k,"error"));
                    edit.putString("building"+s,sharedClass.getString("building"+k,"error"));
                    edit.putString("room"+s,sharedClass.getString("room"+k,"error"));
                    edit.putInt("days"+s,sharedClass.getInt("days"+k,-1));
                    edit.putString("st"+s,sharedClass.getString("st"+k,"error"));
                    edit.putString("et"+s,sharedClass.getString("et"+k,"error"));
                }
                String removeClass = String.valueOf(temp);
                edit.remove("id"+removeClass);
                edit.remove("building"+removeClass);
                edit.remove("room"+removeClass);
                edit.remove("st"+removeClass);
                edit.remove("et"+removeClass);
                edit.remove("days"+removeClass);
                edit.commit();
                classArray.clear();
                onResume();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
        switchPicture();
        adapter.notifyDataSetChanged();
    }

    private void updateView(){
        SharedPreferences sharedCard = getSharedPreferences("allCourse" , MODE_PRIVATE);
        int count = sharedCard.getInt("numcourse",-1);
        String classInfo = "";
        for(int i=1;i<=count;i++){
            boolean present = false;
            String card = "id" + String.valueOf(i);
            classInfo = sharedCard.getString(card,"error");
            for(int j = 0;j<classArray.size();j++){
                if(classInfo.equals(classArray.get(j))){
                    present = true;}}
            if(!present&&(!classInfo.equals("error"))){
                classArray.add(classInfo);}
        }
    }

    public String processResult(String response){
        String processedResponse="";
        SharedPreferences sharedCard = getSharedPreferences("allCourse", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedCard.edit();

        String courseID = "";
        boolean finish = false;
        String temp = response;



        processedResponse = "";
        //located class
        int id = temp.indexOf("\"courseId\"");
        if(id == -1){ //result not found
            processedResponse = "NF";
            Log.e("Show", processedResponse);
            return "NF";
        }

        //int title = temp.indexOf("\",\"title\"");
        courseID = temp.substring(id+12,id+25).replace(" ","");



        int code = temp.indexOf(enrollcode);
        temp = temp.substring(code);

        //boolean checkWhetherSection = false;
        int sec = temp.indexOf("section");
        String checkSection = temp.substring(sec+13,sec+14);
        Log.e("sec",checkSection);
        if(!checkSection.equals("0"))
        {
            courseID += " section";
        }

        while(!finish){
            for(int i = 1; i<=sharedCard.getInt("numcourse",-1);i++){
                if(sharedCard.getString("id"+String.valueOf(i),"").equals(courseID)){
                    return "AD"; //already added
                }
            }

            int num = sharedCard.getInt("numcourse",0);
            num += 1;
            edit.putInt("numcourse",num);

            processedResponse +=  courseID;//course ID
            processedResponse += ";";
            //id + num
            edit.putString("id"+String.valueOf(num),courseID);

            //located key info
            int room = temp.indexOf("\"room\"");
            int building = temp.indexOf("\"building\"");
            int days = temp.indexOf("\"days\"");
            int bTime = temp.indexOf("\"beginTime\"");
            int eTime = temp.indexOf("\"endTime\"");


            processedResponse += temp.substring(building+12,building+17); //Building
            processedResponse += ";";
            //building + num
            edit.putString("building"+String.valueOf(num),temp.substring(building+12,building+17).replace("\"","").replace(",",""));

            processedResponse += temp.substring(room+8,room+12); //Room
            processedResponse += ";";
            //room + num
            edit.putString("room"+String.valueOf(num),temp.substring(room+8,room+12));

            int uday = 0; //store which day are used
            String day = temp.substring(days+8,days+15);
            processedResponse += day;//Days
            processedResponse += ";";
            if(day.indexOf("M")!=-1)
                uday += 1;
            if(day.indexOf("T")!=-1)
                uday += 10;
            if(day.indexOf("W")!=-1)
                uday += 100;
            if(day.indexOf("R")!=-1)
                uday += 1000;
            if(day.indexOf("F")!=-1)
                uday += 10000;
            edit.putInt("days"+String.valueOf(num),uday);

            processedResponse += temp.substring(bTime+13,bTime+18); //Begin
            processedResponse += ";";
            edit.putString("st"+String.valueOf(num),temp.substring(bTime+13,bTime+18));

            processedResponse += temp.substring(eTime+11,eTime+16); //End
            edit.putString("et"+String.valueOf(num),temp.substring(eTime+11,eTime+16));

            Log.e("Show", processedResponse);
            //delegate.processFinish(processedResponse);

            //edit.putString(enrollcode, processedResponse);
            edit.commit();
            finish = true;


        }
        return courseID;
    }

    //Store day in an integer 00000 - 11111
    String translateDays(int i){
        String result = "";
        if(i%10>=1)
            result += "Monday";
        if(i%100>=10)
            result += " Tuesday";
        if(i%1000>=100)
            result += " Wednesday";
        if(i%10000>=1000)
            result += " Thursday";
        if(i/10000==1)
            result += " Friday";
        return result;
    }

    void searchCourse(){
        if(Integer.parseInt(enrollcode)>=0&&Integer.parseInt(enrollcode)<=99999) {

        String input = ucsbUrl + "quarter=" + quarter + "&enrollCode=" + enrollcode + "&objLevelCode=U&pageNumber=1&pageSize=10&includeClassSections=true";
        Log.e("urltest", input);
        courseFind cf = new courseFind(getApplicationContext(), input, enrollcode);
        cf.execute();
        String rs ="";
        try {
            rs = cf.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        rs = processResult(rs);
        Log.e("urltest", rs);
        if(rs.equals("NF"))
            Toast.makeText(SettingActivity.this, "Not found", Toast.LENGTH_SHORT).show();
        else if(rs.equals("AD"))
            Toast.makeText(SettingActivity.this, "already added", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(SettingActivity.this, rs + " added!", Toast.LENGTH_SHORT).show();
            onResume();
        }

    }
    else{
        Toast.makeText(SettingActivity.this, "Wrong enroll code format", Toast.LENGTH_SHORT).show();
    }
    }

    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new NoticeDialogFragment();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, boolean isEnrollCode, String input) {
        // User touched the dialog's positive button
        if(isEnrollCode){
            enrollcode = input;
            searchCourse();
        }
        else{ //personal activity
            SharedPreferences sharedCard = getSharedPreferences("allCourse", MODE_PRIVATE);
            SharedPreferences.Editor edit = sharedCard.edit();

            int num = sharedCard.getInt("numcourse",0);
            num += 1;
            edit.putInt("numcourse",num);

            String temp = input;
            Log.e("tempcheck",temp);
            edit.putString("id"+String.valueOf(num),temp.substring(0,temp.indexOf(";")));
            temp = temp.substring(temp.indexOf(";")+1);
            Log.e("tempcheck",temp);
            edit.putString("building"+String.valueOf(num),temp.substring(0,temp.indexOf(";")));
            temp = temp.substring(temp.indexOf(";")+1);
            Log.e("tempcheck",temp);
            edit.putString("room"+String.valueOf(num),temp.substring(0,temp.indexOf(";")));
            temp = temp.substring(temp.indexOf(";")+1);
            Log.e("tempcheck",temp);
            edit.putInt("days"+String.valueOf(num),Integer.valueOf(temp.substring(0,temp.indexOf(";"))));
            temp = temp.substring(temp.indexOf(";")+1);
            Log.e("tempcheck",temp);
            edit.putString("st"+String.valueOf(num),temp.substring(0,temp.indexOf(";")));
            temp = temp.substring(temp.indexOf(";")+1);
            edit.putString("et"+String.valueOf(num),temp);
            //temp = temp.substring(temp.indexOf(";"));
            edit.commit();
            onResume();

        }
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        dialog.dismiss();
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