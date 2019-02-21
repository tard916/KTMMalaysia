package com.a224tech.bmc208_assignment2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomActivity extends AppCompatActivity {

    TextView txtWelcome;
    ImageView imgWelcome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        imgWelcome = (ImageView) findViewById(R.id.imgWelcome);

        Animation myWelcomeAnim = AnimationUtils.loadAnimation(this,R.anim.mywelcoming);
        txtWelcome.startAnimation(myWelcomeAnim);
        imgWelcome.startAnimation(myWelcomeAnim);

        final Intent nextActivity= new Intent(this, MainActivity.class);

        Thread timer = new Thread(){
            public void run (){
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(nextActivity);
                    finish();
                }
            }
        };

        timer.start();
    }
}
