package com.example.vishal.ambulance_surveillance.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sickbay on 3/28/2018.
 */

public class FCMClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseURL)
    {
        if(retrofit==null) {
            retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
