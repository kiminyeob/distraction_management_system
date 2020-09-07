package org.techtown.push.mapkeywordsearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationSearchActivity extends AppCompatActivity implements AutoPermissionsListener {

    Handler handler = new Handler();
    Button button;
    Button button2;
    EditText editText;
    TextView textView;

    SupportMapFragment mapFragment;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map=googleMap;
            }
        });

        try{
            MapsInitializer.initialize(this);
        } catch (Exception e){
            e.printStackTrace();
        }

        startLocationService();

        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundThreadKaKaoMap thread = new BackgroundThreadKaKaoMap();
                thread.start();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textView.setText("");
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_CODE_LOCATION_RESULT_PRESENT){
            if (resultCode == RESULT_OK){
                String x_ = data.getStringExtra("x");
                String y_ = data.getStringExtra("y");
                //textView.setText("x좌표: "+x_+", y좌표: "+y_);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode,permissions, this);
    }

    @Override
    public void onDenied(int i, String[] permissions) {
        Toast.makeText(this,"permission denied : "+permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int i, String[] permissions) {
        Toast.makeText(this,"permission granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    public void startLocationService(){
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        try{
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null){
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "최근 위치 -> Latitude : "+latitude+", longitude: "+longitude;
                textView.setText(message);
            }

            GPSListener gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);

        } catch (SecurityException e){
            e.printStackTrace();
        }

    }

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
            3. JSON으로 결과 값 받아서 처리하는 코드 추가

        */

        String keyword = editText.getText().toString();
        String result = "";
        boolean flag;

        public void run(){

            String url_address = "https://dapi.kakao.com/v2/local/search/keyword.json?page=1&size=15&sort=distance&query="+keyword+"&x=0&y=0";

            if (keyword.compareTo("") != 0) { // 키워드가 비워져 있지 않으면

                try {
                    URL url = new URL(url_address);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    // header 추가하기
                    conn.setRequestProperty("Authorization", "KakaoAK 1b6cd110fcde5815d96f676aaab63db2");

                    InputStream is = conn.getInputStream();

                    InputStreamReader responseBodyReader = new InputStreamReader(is, "UTF-8");

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


            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (flag) {
                        if (!result.contains("\"total_count\":1")){ // 결과 값이 없으면
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
}