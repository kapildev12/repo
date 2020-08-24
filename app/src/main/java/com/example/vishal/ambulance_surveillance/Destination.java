package com.example.vishal.ambulance_surveillance;

import android.os.Parcel;
import android.os.Parcelable;

public class Destination implements Parcelable {

    public static final Creator<Destination> CREATOR = new Creator<Destination>() {
        @Override
        public Destination createFromParcel(Parcel in) {
            return new Destination(in);
        }

        @Override
        public Destination[] newArray(int size) {
            return new Destination[size];
        }
    };
    private final String name;
    private final String address;
    private final double lat;
    private final double lng;

    public Destination(String name, String address, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    protected Destination(Parcel in) {
        name = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
    }
}
