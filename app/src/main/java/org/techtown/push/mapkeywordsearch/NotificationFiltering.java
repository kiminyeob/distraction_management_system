package org.techtown.push.mapkeywordsearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Set;

// 노티피케이션 filtering 하는 서비스를 실행시키는 activity
public class NotificationFiltering extends AppCompatActivity {

    Button startService;
    Button finishService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_filtering);

        startService = findViewById(R.id.start_notification_service);
        finishService = findViewById(R.id.finish_notification_service);

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (true){ // 체크할 것이 있다면
                    if (!permissionGrantred()) {
                        Intent intent = new Intent(
                                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        startActivity(intent);
                    }
                }
            }
        });

        finishService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (true){ // 체크할 것이 있다면
                    // 해제하는 코드?
                }
            }
        });

    }

    private boolean permissionGrantred() {
        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (sets != null && sets.contains(getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

}