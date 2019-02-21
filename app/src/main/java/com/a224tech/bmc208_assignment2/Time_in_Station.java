package com.a224tech.bmc208_assignment2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class Time_in_Station extends AppCompatActivity {

    TextView tv;
    private String station_name, route_name, time_id, newTime, train_no;
    private String TAG = Time_in_Station.class.getSimpleName();
    private SwipeMenuListView lv;
    private ListAdapter adapter;
    private ArrayList<HashMap<String, String>> contactList;
    private View update_builder_view;
    private TimePicker newTimePT;
    Spinner spinnerTrain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_in__station);

        contactList = new ArrayList<>();

        lv = (SwipeMenuListView) findViewById(R.id.time_list);
        //newTimeET = (EditText) findViewById(R.id.timeForUpd);

        station_name = DisplayStationByRoute.selectedStation;//getIntent().getStringExtra("SelectedStation");
        route_name = DisplayStationByRoute.route_name;//getIntent().getStringExtra("SelectedRoute");

        tv = (TextView) findViewById(R.id.station_name);
        tv.setText(station_name + ": Route " + route_name);
        new fetchTimes().execute();
    }

    private class fetchTimes extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdLoading = new ProgressDialog(Time_in_Station.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading Data...");
            pdLoading.setCancelable(true);
            pdLoading.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                URL url = new URL("http://224tech.com/KTM_ASSIGN/get_all_times.php"); // here is your URL path
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("station", station_name);
                postDataParams.put("route", route_name);
                // postDataParams.put("to", station_to);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                //conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                //writer.write(getPostDataString(postDataParams));

                writer.write("station=" + station_name + "&route=" + route_name);
                //writer.write("");
                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    // return sb.toString();
                    String answer = sb.toString();

                    Log.e(TAG, "Response from url: " + answer);

                    if (answer != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(answer);

                            // Getting JSON Array node
                            JSONArray contacts = jsonObj.getJSONArray("manageRecord");

                            for (int i = 0; i < contacts.length(); i++) {
                                JSONObject c = contacts.getJSONObject(i);
                                if(!c.getString("arrivingTime").isEmpty()){
                                    String time = c.getString("arrivingTime");
                                    String id = c.getString("timeID");
                                    String train_id = c.getString("trainID");
                                    // tmp hash map for single contact
                                    HashMap<String, String> record = new HashMap<>();

                                    // adding each child node to HashMap key => value
                                    record.put("arrivingTime", time);
                                    record.put("timeID", id);
                                    record.put("trainID", train_id);

                                    // adding contact to contact list
                                    contactList.add(record);
                                }
                                //String arriving_time = c.getString("arrivingTime");
                                //String role = c.getString("role");


                            }
                        } catch (final JSONException e) {
                            Log.e(TAG, "Json parsing error: " + e.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Json parsing error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                        }

                    } else {
                        Log.e(TAG, "Couldn't get json from server.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Couldn't get json from server. Check LogCat for possible errors!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                else {
                    // return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                //return new String("Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdLoading.dismiss();
            adapter = new SimpleAdapter(Time_in_Station.this, contactList,
                    R.layout.time_list, new String[]{"arrivingTime", "trainID"},
                    new int[]{R.id.time, R.id.train});
            lv.setAdapter(adapter);



            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            0xCE)));
                    // set item width
                    openItem.setWidth(400);
                    // set item title
                    openItem.setIcon(R.drawable.edit);
                    // set item title fontsize
                    openItem.setTitleSize(2);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);

                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            0xC9)));
                    // set item width
                    deleteItem.setWidth(400);
                    // set a icon
                    deleteItem.setIcon(R.drawable.update);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };

            lv.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);

            // Close Interpolator
            lv.setCloseInterpolator(new BounceInterpolator());

            // set creator
            lv.setMenuCreator(creator);

            lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    HashMap item = contactList.get(position);
                    String time_value = item.get("arrivingTime").toString();
                    time_id = item.get("timeID").toString();
                    switch (index) {
                        case 0:

                            AlertDialog.Builder update_builder = new AlertDialog.Builder(Time_in_Station.this);
                            update_builder_view = getLayoutInflater().inflate(R.layout.update_dialog,null);
                            update_builder.setView(update_builder_view);
                            update_builder.setMessage("Enter new time?")
                                    .setCancelable(true)
                                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            newTimePT = update_builder_view.findViewById(R.id.timeForUpd);
                                            newTime = newTimePT.getHour() + ":" + newTimePT.getMinute() + ":00";
                                            new UpdateTime().execute();
                                            Intent intent = new Intent(Time_in_Station.this, Time_in_Station.class);
                                            finish();
                                            startActivity(intent);

                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            update_builder.create().show();


                            break;

                        case 1:
                            //Start Dialog
                            AlertDialog.Builder delete_builder = new AlertDialog.Builder(Time_in_Station.this);
                            delete_builder.setMessage("Are you sure you want to delete this Time?")
                                    .setCancelable(false)
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            new DeleteTime().execute();
                                            Intent intent = new Intent(Time_in_Station.this, Time_in_Station.class);
                                            finish();
                                            startActivity(intent);
                                            //new GetTimes().execute();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = delete_builder.create();
                            alert.show();
                            //Toast.makeText(Times_of_station.this, time_id, Toast.LENGTH_LONG).show();
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });}

    }

    public class DeleteTime extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://224tech.com/KTM_ASSIGN/deleteTime.php"); // here is your URL path


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));


                writer.write("time_id=" + time_id);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

        }

    }

    public class UpdateTime extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://224tech.com/KTM_ASSIGN/updateTime.php"); // here is your URL path


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));


                writer.write("time_id=" + time_id + "&new_time=" + newTime);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {

        }

    }

    public void goToAddTime(View v){
        Intent intent = new Intent(Time_in_Station.this, AddTimes.class);
        startActivity(intent);
    }
}
