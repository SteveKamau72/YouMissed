package com.youmissed;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Random;

/**
 * Created by steve on 1/14/17.
 */

public class NotificationHandler {
    // Notification handler singleton
    private static NotificationHandler nHandler;
    private static NotificationManager mNotificationManager;
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
        final int progresID = new Random().nextInt(1000);

        // building the notification
        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Sending SMS to Missed call...")
                .setContentText("Tap to cancel")
                .setTicker("YouMissed: Missed call")
                .setSound(path)
                .setUsesChronometer(true);


        AsyncTask<Integer, Integer, Integer> sendSmsTask = new AsyncTask<Integer, Integer, Integer>() {
            String result = "";

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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
                        nBuilder
                                .setContentTitle("Sending SMS to missed call...")
                                .setContentText("Tap to cancel")
                                .setProgress(delayTime, i, false)
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
                    try {

                        String SENT = "sent";
                        String DELIVERED = "delivered";

                        Intent sentIntent = new Intent(SENT);
                    /*Create Pending Intents*/
                        PendingIntent sentPI = PendingIntent.getBroadcast(
                                context, 0, sentIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        Intent deliveryIntent = new Intent(DELIVERED);
                        PendingIntent deliverPI = PendingIntent.getBroadcast(
                                context, 0, deliveryIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                    /* Register for SMS send action */
                        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {

                            @Override
                            public void onReceive(Context context, Intent intent) {


                                switch (getResultCode()) {

                                    case Activity.RESULT_OK:
                                        result = "SMS successfully sent";
                                        smsSent = true;
                                        break;
                                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                        result = "Failed to send SMS";
                                        smsSent = false;
                                        break;
                                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                                        result = "Failed to send SMS: Radio off";
                                        smsSent = false;
                                        break;
                                    case SmsManager.RESULT_ERROR_NULL_PDU:
                                        result = "Failed to send SMS: No PDU defined";
                                        smsSent = false;
                                        break;
                                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                                        result = "Failed to send SMS: No service";
                                        smsSent = false;
                                        break;
                                }
                                updateSmsStatus(result, nBuilder, progresID, number, message, context, smsSent);
                                Toast.makeText(context, result,
                                        Toast.LENGTH_LONG).show();

                            }

                        }, new IntentFilter(SENT));
                    /* Register for Delivery event */
                        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {

                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Toast.makeText(context, "SMS successfully delivered",
                                        Toast.LENGTH_LONG).show();
                                Log.d("sms_track_del", "delivered");
                                updateSmsStatus("SMS successfully delivered", nBuilder, progresID, number, message, context, true);
                            }

                        }, new IntentFilter(DELIVERED));

                    /*Send SMS*/
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(number, null, message, sentPI,
                                deliverPI);
                    } catch (Exception ex) {
                        updateSmsStatus(ex.getMessage(), nBuilder, progresID, number, message, context, false);
                        Log.d("sms_track_e", ex.getMessage());
                        Toast.makeText(context,
                                ex.getMessage(), Toast.LENGTH_LONG)
                                .show();
                        ex.printStackTrace();
                    }
                } else {
                    updateSmsStatus("Sending SMS feature disabled", nBuilder, progresID, number, message, context, false);
                }

            }
        };

        // Executes the progress task
        sendSmsTask.execute();
    }

    private void updateSmsStatus(String s, NotificationCompat.Builder nBuilder, int progresID, String number, String message, Context context, Boolean smsSent) {
        resultSmsTask = s;
        Log.d("sms_track_del2", resultSmsTask);
        this.smsSent = smsSent;
        //update notification
        nBuilder.setContentText(resultSmsTask)
                .setContentTitle("YouMissed: Missed call")
                .setTicker(resultSmsTask)
                .setSmallIcon(R.mipmap.ic_launcher)
                /*.setProgress(0, 0, false)
                .setUsesChronometer(false)*/;

        mNotificationManager.notify(progresID, nBuilder.build());

        MainActivity mainActivity = new MainActivity();
        mainActivity.saveMissedCallToRealm(number, message, context, smsSent);
    }
}
