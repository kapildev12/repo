package com.example.vishal.ambulance_surveillance.Common;

import android.location.Location;

import com.example.vishal.ambulance_surveillance.Models.User;
import com.example.vishal.ambulance_surveillance.Remote.FCMClient;
import com.example.vishal.ambulance_surveillance.Remote.IFCMService;
import com.example.vishal.ambulance_surveillance.Remote.IGoogleAPI;
import com.example.vishal.ambulance_surveillance.Remote.RetrofitClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HP on 12/8/2017.
 */

public class Common {


    public static Location mLastLocation=null;
    public static final String drivers="Drivers";
    public static final String user_drivers="DriversInformation";
    public static final String user_rider="RidersInformation";
    public static final String pickup_request="PickupRequest";
    public static final String token_tbl="Tokens";
    public static User currentUser;
    public static final String UserField="usr";
    public static final String PasField="pwd";


    public static final String baseURL="https://maps.googleapis.com";
    public static final String fcmURL="https://fcm.googleapis.com/";


    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
