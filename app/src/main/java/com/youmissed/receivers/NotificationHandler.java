package com.youmissed.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.util.Log;

import com.youmissed.R;
import com.youmissed.activities.MainActivity;
import com.youmissed.app.CallUserIdentifier;

import java.util.Date;

/**
 * Created by steve on 1/14/17.
 */

public class NotificationHandler {
    // Notification handler singleton
    private static NotificationHandler nHandler;
    private static NotificationManager mNotificationManager;
    Bitmap bitmap;
    // Sets an ID for the notification, so it can be updated
    int notifyID = 1;
    String contactName;
    private Boolean smsSent;
    private String resultSmsTask;

    public NotificationHandler() {
    }

    /**
     * Singleton pattern implementation
     *
     * @return
     */
    public static NotificationHandler getInstance(Context context) {
        if (nHandler == null) {
            nHandler = new NotificationHandler();
            mNotificationManager =
                    (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return nHandler;
    }

    /**
     * Show a determinate and undeterminate progress notification
     *
     * @param context, activity context
     * @param number
     * @param message
     * @param start
     */
    public void createProgressNotification(final Context context, final String number, final String message, Date start) {
        // used to update the progress notification
        contactName = new CallUserIdentifier().getUserNameFromContacts(context, number);
        final int progresID = 2989;
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("toStopSendingSms", true);
        intent.putExtra("contact_name", contactName);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context.getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);
        // building the notification
        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setAutoCancel(false)
                .setContentTitle("Sending SMS to " + contactName)
                .setContentText("Tap to cancel")
                .setTicker("YouMissed: " + contactName + " called")
                .setSound(path)
                .setContentIntent(pendingIntent)
                .setUsesChronometer(true);


        final AsyncTask<Integer, Integer, Integer> sendSmsTask = new AsyncTask<Integer, Integer, Integer>() {
            String result = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.d("progress_notif", "created");
                mNotificationManager.notify(progresID, nBuilder.build());
            }

            @Override
            protected Integer doInBackground(Integer... params) {
                try {
                    // Sleeps according to set time to show the undeterminated progress
                    SharedPreferences sharedPreferences = context.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
                    int delayTime = sharedPreferences.getInt("delay_time", 10000);

                    Thread.sleep(delayTime);

                    // update the progress

                    for (int i = 0; i < (delayTime / 1000) + 1; i += 5) {
                        nBuilder.setContentTitle("Sending SMS to " + contactName)
                                .setContentText("Tap to cancel").setAutoCancel(false)
                                .setProgress(delayTime, i, false).setLargeIcon(bitmap)
                                .setSmallIcon(R.mipmap.ic_launcher);

                        // use the same id for update instead created another one
                        mNotificationManager.notify(progresID, nBuilder.build());
                        Thread.sleep(500);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }


            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                SharedPreferences sharedPreferences = context.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
                Boolean isSwitchChecked = sharedPreferences.getBoolean("sms_switch", true);
                if (isSwitchChecked) {
                    MainActivity ma = new MainActivity();
                    if (ma.isToSendSms()) {
                        if (contactName.equalsIgnoreCase("Unknown")) {
                            if (sharedPreferences.getBoolean("sms_unknown_callers", true)) {
                                sendSMS(progresID, message, context, number);
                            } else {
                                updateSmsStatus("Sending SMS to unknown callers disabled", nBuilder, progresID, number, message, context, false);
                            }
                        } else {
                            sendSMS(progresID, message, context, number);
                        }

                    }
                } else {
                    updateSmsStatus("Sending SMS feature disabled", nBuilder, progresID, number, message, context, false);
                }

            }
        };

        // Executes the progress task
        sendSmsTask.execute();
    }

    private void sendSMS(int progresID, String message, Context context, String number) {
        String SENT_SMS_FLAG = "sent";
        Log.d("sms_track_sending", "triggered");
        SmsManager smsManager = SmsManager.getDefault();
        Intent intent = new Intent(SENT_SMS_FLAG);
        intent.putExtra("progressID", progresID);
        intent.putExtra("phoneNumber", number);
        intent.putExtra("message", message);
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0,
                intent, 0);
        context.getApplicationContext().registerReceiver(
                new MessageSentListener(),
                new IntentFilter(SENT_SMS_FLAG));
        smsManager.sendTextMessage(number, null,
                message, sentIntent, null);
    }

    private void updateSmsStatus(String s, NotificationCompat.Builder nBuilder, int progresID, String number, String message, Context context, Boolean smsSent) {
        String contactName = new CallUserIdentifier().getUserNameFromContacts(context, number);
        resultSmsTask = s;
        Log.d("sms_track_del2", resultSmsTask);
        this.smsSent = smsSent;
        Intent intent = new Intent(context, MainActivity.class);
        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context.getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);
        //update notification
        nBuilder.setContentText(resultSmsTask)
                .setContentTitle("YouMissed: " + contactName + " called")
                .setTicker(resultSmsTask)
                .setLargeIcon(bitmap).setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);

        mNotificationManager.notify(progresID, nBuilder.build());

        MainActivity mainActivity = new MainActivity();
        mainActivity.saveMissedCallToRealm(number, message, context, smsSent);
    }
}
