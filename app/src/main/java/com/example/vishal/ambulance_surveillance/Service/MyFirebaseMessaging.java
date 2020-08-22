package com.example.vishal.ambulance_surveillance.Service;

import android.content.Intent;

import com.example.vishal.ambulance_surveillance.CustomerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

/**
 * Created by sickbay on 3/28/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification().getTitle().equals("Cancel")) {
            Intent intent = new Intent("Cancel");
            getApplicationContext().sendBroadcast(intent);

        } else {
            LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", customer_location.latitude);
            intent.putExtra("lng", customer_location.longitude);
            intent.putExtra("customer", remoteMessage.getNotification().getTitle());
            intent.putExtra("name", remoteMessage.getNotification().getTag());
            startActivity(intent);
        }

    }
}