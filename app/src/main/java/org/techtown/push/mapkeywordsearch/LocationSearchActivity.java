package org.techtown.push.mapkeywordsearch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationSearchActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_LOCATION_RESULT_PRESENT = 101;

    Handler handler = new Handler();
    ScrollView scrollView;
    Button button;
    Button button2;
    EditText editText;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollview);

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
                textView.setText("");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_LOCATION_RESULT_PRESENT){
            if (resultCode == RESULT_OK){
                String x_ = data.getStringExtra("x");
                String y_ = data.getStringExtra("y");
                textView.setText("x좌표: "+x_+", y좌표: "+y_);
            }
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

        public void run(){

            String url_address = "https://dapi.kakao.com/v2/local/search/keyword.json?page=1&size=15&sort=distance&query="+keyword+"&x=0&y=0";

            if (keyword != "") {

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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {

                    //Log.d("test",result);
                    //textView.setText(result);

                    Intent intent = new Intent(getApplicationContext(), LocationResultPresent.class);
                    intent.putExtra("result",result);
                    startActivityForResult(intent, REQUEST_CODE_LOCATION_RESULT_PRESENT);

                }
            });
        }
    }
}