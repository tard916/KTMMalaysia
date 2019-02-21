package com.a224tech.bmc208_assignment2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchResult extends AppCompatActivity implements TextToSpeech.OnInitListener{

    String From;
    String To;
    String Time;
    String  trainNumber;
    String tick_Price;
    TextView txtYear, txtTo, txtTime, txtFee, txtnumTrain;

    private TextToSpeech tts;
    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;

    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);


        tts = new TextToSpeech(this, this);
         txtYear = (TextView) findViewById(R.id.txtFrom);
         txtTo = (TextView) findViewById(R.id.txtTo);
         txtTime = (TextView) findViewById(R.id.txtTime);
         txtFee = (TextView) findViewById(R.id.txtFee);

         //Animation for the textview time.
         Animation anim = new AlphaAnimation(0.5f, 1.0f);
         anim.setDuration(90);
         anim.setStartOffset(90);
         anim.setRepeatMode(Animation.REVERSE);
         anim.setRepeatCount(Animation.INFINITE);
         anim.setAnimationListener(new Animation.AnimationListener() {
             @SuppressLint("ResourceAsColor")
             @Override
             public void onAnimationStart(Animation animation) {
                 txtTime.setTextColor(Color.RED);
             }

             @SuppressLint("ResourceAsColor")
             @Override
             public void onAnimationEnd(Animation animation) {
                 txtTime.setTextColor(Color.BLACK);
             }

             @SuppressLint("ResourceAsColor")
             @Override
             public void onAnimationRepeat(Animation animation) {
                 txtTime.setTextColor(Color.RED);
             }
         });
        txtTime.startAnimation(anim);
        txtnumTrain = (TextView) findViewById(R.id.txtnumTrain);

        if(getIntent().hasExtra("From")){
            From = getIntent().getStringExtra("From");
            txtYear.setText(From);
        }
        if(getIntent().hasExtra("To")){
            To = getIntent().getStringExtra("To");
            txtTo.setText(To);
        }

        new SendPostRequest().execute();
        contactList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        new GetSechdule().execute();

    }

    @Override
    public void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }





    public class SendPostRequest extends AsyncTask<String, Void, String> {
        ProgressDialog pdLoading = new ProgressDialog(SearchResult.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading Data...");
            pdLoading.setCancelable(true);
            pdLoading.show();

        }


        protected String doInBackground(String... arg0) {

            try {
                //URL url = new URL("http://192.168.43.146/testing/login.php"); // here is your URL path
                //URL url = new URL("http://10.1.15.67/testing/login.php"); // here is your URL path
                //URL url = new URL("http://192.168.1.140/testing/login.php"); // here is your URL path phone
                URL url = new URL("http://www.224tech.com/KTM_ASSIGN/getFares.php"); // here is your URL path
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("from", From);
                postDataParams.put("to", To);
                //Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                //conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
                //writer.write(getPostDataString(postDataParams));

                writer.write("from=" + From+"&to="+To);
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
            pdLoading.dismiss();
                Toast.makeText(getApplicationContext(), result,
                        Toast.LENGTH_SHORT).show();


            tick_Price = result;
            txtFee.setText("RM"+tick_Price);


        }
    }

    private class GetSechdule extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast.makeText(MainActivity.this,"Json Data is downloading",Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                URL url = new URL("http://www.224tech.com/KTM_ASSIGN/nextTrain.php"); // here is your URL path
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("from", From);
                postDataParams.put("to", To);
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

                writer.write("from=" + From+"&to="+To);
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
                            JSONObject closerTime = contacts.getJSONObject(0);
                            Time = closerTime.getString("arrivingTime");
                            trainNumber = closerTime.getString("trainNumber");
                            // looping through All Contacts
                            for (int i = 1; i < contacts.length(); i++) {
                                JSONObject c = contacts.getJSONObject(i);
                                String trainNumber = c.getString("trainNumber");
                                String arrivingTime = c.getString("arrivingTime");
                                //String role = c.getString("role");

                                // tmp hash map for single contact
                                HashMap<String, String> record = new HashMap<>();

                                // adding each child node to HashMap key => value
                                record.put("trainNumber", trainNumber);
                                record.put("arrivingTime", arrivingTime);


                                // adding contact to contact list
                                contactList.add(record);
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
            ListAdapter adapter = new SimpleAdapter(SearchResult.this, contactList,
                    R.layout.list_item_time, new String[]{ "trainNumber","arrivingTime"},
                    new int[]{R.id.trainNumber, R.id.arrivingTime});
            lv.setAdapter(adapter);
            txtTime.setText(Time);
            txtnumTrain.setText(trainNumber);

        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        getMenuInflater().inflate(R.menu.main_menu, menu);//Menu Resource, Menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent(this, Login.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.UK);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {

                tts.setSpeechRate(0.9f);
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut(){
        String text ="The train "+txtnumTrain.getText().toString()+
                "for "+txtTo.getText().toString()+". will leave at"+
                txtTime.getText().toString()+".Ticket price"+txtFee.getText().toString()+"Ringgit";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
