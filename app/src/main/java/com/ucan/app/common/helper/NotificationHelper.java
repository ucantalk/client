package com.ucan.app.common.helper;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Looper;

import com.ucan.app.R;
import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.db.ConversationSqlManager;
import com.ucan.app.base.db.GroupNoticeSqlManager;
import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.NotificationUtil;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.ui.activities.MainActivity;
import com.yuntongxun.ecsdk.ECMessage;


/**
 * 状态栏通知
 */
public class NotificationHelper {

    public static final int NOTIFY_ID_PUSHCONTENT = 35;

    private Context mContext;

    private static NotificationManager mNotificationManager;

    public static NotificationHelper mInstance;

    public static NotificationHelper getInstance() {
        if (mInstance == null) {
            mInstance = new NotificationHelper(AppManager.getContext());
        }

        return mInstance;
    }

    MediaPlayer mediaPlayer = null;

    public void playNotificationMusic(String voicePath) throws IOException {
        //paly music ...
        AssetFileDescriptor fileDescriptor = mContext.getAssets().openFd(voicePath);
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
        mediaPlayer.prepare();
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }


    private NotificationHelper(Context context) {
        mContext = context;
    }

    public final void showCustomNewMessageNotification(Context context, String pushContent, String fromUserName, String sessionId, int lastMsgType) {
        LogUtil.w(LogUtil.getLogUtilsTag(NotificationHelper.class),
                "showCustomNewMessageNotification pushContent： " + pushContent
                        + ", fromUserName: " + fromUserName + " ,sessionId: " + sessionId + " ,msgType: " + lastMsgType);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("nofification_type", "pushcontent_notification");
        intent.putExtra("Intro_Is_Muti_Talker", true);
        intent.putExtra("Main_FromUserName", fromUserName);
        intent.putExtra("Main_Session", sessionId);
        intent.putExtra("MainUI_User_Last_Msg_Type", lastMsgType);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 35, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String tickerText = getTickerText(mContext, fromUserName, lastMsgType);
        int sessionUnreadCount = ConversationSqlManager.getInstance().qureySessionUnreadCount();
        int allSessionUnreadCount = ConversationSqlManager.getInstance().qureyAllSessionUnreadCount();
        String contentTitle = getContentTitle(context, sessionUnreadCount, fromUserName);
        String contentText = getContentText(context, sessionUnreadCount, allSessionUnreadCount, pushContent, lastMsgType);

        boolean shake = UCPreferences.getSharedPreferences().getBoolean(PreferenceSettings.SETTINGS_NEW_MSG_SHAKE.getId(), true);
        boolean sound = UCPreferences.getSharedPreferences().getBoolean(PreferenceSettings.SETTINGS_NEW_MSG_SOUND.getId(), true);
        int defaults;
        if ((sound && shake)) {
            defaults = Notification.DEFAULT_ALL;
            shake = false;
        } else if ((sound && !shake)) {
            defaults = Notification.DEFAULT_SOUND;
            shake = false;
        } else if (!sound && shake) {
            defaults = Notification.DEFAULT_VIBRATE;
            shake = true;
        } else if (!sound && !shake) {
            defaults = Notification.DEFAULT_LIGHTS;
            shake = true;
        } else {
            defaults = Notification.DEFAULT_ALL;
            shake = false;
        }

        Notification notification = NotificationUtil.buildNotification(context, R.drawable.title_bar_logo, defaults, shake, tickerText, contentTitle, contentText, null, pendingIntent);
        notification.flags = (Notification.FLAG_AUTO_CANCEL | notification.flags);
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_ID_PUSHCONTENT, notification);
    }


    /**
     * @param contex
     * @param fromUserName
     * @param msgType
     * @return
     */
    public final String getTickerText(Context contex, String fromUserName, int msgType) {
        if (msgType == ECMessage.Type.TXT.ordinal()) {
            return contex.getResources().getString(R.string.notification_fmt_one_txttype, fromUserName);
        } else if (msgType == ECMessage.Type.IMAGE.ordinal()) {
            return contex.getResources().getString(R.string.notification_fmt_one_imgtype, fromUserName);
        } else if (msgType == ECMessage.Type.VOICE.ordinal()) {
            return contex.getResources().getString(R.string.notification_fmt_one_voicetype, fromUserName);
        } else if (msgType == ECMessage.Type.FILE.ordinal()) {
            return contex.getResources().getString(R.string.notification_fmt_one_filetype, fromUserName);
        } else if (msgType == GroupNoticeSqlManager.NOTICE_MSG_TYPE) {
            return contex.getResources().getString(R.string.str_system_message_group_notice);
        } else {
            //return contex.getResources().getString(R.string.app_name);
            return contex.getPackageManager().getApplicationLabel(contex.getApplicationInfo()).toString();
        }

    }

    public final String getContentTitle(Context context, int sessionUnreadCount, String fromUserName) {
        if (sessionUnreadCount > 1) {
            return context.getString(R.string.app_name);
        }

        return fromUserName;
    }

    /**
     * @param context
     * @return
     */
    public final String getContentText(Context context, int sessionCount, int sessionUnread, String pushContent, int lastMsgType) {

        if (sessionCount > 1) {

            return context.getResources().getQuantityString(
                    R.plurals.notification_fmt_multi_msg_and_talker, 1,
                    sessionCount, sessionUnread);
        }

        if (sessionUnread > 1) {
            return context.getResources().getQuantityString(
                    R.plurals.notification_fmt_multi_msg_and_one_talker, sessionUnread, sessionUnread);
        }

        if (lastMsgType == ECMessage.Type.TXT.ordinal()) {
            return pushContent;
        } else if (lastMsgType == ECMessage.Type.FILE.ordinal()) {
            return context.getResources().getString(R.string.app_file);
        } else if (lastMsgType == ECMessage.Type.VOICE.ordinal()) {
            return context.getResources().getString(R.string.app_voice);
        } else if (lastMsgType == ECMessage.Type.IMAGE.ordinal()) {
            return context.getResources().getString(R.string.app_pic);
        } else {
            return pushContent;
        }

    }

    private void cancel() {
        NotificationManager notificationManager = (NotificationManager) AppManager
                .getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancel(0);
    }

    /**
     * 取消所有的状态栏通知
     */
    public final void forceCancelNotification() {
        cancel();
        NotificationManager notificationManager = (NotificationManager) AppManager
                .getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancel(NOTIFY_ID_PUSHCONTENT);

    }

    public final Looper getLooper() {
        return Looper.getMainLooper();
    }

    public final void showKickoffNotification(Context context, String kickofftext) {

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("nofification_type", "pushcontent_notification");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 35, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = NotificationUtil.buildNotification(context, R.drawable.ic_launcher, Notification.FLAG_SHOW_LIGHTS, true, kickofftext, "", "", null, pendingIntent);
        notification.flags = (Notification.FLAG_AUTO_CANCEL | notification.flags);
        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFY_ID_PUSHCONTENT, notification);
    }

    /**
     * 后台呈现音视频呼叫Notification
     *
     * @param
     */
//    public static void showCallingNotification(ECVoIPCallManager.CallType callType) {
//        try {
//            getInstance().checkNotification();
//            String topic = getInstance().mContext.getString(R.string.ec_voip_is_talking_tip);
//            Notification notification = new Notification(R.drawable.title_bar_logo, null,
//                    System.currentTimeMillis());
//            notification.flags = Notification.FLAG_AUTO_CANCEL;
//            notification.tickerText = topic;
//            Intent intent;
//            if(callType == ECVoIPCallManager.CallType.VIDEO) {
//                intent = new Intent(ECVoIPBaseActivity.ACTION_VIDEO_CALL);
//            } else {
//                intent = new Intent(ECVoIPBaseActivity.ACTION_VOICE_CALL);
//            }
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent contentIntent = PendingIntent.getActivity(getInstance().mContext,
//                    R.string.app_name,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//            notification.setLatestEventInfo(getInstance().mContext,
//                    topic,
//                    null,
//                    contentIntent);
//
//            mNotificationManager.notify(CCP_NOTIFICATOIN_ID_CALLING, notification);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private void checkNotification() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

    }

    public static void cancelCCPNotification(int id) {
        getInstance().checkNotification();
        mNotificationManager.cancel(id);
    }
}
