package org.techtown.push.mapkeywordsearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationSearchActivity extends AppCompatActivity {

    Handler handler = new Handler();
    Button button;
    EditText editText;
    SupportMapFragment mapFragment;
    GoogleMap map;
    MarkerOptions myLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);
        editText = findViewById(R.id.editText);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map=googleMap;
                map.setMyLocationEnabled(true);
            }
        });

        try{
            MapsInitializer.initialize(this);
        } catch (Exception e){
            e.printStackTrace();
        }

        // REST API를 통해 검색 결과를 가져오는 메서드 출력
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            BackgroundThreadKaKaoMap thread = new BackgroundThreadKaKaoMap();
            thread.start();
            }
        });

        startLocationService();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        if (map != null){
            map.setMyLocationEnabled(true);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        super.onPause();
        if (map != null){
            map.setMyLocationEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_CODE_LOCATION_RESULT_PRESENT){
            if (resultCode == RESULT_OK){
                String x_ = data.getStringExtra("x");
                String y_ = data.getStringExtra("y");
                String locationName = data.getStringExtra("locationName");
                LatLng curPoint = new LatLng(Double.parseDouble(y_), Double.parseDouble(x_));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
                showLocationMarker(curPoint, locationName);
            }
        }
    }

    public void startLocationService(){

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try{
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "최근 나의 위치 -> Latitude : "+latitude+", longitude: "+longitude;
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(getApplicationContext(),"최근 위치를 가져오지 못했습니다.",Toast.LENGTH_LONG).show();
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 1000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);

        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    // 카카오 맵에서 위치 정보를 가져오는 코드
    class BackgroundThreadKaKaoMap extends Thread {

        /*

        [참고]

            editText는 class에서 public variable로 선언되어 있어야 한다.

         */

        /*

        [to do]

            1. url_address의 request를 변수로 튜닝할 있도록 코드를 수정하면 좋을 것 같음

            String x;
            String y;
            String size;
            String page;

            2. 인터넷에 연결이 안되어 있거나, 결과 값을 받아오지 못할 시에 처리할 코드 작성

        */

        String keyword = editText.getText().toString();
        String result = "";
        boolean flag;
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 대전 시청 위치를 default로
        String latitude = "127.384776";
        String longitude = "36.350642";

        public void run(){
            try {
                @SuppressLint("MissingPermission") Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());
                }
            }catch (SecurityException e){
                e.printStackTrace();
            }

            String url_address = "https://dapi.kakao.com/v2/local/search/keyword.json?page=1&size=15&sort=distance&query="+keyword+"&x="+longitude+"&y="+latitude;

            if (keyword.compareTo("") != 0) { // 키워드가 비워져 있지 않으면

                try {
                    URL url = new URL(url_address);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // header 추가하기
                    conn.setRequestProperty("Authorization", "KakaoAK 1b6cd110fcde5815d96f676aaab63db2");

                    InputStream is = conn.getInputStream();

                    //Get the stream
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    // Set the result
                    result = builder.toString();

                    flag = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                flag = false;
            }

            // 검색 결과가 있으면 LocationResultPresent Activity 에서 결과를 출력한다.
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (flag) {
                        if (result.contains("\"total_count\":0")){ // 결과 값이 없으면
                            Toast.makeText(getApplicationContext(),"결과 값이 없습니다. 키워드를 확인해 주세요",Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), LocationResultPresent.class);
                            intent.putExtra("result", result);
                            startActivityForResult(intent, Constants.REQUEST_CODE_LOCATION_RESULT_PRESENT);
                        }
                    } else{
                        Toast.makeText(getApplicationContext(),"키워드를 입력해주세요",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public class GPSListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            // showCurrentLocation(latitude, longitude);
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

        // 위도, 경도 위치로 지도로 이동
        private void showCurrentLocation(Double latitude, Double longitude){
            LatLng curPoint = new LatLng(latitude, longitude);
            map.animateCamera(CameraUpdateFactory.newLatLng(curPoint));
        }
    }

    private void showLocationMarker(LatLng curPoint, String locationName){
        map.clear();
        myLocationMarker = new MarkerOptions();
        myLocationMarker.position(curPoint);
        myLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
        myLocationMarker.title(locationName+"\n");
        myLocationMarker.snippet("GPS로 확인한 위치");
        map.addMarker(myLocationMarker);
    }
}