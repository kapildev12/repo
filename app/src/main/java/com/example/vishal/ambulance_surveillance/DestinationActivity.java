package com.example.vishal.ambulance_surveillance;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.example.vishal.ambulance_surveillance.Common.Common;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DestinationActivity extends AppCompatActivity implements OnListReady,
        DestinationAdapter.DestinationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        new GetPlaces(this).execute(getUrl());
    }

    private String getUrl() {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" +
                Common.mLastLocation.getLatitude() +
                "," +
                Common.mLastLocation.getLongitude() + "&radius=" +
                "30000" +
                "&type=" +
                "hospital" +
                "&sensor=true" +
                "&fields=formatted_address,name,geometry,vicinity" +
                "&key=" +
                getResources().getString(R.string.google_direction_api);
    }

    @Override
    public void onListReady(@NonNull List<Destination> destinations) {
        DestinationAdapter adapter = new DestinationAdapter(destinations, this);
        RecyclerView rvDestinations = (RecyclerView) findViewById(R.id.rvDestinations);
        rvDestinations.setAdapter(adapter);
    }

    @Override
    public void onSelectDestination(@NonNull Destination destination) {
        Intent data = new Intent();
        data.putExtra("destination", destination);
        setResult(RESULT_OK, data);
        finish();
    }


    private static class GetPlaces extends AsyncTask<String, String, String> {

        @NonNull
        private final OnListReady listReady;

        public GetPlaces(@NonNull OnListReady listReady) {
            this.listReady = listReady;
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            DownloadURL downloadURL = new DownloadURL();
            try {
                return downloadURL.readUrl(url);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            DataParser parser = new DataParser();
            List<HashMap<String, String>> nearbyPlaceList = parser.parse(s);
            List<Destination> destinations = new ArrayList<>();
            for (HashMap<String, String> map : nearbyPlaceList) {
                double lat = Double.parseDouble(map.get("lat"));
                double lng = Double.parseDouble(map.get("lng"));
                Destination destination = new Destination(map.get("place_name"), map.get("address"), lat, lng);
                destinations.add(destination);
            }
            listReady.onListReady(destinations);
        }
    }
}