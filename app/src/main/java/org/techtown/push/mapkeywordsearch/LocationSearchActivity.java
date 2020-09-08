package org.techtown.push.mapkeywordsearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
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

    // Thread 내에서 UI에 접근하기 위한 핸들러
    Handler handler = new Handler();

    // user input 및 출력 관련 view
    Button button;
    EditText editText;
    TextView textView_placeName;
    TextView textView_latitude;
    TextView textView_longitude;
    TextView textView_distance;

    //지도 관련 view
    SupportMapFragment mapFragment;
    GoogleMap map;
    MarkerOptions myLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);
        editText = findViewById(R.id.editText);
        textView_placeName = findViewById(R.id.locationName);
        textView_latitude = findViewById(R.id.latitude);
        textView_longitude = findViewById(R.id.longitude);
        textView_distance = findViewById(R.id.distance);

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


    // LocationResultPresent로부터 결과 값(GPS값과 장소 이름)을 intent를 통해서 받아오고 지도에 표시
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_CODE_LOCATION_RESULT_PRESENT){
            if (resultCode == RESULT_OK){
                String x_ = data.getStringExtra("x"); // longitude
                String y_ = data.getStringExtra("y"); // latitude
                String locationName = data.getStringExtra("locationName");
                LatLng curPoint = new LatLng(Double.parseDouble(y_), Double.parseDouble(x_));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
                showLocationMarker(curPoint, locationName);
                textView_placeName.setText(locationName);
                textView_latitude.setText(y_);
                textView_longitude.setText(x_);
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
        [to do] 인터넷에 연결이 안되어 있거나, 결과 값을 받아오지 못할 시에 처리할 코드 작성
        */

        String keyword = editText.getText().toString(); // 키워드 값을 editText로부터 가져옴
        String result = ""; // 결과 값을 저장할 문자열
        boolean flag; // 결과 값이 없거나 editText가 empty면 false, 그외에는 true
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 대전 시청 위치를 default로
        String latitude = "127.384776";
        String longitude = "36.350642";

        public void run(){
            try {
                // 마지막으로 tracking 한 GPS 값을 가져온다.
                @SuppressLint("MissingPermission") Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = Double.toString(location.getLatitude());
                    longitude = Double.toString(location.getLongitude());
                }
            }catch (SecurityException e){
                e.printStackTrace();
            }

            //마지막으로 tracking한 값을 기준으로 검색 결과를 가져옴 (REST API)
            String url_address = "https://dapi.kakao.com/v2/local/search/keyword.json?page=1&size=15&sort=distance&query="+keyword+"&x="+longitude+"&y="+latitude;

            if (keyword.compareTo("") != 0) { // 키워드가 비워져 있지 않으면

                try {
                    URL url = new URL(url_address);
                    String KaKaoKeyValue = "1b6cd110fcde5815d96f676aaab63db2";
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // 헤더 추가하기 (키 값)
                    conn.setRequestProperty("Authorization", "KakaoAK "+KaKaoKeyValue);

                    //Get the stream
                    InputStream is = conn.getInputStream();
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    //결과 값을 읽어 들인다.
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
                            flag = false;
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

            // 검색된 location이 없을 때만 distance를 구함(if 문을 넣지 않으면 null exception 출력됨)
            if (textView_placeName.getText().toString()!="")
                getDistance(latitude, longitude);
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

        // 위도(latitude), 경도(longitude) 위치로 지도로 이동
        private void showCurrentLocation(Double latitude, Double longitude){
            LatLng curPoint = new LatLng(latitude, longitude);
            map.animateCamera(CameraUpdateFactory.newLatLng(curPoint));
        }

        // 거리 계산 메서드
        public double getDistance(double lat , double lng){
            double distance;
            Location locationA = new Location("point A");
            locationA.setLatitude(lat);
            locationA.setLongitude(lng);

            Location locationB = new Location("point B");
            locationB.setLatitude(Double.parseDouble(textView_latitude.getText().toString()));
            locationB.setLongitude(Double.parseDouble(textView_longitude.getText().toString()));

            distance = Math.round(locationA.distanceTo(locationB)*100)/100.0;



            // 사용자의 위치와 지정된 위치가 일정 거리 이하이면 특정 액션이 수행되는 샘플 코드
            if(distance > 50){
                textView_distance.setTextColor(Color.BLACK);
                textView_distance.setText("선택한 위치와 현재 나와의 거리: "+(Double.toString(distance))+"m");
            } else {
                textView_distance.setTextColor(Color.RED);
                textView_distance.setText("선택한 위치와 현재 나와의 거리: "+(Double.toString(distance))+"m (액션이 트리거 됩니다!");

                // action test
                changeRingerModeAction(Constants.CHANGE_TO_MUTE);
            }

            return distance;
        }


    }

    // 지도 상에 marker를 표시한다.
    private void showLocationMarker(LatLng curPoint, String locationName){
        map.clear();
        myLocationMarker = new MarkerOptions();
        myLocationMarker.position(curPoint);
        myLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
        myLocationMarker.title(locationName+"\n");
        myLocationMarker.snippet("GPS로 확인한 위치");
        map.addMarker(myLocationMarker);
    }


    // ringer mode change test functions
    public void changeRingerModeAction(int mode){

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        switch (mode){
            case Constants.CHANGE_TO_MUTE:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;

            case Constants.CHANGE_TO_SOUND:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;

            case Constants.CHANGE_TO_VIBRATE:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
        }
    }
}