
package com.ucan.app.common.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Build.VERSION_CODES;

public class NotificationUtil {

    public static final String TAG = LogUtil.getLogUtilsTag(Notification.class);

    @TargetApi(VERSION_CODES.HONEYCOMB)
    public static Notification buildNotification(Context context, int icon,
                                                 int defaults, boolean onlyVibrate, String tickerText,
                                                 String contentTitle, String contentText, Bitmap largeIcon,
                                                 PendingIntent intent) {

            Notification.Builder builder = new Notification.Builder(context);
            builder.setLights(0xff0000ff, 300, 1000)
                    .setSmallIcon(icon)
                    .setTicker(tickerText)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(intent);
            if (onlyVibrate) {
                defaults &= Notification.DEFAULT_VIBRATE;
            }
            LogUtil.d(TAG, "defaults flag " + defaults);
            builder.setDefaults(defaults);
            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon);
            }
            return builder.build();
    }

}
