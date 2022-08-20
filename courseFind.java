package com.huday.thegauchonavigator;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class courseFind extends AsyncTask<String, String, String> {

    private HttpURLConnection urlConnection;
    private String requestUrl;
    private String response;
    private String enrollcode;


    private static String processedResponse;

    private WeakReference<Context> callingContext;

    courseFind(Context context, String rurl, String enrollCode) {
        this.callingContext = new WeakReference<>(context);
        this.requestUrl = rurl;
        this.enrollcode = enrollCode;
    }

    @Override
    protected String doInBackground(String... uri) {
        BufferedReader reader = null;

        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            //ucsb api key
            String apikey = callingContext.get().getString(R.string.ucsb_api_key);

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("accept", "application/json");
            urlConnection.setRequestProperty("ucsb-api-version", "1.0");
            urlConnection.setRequestProperty("ucsb-api-key", apikey);


            if (urlConnection.getResponseCode() == 200) {

                InputStream stream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line;
                //Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                while ((line = reader.readLine()) != null) {
                    Log.e("Show", line+"\n");
                    buffer.append(line);
                }

                return buffer.toString();
            } else {
                //Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
                Log.e("Error", "Error response code: " +
                        urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        response = result;
    }

    public String getClassList() {
        return "";
    }

    //Get useful information from result


    static String getResponse(){
        //Log.e("Send", processedResponse);
        return processedResponse;
    }

    static void resetResponse() {
        processedResponse = ""; // Do not modify this!
    }

}
