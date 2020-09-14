package org.techtown.push.mapkeywordsearch;

import android.app.Notification;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/*

구현해야 할 내용: keyword 나 app list를 설정하는 코드를 삽입해야 해야 함
Blocking 하는 동안에는 "어떤 서비스" 가 돌면서 blocking 중이라는 표시를 해줘야 할 것 같음(FOCUS BOT처럼)

 */

public class NotificationFilteringListener extends NotificationListenerService {

    public final static String TAG = "NotificationFilteringListener";

    public NotificationFilteringListener() {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.d(TAG, "onNotificationRemoved ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId());
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        CharSequence message = extras.getCharSequence(Notification.EXTRA_TEXT_LINES);
        Icon smallIcon = notification.getSmallIcon();
        Icon largeIcon = notification.getLargeIcon();

        String msg = "onNotificationPosted ~ " +
                " packageName: " + sbn.getPackageName() +
                " id: " + sbn.getId() +
                " postTime: " + sbn.getPostTime() +
                " title: " + title +
                " text : " + text +
                " subText: " + subText;

        Log.d(TAG, msg);

        if (title != null) {
            if (text.toString().contains("키워드")) {
                NotificationFilteringListener.this.cancelNotification(sbn.getKey());
            }
        }
    }
}
