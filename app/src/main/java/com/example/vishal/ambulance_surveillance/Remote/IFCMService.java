package com.example.vishal.ambulance_surveillance.Remote;

import com.example.vishal.ambulance_surveillance.Models.FCMResponse;
import com.example.vishal.ambulance_surveillance.Models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by sickbay on 3/28/2018.
 */

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAACIac6KM:APA91bHoNrzpmQhsTvHUrvCO2t_3l2GBHaYNGg8TMHCCtRPCdqYYEHiDa8WcRKv2VIOtP6thyFM_2YhYlID7J1wC75dwseJhRTDqjzJEaRdfex6riYSlyft1uRUa36gwQyPd60snZynQ"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
