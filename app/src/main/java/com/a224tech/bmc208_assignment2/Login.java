package com.a224tech.bmc208_assignment2;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class Login extends Activity {


    private Button btnLogin;

    private EditText inputName;
    private EditText inputPassword;

    String username = "";
    String password = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputName = (EditText) findViewById(R.id.username);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = inputName.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!name.isEmpty() && !password.isEmpty()) {
                    submitLogin(view);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });
    }

    public void submitLogin(View view)
    {
        username = inputName.getText().toString();
        password = inputPassword.getText().toString();

        //Toast.makeText(getApplicationContext(),username+" "+password, Toast.LENGTH_LONG).show();

        new SendPostRequest().execute();
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {



        protected String doInBackground(String... arg0) {

            try {
                //URL url = new URL("http://192.168.43.146/testing/login.php"); // here is your URL path
                //URL url = new URL("http://10.1.15.67/testing/login.php"); // here is your URL path
                //URL url = new URL("http://192.168.1.140/testing/login.php"); // here is your URL path phone
                 URL url = new URL("http://www.224tech.com/KTM_ASSIGN/login.php"); // here is your URL path
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name", username);
                postDataParams.put("pass", password);
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

                writer.write("name=" + username+"&pass="+password);
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
            String check = "You login successfully" ;
            if (Objects.equals(result, check)){
                Intent intent = new Intent(Login.this, Admin_Home.class);
                intent.putExtra("userName",username);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), result,
                        Toast.LENGTH_SHORT).show();
            }else{

                Toast.makeText(getApplicationContext(), result,
                        Toast.LENGTH_LONG).show();
            }


        }
    }

    /*public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            //result.append(URLEncoder.encode(key, "UTF-8"));
            //result.append("=");
            //result.append(URLEncoder.encode(value.toString(), "UTF-8"));
            result.append(key);
            result.append("=");
            result.append(value.toString());
        }

        return result.toString();
    }*/
}