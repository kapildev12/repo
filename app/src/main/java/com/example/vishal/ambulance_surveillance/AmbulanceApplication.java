package com.example.vishal.ambulance_surveillance;

import android.app.Application;
import android.content.Context;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AmbulanceApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();
       // CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Exo-Medium.ttf").setFontAttrId(R.attr.fontPath).build());
    }
}
