package com.example.vishal.ambulance_surveillance.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by HP on 12/8/2017.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);
}
