package com.example.vishal.ambulance_surveillance.Models;

/**
 * Created by sickbay on 3/28/2018.
 */

public class Result {


    public String message_id;

    public Result() {
    }

    public Result(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
}
