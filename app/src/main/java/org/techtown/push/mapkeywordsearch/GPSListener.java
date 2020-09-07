package org.techtown.push.mapkeywordsearch;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class GPSListener implements LocationListener{

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        LocationSearchActivity activity = new LocationSearchActivity(); // 가능한 코드인가?

        String message = "내 위치 -> Latitude : "+latitude+", longitude: "+longitude;
        activity.textView.setText(message);

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}
