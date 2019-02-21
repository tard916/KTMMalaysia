package com.a224tech.bmc208_assignment2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;



public class ManageFares extends AppCompatActivity {

    private ViewPager mViewPager;
    private Toolbar mToolbar;

    private TabLayout mTabLayout;

    private String TAG = MainActivity.class.getSimpleName();
    private SwipeMenuListView lv;

    ArrayList<HashMap<String, String>> faresList;
    HashMap<String, String> destination;
    private AlertDialog.Builder alertDialog;
    private EditText tvedit_fares;
    private TextView from,to,fee;

    private View view;
    private String departure,arrival,price,fares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_fares);

        faresList = new ArrayList<>();
        lv = (SwipeMenuListView) findViewById(R.id.listOfFares);
        new GetFares().execute();


    }



    private class GetFares extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdLoading = new ProgressDialog(ManageFares.this);
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
                // here is your URL path
                URL url = new URL("http://www.224tech.com/KTM_ASSIGN/get_all_fares.php");

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

                //writer.write("name=" + username+"&pass="+password);
                writer.write("");
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

                            // looping through All Contacts
                            for (int i = 0; i < contacts.length(); i++) {
                                JSONObject c = contacts.getJSONObject(i);
                                String username = c.getString("from_station");
                                String password = c.getString("to_station");
                                String role = c.getString("fare");

                                // tmp hash map for single contact
                                HashMap<String, String> record = new HashMap<>();

                                // adding each child node to HashMap key => value
                                record.put("from_station", username);
                                record.put("to_station", password);
                                record.put("fare", role);

                                // adding contact to contact list
                                faresList.add(record);
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
                                        "Couldn't get json from server. " +
                                                "Check LogCat for possible errors!",
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
            ListAdapter adapter = new SimpleAdapter(ManageFares.this, faresList,
                    R.layout.list_fares_item, new String[]{ "from_station","to_station","fare"},
                    new int[]{R.id.username2, R.id.password2, R.id.role2});
            lv.setAdapter(adapter);

            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "delete" item
                    SwipeMenuItem addItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    addItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            0xC9)));
                    // set item width
                    addItem.setWidth(400);
                    // set a icon
                    addItem.setIcon(R.drawable.add);
                    // add to menu
                    menu.addMenuItem(addItem);
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
                    openItem.setTitleSize(18);
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
                    deleteItem.setIcon(R.drawable.trash);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };

