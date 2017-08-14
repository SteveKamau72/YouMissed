package com.youmissed.receivers;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.youmissed.app.CallUserIdentifier;
import com.youmissed.R;
import com.youmissed.activities.MainActivity;

/**
 * Created by steve on 1/20/17.
 */
public class MessageSentListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = this.getResultCode();
        boolean successfullySent = resultCode == Activity.RESULT_OK;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context);

        String phoneNumber = intent.getStringExtra("phoneNumber");
        String message = intent.getStringExtra("message");
        int progresID = intent.getIntExtra("progressID", 2989);


//        Log.d("sms_track_name", new CallUserIdentifier().getUserNameFromContacts(context, phoneNumber));
        String contactName = new CallUserIdentifier().getUserNameFromContacts(context, phoneNumber);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Intent myIntent = new Intent(context, MainActivity.class);
        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context.getApplicationContext());
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(myIntent);

        PendingIntent pendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_ONE_SHOT);
        if (successfullySent) {

            //update notification
            // building the notification
            //Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            nBuilder.setContentText("SMS successfully sent")
                    .setContentTitle("YouMissed: " + contactName + " called")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setTicker("YouMissed: " + contactName + " called");
            mNotificationManager.notify(progresID, nBuilder.build());
        } else {
            nBuilder.setContentText("Failed to send SMS")
                    .setContentTitle("YouMissed: " + contactName + " called")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setTicker("YouMissed: " + contactName + " called");

            mNotificationManager.notify(progresID, nBuilder.build());

        }
        MainActivity mainActivity = new MainActivity();
        mainActivity.saveMissedCallToRealm(phoneNumber, message, context, successfullySent);

        //That boolean up there indicates the status of the message
        context.getApplicationContext().unregisterReceiver(this);
        //Notice how I get the app context again here and unregister this broadcast
        //receiver to clear it from the system since it won't be used again

    }
}
