package com.example.vishal.ambulance_surveillance;

import android.support.annotation.NonNull;

import java.util.List;

public interface OnListReady {

    void onListReady(@NonNull List<Destination> destinationList);
}