// set creator
            lv.setMenuCreator(creator);

            lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    destination= faresList.get(position);
                    departure = destination.get("from_station").toString();
                    arrival = destination.get("to_station").toString();
                    price = destination.get("fare").toString();
                    /*Toast.makeText(ManageFares.this,
                            departure,
                            Toast.LENGTH_LONG).show();*/
                    switch (index) {
                        case 0:
                            addDialog();
                            break;
                        case 1:
                            updateDialog();
                            break;
                        case 2:
                            deleteDialog();
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });
            lv.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

            // Close Interpolator
            lv.setCloseInterpolator(new BounceInterpolator());


        }

        private void updateDialog(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageFares.this);
            view = getLayoutInflater().inflate(R.layout.update_fares_dialog,null);

            alertDialog.setView(view);

            // 2. Chain together various setter methods to set the dialog characteristics
            alertDialog.setMessage("please enter your new fare")
                    .setTitle("Update Fare.");
            // Add the buttons
            alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    tvedit_fares = (EditText)view.findViewById(R.id.edit_fares);

                    fares = tvedit_fares.getText().toString();
                    Toast.makeText(ManageFares.this,
                            fares,
                            Toast.LENGTH_LONG).show();
                    new updateFaresAdmin().execute();
                    Intent intent = new Intent(ManageFares.this, ManageFares.class);
                    finish();
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            from = (TextView)view.findViewById(R.id.txtFrom);
            to = (TextView)view.findViewById(R.id.txtTo);
            fee = (TextView)view.findViewById(R.id.txtFee);
            from.setText("Departure: "+departure);
            to.setText("Arrival: "+arrival);
            fee.setText("The fee: "+ price);
            //fares = edit_fares.getText().toString();
            ///Toast.makeText(ManageFares.this,
                   // fares,
                    //Toast.LENGTH_LONG).show();
            // Create the AlertDialog edit_fares
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }

        private void addDialog(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageFares.this);
            view = getLayoutInflater().inflate(R.layout.add_fares_dialog,null);

            alertDialog.setView(view);

            // 2. Chain together various setter methods to set the dialog characteristics
            alertDialog.setMessage("please enter your new fare")
                    .setTitle("Add Fare.");
            // Add the buttons
            alertDialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    tvedit_fares = (EditText)view.findViewById(R.id.add_fares);

                    fares = tvedit_fares.getText().toString();
                    Toast.makeText(ManageFares.this,
                            fares,
                            Toast.LENGTH_LONG).show();
                    new addFaresAdmin().execute();
                    Intent intent = new Intent(ManageFares.this, ManageFares.class);
                    finish();
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            from = (TextView)view.findViewById(R.id.txtFrom);
            to = (TextView)view.findViewById(R.id.txtTo);
            fee = (TextView)view.findViewById(R.id.txtFee);
            from.setText("Departure: "+departure);
            to.setText("Arrival: "+arrival);
            fee.setText("The fee: "+ price);
            //fares = edit_fares.getText().toString();
            //Toast.makeText(ManageFares.this,
            // fares,
            //Toast.LENGTH_LONG).show();
            // Create the AlertDialog edit_fares
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }

        private void deleteDialog(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageFares.this);
            view = getLayoutInflater().inflate(R.layout.delete_fares_dialog,null);

            alertDialog.setView(view);

            // 2. Chain together various setter methods to set the dialog characteristics
            alertDialog.setMessage("Warning the deleted fare will lost.")
                    .setTitle("Delete Fare.");
            // Add the buttons
            alertDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    //tvedit_fares = (EditText)view.findViewById(R.id.add_fares);

                    //fares = tvedit_fares.getText().toString();
                    //Toast.makeText(ManageFares.this,
                            //fares,
                            //Toast.LENGTH_LONG).show();
                    new deleteFaresAdmin().execute();
                    Intent intent = new Intent(ManageFares.this, ManageFares.class);
                    finish();
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            from = (TextView)view.findViewById(R.id.txtFrom);
            to = (TextView)view.findViewById(R.id.txtTo);
            fee = (TextView)view.findViewById(R.id.txtFee);
            from.setText("Departure: "+departure);
            to.setText("Arrival: "+arrival);
            fee.setText("The fee: "+ price);
            //fares = edit_fares.getText().toString();
            ///Toast.makeText(ManageFares.this,
            // fares,
            //Toast.LENGTH_LONG).show();
            // Create the AlertDialog edit_fares
            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }
    }

    public class updateFaresAdmin extends AsyncTask<String, Void, String> {



        protected String doInBackground(String... arg0) {

            try {
                //URL url = new URL("http://192.168.43.146/testing/login.php"); // here is your URL path
                //URL url = new URL("http://10.1.15.67/testing/login.php"); // here is your URL path
                //URL url = new URL("http://192.168.1.140/testing/login.php"); // here is your URL path phone
                URL url = new URL("http://www.224tech.com/KTM_ASSIGN/updateFare.php"); // here is your URL path
                /*JSONObject postDataParams = new JSONObject();
                postDataParams.put("from", departure);
                postDataParams.put("to", arrival);
                postDataParams.put("updatedFare", fares);
                Log.e("params",postDataParams.toString());*/

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                //conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os,
                        "UTF-8"));
                //writer.write(getPostDataString(postDataParams));

                writer.write("from="+departure+"&to="+arrival+"&updatedFare="+fares);
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
                    return sb.toString();



                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }



        }

        @Override
        protected void onPostExecute(String result) {
            String check = "true" ;
            if (Objects.equals(result, check)){

                Toast.makeText(getApplicationContext(), "Data Updated!",
                        Toast.LENGTH_SHORT).show();
            }else{

                Toast.makeText(getApplicationContext(), "Failed to Update",
                        Toast.LENGTH_LONG).show();
            }


        }
    }

    public class addFaresAdmin extends AsyncTask<String, Void, String> {



        protected String doInBackground(String... arg0) {

            try {
                //URL url = new URL("http://192.168.43.146/testing/login.php"); // here is your URL path
                //URL url = new URL("http://10.1.15.67/testing/login.php"); // here is your URL path
                //URL url = new URL("http://192.168.1.140/testing/login.php"); // here is your URL path phone
                URL url = new URL("http://www.224tech.com/KTM_ASSIGN/addFares.php"); // here is your URL path
                /*JSONObject postDataParams = new JSONObject();
                postDataParams.put("from", departure);
                postDataParams.put("to", arrival);
                postDataParams.put("updatedFare", fares);
                Log.e("params",postDataParams.toString());*/

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                //conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os,
                        "UTF-8"));
                //writer.write(getPostDataString(postDataParams));

                writer.write("from="+departure+"&to="+arrival+"&updatedFare="+fares);
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
                    return sb.toString();



                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }



        }

        @Override
        protected void onPostExecute(String result) {
            String check = "true" ;
            if (Objects.equals(result, check)){

                Toast.makeText(getApplicationContext(), "Data Updated!",
                        Toast.LENGTH_SHORT).show();
            }else{

                Toast.makeText(getApplicationContext(), "Failed to Update",
                        Toast.LENGTH_LONG).show();
            }


        }
    }

    public class deleteFaresAdmin extends AsyncTask<String, Void, String> {



        protected String doInBackground(String... arg0) {

            try {
                //URL url = new URL("http://192.168.43.146/testing/login.php"); // here is your URL path
                //URL url = new URL("http://10.1.15.67/testing/login.php"); // here is your URL path
                //URL url = new URL("http://192.168.1.140/testing/login.php"); // here is your URL path phone
                URL url = new URL("http://www.224tech.com/KTM_ASSIGN/deleteFare.php"); // here is your URL path
                /*JSONObject postDataParams = new JSONObject();
                postDataParams.put("from", departure);
                postDataParams.put("to", arrival);
                postDataParams.put("updatedFare", fares);
                Log.e("params",postDataParams.toString());*/

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                //conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os,
                        "UTF-8"));
                //writer.write(getPostDataString(postDataParams));

                writer.write("from="+departure+"&to="+arrival);
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
                    return sb.toString();



                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }



        }

        @Override
        protected void onPostExecute(String result) {
            String check = "true" ;
            if (Objects.equals(result, check)){

                Toast.makeText(getApplicationContext(), "Data Deleted!",
                        Toast.LENGTH_SHORT).show();
            }else{

                Toast.makeText(getApplicationContext(), "Failed to Deleted",
                        Toast.LENGTH_LONG).show();
            }


        }
    }
}
