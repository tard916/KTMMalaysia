package com.a224tech.bmc208_assignment2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomActivity extends AppCompatActivity {

    TextView txtWelcome;
    ImageView imgWelcome;
    MediaPlayer mMediaPlayer;
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

        mMediaPlayer = MediaPlayer.create(WelcomActivity.this,R.raw.trains);
        mMediaPlayer.start();

        Thread timer = new Thread() {
            public void run() {

                try {

                    sleep(7000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {

                    //onPause();
                    startActivity(nextActivity);
                    finish();
                }
            }
        };


        timer.start();
    }
    protected void onPause(){
        super.onPause();
        mMediaPlayer.release();
            }
}
