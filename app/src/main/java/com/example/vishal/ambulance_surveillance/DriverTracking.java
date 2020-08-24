package com.example.vishal.ambulance_surveillance;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.vishal.ambulance_surveillance.Common.Common;
import com.example.vishal.ambulance_surveillance.Models.FCMResponse;
import com.example.vishal.ambulance_surveillance.Models.Notification;
import com.example.vishal.ambulance_surveillance.Models.Sender;
import com.example.vishal.ambulance_surveillance.Models.Token;
import com.example.vishal.ambulance_surveillance.Remote.IFCMService;
import com.example.vishal.ambulance_surveillance.Remote.IGoogleAPI;
import com.example.vishal.ambulance_surveillance.helper.DirectionJSONParser;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    double riderLat, riderLng;
    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;
    String customerId;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(DriverTracking.this, "Patient cancelled the booking", Toast.LENGTH_LONG).show();
            finish();
        }
    };
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Polyline direction;
    private Marker driverMarker;
    private Button startTrip;
    private Button destByNear;
    private Button destByInjury;
    private Destination destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startTrip = (Button) findViewById(R.id.buttonstart);
        destByNear = (Button) findViewById(R.id.DestinationByNear);
        destByInjury = (Button) findViewById(R.id.DestinationByInjury);

        startTrip.setVisibility(View.INVISIBLE);
        destByInjury.setVisibility(View.INVISIBLE);
        destByNear.setVisibility(View.INVISIBLE);

        if (getIntent() != null) {
            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderLng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("CustomerId");
        }
        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();
        setUpLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter("Cancel"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "this device is not supported", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private void setUpLocation() {


        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
            displayLocation();

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.drivers));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(riderLat, riderLng), 0.01f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(customerId);
                StartRide();
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });

    }

    private void StartRide() {
        startTrip.setVisibility(View.VISIBLE);

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverTracking.this, DestinationActivity.class);
                startActivityForResult(intent, 1);
             /*   startTrip.setVisibility(View.GONE);
                destByInjury.setVisibility(View.GONE);
                destByNear.setVisibility(View.VISIBLE);

                destByNear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DriverTracking.this, DestinationActivity.class);
                        startActivityForResult(intent, 1);
                    }
                });
                destByInjury.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DriverTracking.this, InjuryType.class);
                        startActivity(intent);
                    }
                });*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1 && data != null) {
            sendStartedNotification(customerId);
            Toast.makeText(this, "Trip started", Toast.LENGTH_LONG).show();
            Destination destination = data.getParcelableExtra("destination");
            this.destination = destination;
            if (direction != null)
                direction.remove();
            getDirection(destination.getLat(), destination.getLng());
            startTrip.setText("End trip");
            startTrip.setOnClickListener(v -> {
                sendEndedNotification(customerId);
                Toast.makeText(this, "Trip ended! Please take patient to hospital!", Toast.LENGTH_LONG).show();
                finish();
            });

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(destination.getLat(), destination.getLng()))
                    .title(destination.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker()));
        }
    }

    private void sendStartedNotification(String customerId) {

        Notification notification = new Notification("Started", "Your trip is started!");
        Sender send = new Sender(customerId, notification);


        mFCMService.sendMessage(send).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {

            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendEndedNotification(String customerId) {

        Notification notification = new Notification("Ended", "Your trip is ended! Please take patient to hospital");
        Sender send = new Sender(customerId, notification);


        mFCMService.sendMessage(send).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {

            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }


    private void sendArrivedNotification(String customerId) {

        Token token = new Token(customerId);
        Notification notification = new Notification("Arrived", String.format("The driver is arrived at your location", Common.currentUser.getName()));
        Sender send = new Sender(token.getToken(), notification);


        mFCMService.sendMessage(send).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success != 1) {
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void sendLocationUpdate(String customerId, Location location) {

        Token token = new Token(customerId);
        String body = location.getLatitude() + ";" + location.getLongitude();
        Notification notification = new Notification("Location", body);
        Sender send = new Sender(token.getToken(), notification);


        mFCMService.sendMessage(send).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {

            }

            @Override
            public void onFailure(@NonNull Call<FCMResponse> call, @NonNull Throwable t) {

            }
        });

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation(Location location, double lat, double lng) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (location != null) {

            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();
            if (driverMarker != null) {
                driverMarker.remove();

            }
            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("you").icon(BitmapDescriptorFactory.defaultMarker()));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

            if (direction != null) {
                direction.remove();
            }

            getDirection(lat, lng);
        } else {
            Log.d("ERROR", "cannot display Location ");
        }

    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation != null) {

            final double latitude = Common.mLastLocation.getLatitude();
            final double longitude = Common.mLastLocation.getLongitude();
            if (driverMarker != null) {
                driverMarker.remove();

            }
            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("you").icon(BitmapDescriptorFactory.defaultMarker()));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17.0f));

            if (direction != null) {
                direction.remove();
            }

            getDirection();
        } else {
            Log.d("ERROR", "cannot display Location ");
        }

    }

    private void getDirection(double lat, double lng) {

        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);


            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        new ParserTask().execute(response.body().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {
                    Toast.makeText(DriverTracking.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void getDirection() {

        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + riderLat + "," + riderLng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);


            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {

                        new ParserTask().execute(response.body());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {
                    Toast.makeText(DriverTracking.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        if (destination != null)
            displayLocation(location, destination.getLat(), destination.getLng());
        else
            displayLocation();
        sendLocationUpdate(customerId, location);
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Requesting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... params) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(params[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();


            ArrayList points = new ArrayList();
            PolylineOptions polylineOptions = null;


            for (int i = 0; i < lists.size(); i++) {

                List<HashMap<String, String>> path = lists.get(i);


                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);


                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);


                }
                polylineOptions = new PolylineOptions();
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            if (lists.size() != 0) {
                direction = mMap.addPolyline(polylineOptions);
            }
        }
    }
}
