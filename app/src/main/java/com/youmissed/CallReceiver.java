package com.youmissed;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by steve on 1/11/17.
 */

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //
        Toast.makeText(ctx, "Call ended: " + number, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //
        // TODO: 1/11/17 check if phone number exists in db, if so, stop sending sms 
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        //
        //Toast.makeText(ctx, "Missed call: " + number, Toast.LENGTH_LONG).show();
        SharedPreferences sharedPref = ctx.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        String message = sharedPref.getString("sms_message", "Hey, I'm currently not near my phone, but i'll get back to you as soon as i can :)"
        );
        NotificationHandler.getInstance(ctx).createProgressNotification(ctx, number, message, start);

    }

}