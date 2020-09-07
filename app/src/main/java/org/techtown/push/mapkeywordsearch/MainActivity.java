package org.techtown.push.mapkeywordsearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationSearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                        Intent.FLAG_ACTIVITY_SINGLE_TOP|
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, Constants.REQUEST_CODE_LOCATION_SEARCH_INTERFACE);
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this,101);
    }

    // 권한 받는 코드
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
}