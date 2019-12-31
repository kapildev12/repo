package com.example.vishal.ambulance_surveillance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread t = new Thread() {

            public void run() {

                try {

                    sleep(2000);
                    finish();
                    Intent cv = new Intent(SplashActivity.this, LoginRegistrationActivity.class/*otherclass*/);
                    startActivity(cv);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();

    }
}
