package com.a224tech.bmc208_assignment2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AddTimes extends AppCompatActivity {

    private String station_name, route_name, time_id, addedTime, train_no;
    static String selectedTrain;
    private String TAG = Time_in_Station.class.getSimpleName();
    private static final String TAG_TRAIN = "trainID";
    private ListAdapter adapter;
    private ListView lv;
    private ArrayList<HashMap<String, String>> contactList;
    private View add_builder_view;
    private TimePicker newTimePT;
    StringBuffer sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_times);

        station_name = DisplayStationByRoute.selectedStation;//getIntent().getStringExtra("SelectedStation");
        route_name = DisplayStationByRoute.route_name;//getIntent().getStringExtra("SelectedRoute");

        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.train_list);

        new GetTrains().execute();
    }

    private class GetTrains extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdLoading = new ProgressDialog(AddTimes.this);
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
                } else {
                    // return new String("false : "+responseCode);
                }
            } catch (Exception e) {
                //return new String("Exception: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdLoading.dismiss();
            adapter = new SimpleAdapter(AddTimes.this, contactList,
                    R.layout.train_list, new String[]{"trainID"},
                    new int[]{R.id.trainNo});
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

                    HashMap<String,String> map =(HashMap<String,String>)lv.getItemAtPosition(position);
                    selectedTrain = map.get(TAG_TRAIN);
                    Toast.makeText(getApplicationContext(),
                            selectedTrain,
                            Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder add_builder = new AlertDialog.Builder(AddTimes.this);
                    add_builder_view = getLayoutInflater().inflate(R.layout.add_time_dialog,null);
                    add_builder.setView(add_builder_view);
                    add_builder.setMessage("Add")
                            .setCancelable(true)
                            .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    newTimePT = add_builder_view.findViewById(R.id.timeForAdd);
                                    addedTime = newTimePT.getHour() + ":" + newTimePT.getMinute() + ":00";
                                    Toast.makeText(getApplicationContext(),
                                            addedTime,
                                            Toast.LENGTH_SHORT).show();
                                    new AddNewTime().execute();
                                    Intent intent = new Intent(AddTimes.this, Time_in_Station.class);
                                    finish();
                                    startActivity(intent);

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    add_builder.create().show();
                }
            });
        }
    }
    public class AddNewTime extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://224tech.com/KTM_ASSIGN/addTime.php"); // here is your URL path


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));


                writer.write("route=" + route_name + "&station=" + station_name + "&train=" + selectedTrain + "&newTime=" + addedTime);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    sb = new StringBuffer("");
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
            if(sb.toString().equals("true")){
                Toast.makeText(getApplicationContext(),
                        "Added Successfully",
                        Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(),
                        "There is already value for this",
                        Toast.LENGTH_LONG).show();
            }
        }

    }
}
