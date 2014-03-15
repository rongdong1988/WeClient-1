package com.suan.weclient.pushService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.suan.weclient.R;
import com.suan.weclient.activity.MainActivity;
import com.suan.weclient.util.net.DataParser;


public class MessageNotification {
    private static MessageNotification mInstance = null;
    private final static int messageNotificationID = 1000;
    private final static int peopleNotificationID = 2000;
    private NotificationManager mNotificationManager = null;
    public static final String INTENT_ACTION_FROM_NOTIGICATION = "intentFromNotification";
    private Context mContext;



    private MessageNotification(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
    }

    public synchronized static MessageNotification getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MessageNotification(context);
        }
        return mInstance;
    }

    public void createNotification( int amount, String accountName,int userIndex) {
        Notification mNotification = new Notification();
        mNotification.icon = R.drawable.icon_notification;
        mNotification.defaults |= Notification.DEFAULT_SOUND;
        mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotification.defaults |= Notification.DEFAULT_LIGHTS;

        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        Intent intent = new Intent(mContext, MainActivity.class);
/*

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
*/

        intent.putExtra("currentIndex",userIndex);

        PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.contentIntent = mPendingIntent;
        String fromAccount = "(" + mContext.getResources().getString(R.string.from_account) + ":\"" + accountName + "\"" + ")";

        mNotification.tickerText = amount + mContext.getResources().getString(R.string.new_message);

        RemoteViews newMessageRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification_layout);
        newMessageRemoteViews.setTextViewText(R.id.custom_notification_text_content, amount + mContext.getResources().getString(R.string.new_message) + fromAccount);
        newMessageRemoteViews.setImageViewResource(R.id.custom_notification_img_type, R.drawable.profile_head_default);
        mNotification.contentView = newMessageRemoteViews;

        mNotificationManager.cancel(messageNotificationID);
        mNotificationManager.notify(messageNotificationID, mNotification);

    }

    public void clearAllNotification() {
        mNotificationManager.cancelAll();
    }

}
