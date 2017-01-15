package com.youmissed;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SendSmsActivityDialog extends Activity {
    final Context context = this;
    protected CircleCountDownView countDownView;
    int progress;
    int endTime;
    CountDownTimer countDownTimer;
    Button cancelTimerBt;
    String phoneNumber, dateMissed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //String messageContent = extras.getString("msgContent");
            phoneNumber = extras.getString("sender");
            dateMissed = extras.getString("start_date");
            showDialogCustom();

        }
        //setContentView(R.layout.activity_send_sms_dialog);
    }

    private void showDialogCustom() {
        // custom dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.title);
        tvTitle.setText("Missed call from: ");
        TextView tvUsername = (TextView) dialogView.findViewById(R.id.user_name);
        tvUsername.setText("Steve Kamau");
        TextView tvPhone = (TextView) dialogView.findViewById(R.id.phone_number);
        tvPhone.setText(phoneNumber);


        countDownView = (CircleCountDownView) dialogView.findViewById(R.id.circle_count_down_view);

        cancelTimerBt = (Button) dialogView.findViewById(R.id.cancleTimer);
        startCountDown(dialogView);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    protected void startCountDown(final View view) {

        String timeInterval = "10";
        if (TextUtils.isEmpty(timeInterval)) {
            //Toast.makeText(this, "Please enter end time in Edit text.", Toast.LENGTH_SHORT).show();
        } else {
            // view.setVisibility(View.GONE); // hide button
            countDownView.setVisibility(View.VISIBLE); // show progress view
            cancelTimerBt.setVisibility(View.VISIBLE); // show cancel button

            progress = 1;
            endTime = Integer.parseInt(timeInterval); // up to finish time

            countDownTimer = new CountDownTimer(endTime * 1000 /*finishTime**/, 1000 /*interval**/) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countDownView.setProgress(progress, endTime);
                    progress = progress + 1;
                }

                @Override
                public void onFinish() {
                    countDownView.setProgress(progress, endTime);
                    view.setVisibility(View.VISIBLE);
                    cancelTimerBt.setVisibility(View.GONE);
                    new MySmsTask().execute();
                    finish();
                }
            };
            countDownTimer.start(); // start timer

            // hide softkeyboard
            View currentFocus = this.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public void stopCountDown(View view) {

        countDownView.setProgress(endTime, endTime);
        countDownTimer.cancel();
        cancelTimerBt.setVisibility(View.GONE);
        finish();
        //startTimerBt.setVisibility(View.VISIBLE);
    }

    private class MySmsTask extends AsyncTask<String, String, Integer> {
        int success;

        @Override
        protected Integer doInBackground(String... params) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            //SharedPreferences.Editor editor = sharedPref.edit();
            // editor.putString("sms_message");
            // editor.commit();
            String message = sharedPref.getString("sms_message", "Hey, I'm currently not near my phone, but i'll get back to you as soon as i can :)"
            );
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                success = 0;
                RealmController.with(SendSmsActivityDialog.this).updateSmsSentStatus(phoneNumber);
            } catch (Exception ex) {
                success = 1;
                ex.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Integer result) {
            if (success == 0) {
                Toast.makeText(getBaseContext(), "Sent sms to " + phoneNumber, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Failed to send sms to " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
        }


    }

}
