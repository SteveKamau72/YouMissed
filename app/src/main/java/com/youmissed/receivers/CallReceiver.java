package com.youmissed.receivers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.youmissed.R;
import com.youmissed.utils.Toasty;

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
        Toasty.info(ctx, "Call ended: " + number, Toast.LENGTH_SHORT, true).show();

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
        String message = sharedPref.getString("sms_message",
                ctx.getResources().getString(R.string.default_sms_message)
        );

        Log.d("sms_track_e", "On missed call");
        NotificationHandler.getInstance(ctx).createProgressNotification(ctx, number, message, start);
        Log.d("contact_number", number);
    }

}