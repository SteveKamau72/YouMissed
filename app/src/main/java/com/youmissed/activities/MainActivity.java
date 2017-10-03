package com.youmissed.activities;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.nineoldandroids.animation.Animator;
import com.viewpagerindicator.CirclePageIndicator;
import com.youmissed.R;
import com.youmissed.adapters.MainPagerAdapter;
import com.youmissed.app.RealmController;
import com.youmissed.utils.CallsModel;
import com.youmissed.utils.CircularBarPager;
import com.youmissed.utils.IntentUtils;
import com.youmissed.utils.Toasty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    /**
     * The animation time in milliseconds that we take to display the steps taken
     */
    private static final int BAR_ANIMATION_TIME = 1000;
    static boolean ring = false;
    static boolean callReceived = false;
    Realm realm;
    SharedPreferences preferences;
    @BindView(R.id.settings)
    ImageView settingsImgageView;
    @BindView(R.id.home)
    ImageView homeImgageView;
    @BindView(R.id.missed_calls)
    ImageView callsImgageView;
    @BindView(R.id.launched_first_time)
    FrameLayout frameLayout;
    FragmentTransaction fragmentTransaction;
    Boolean toStopSendingSMS = false;
    private CircularBarPager mCircularBarPager;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        onNewIntent(getIntent());
        initViews();
        //get realm instance
        realm = RealmController.with(this).getRealm();
        sharedPreferences = getSharedPreferences("ACCOUNT", MODE_PRIVATE);
        //RealmController.with(this).updateSmsSentStatus("0772576503");
        Log.d("my_realm_data", String.valueOf(RealmController.with(this).getMissedCalls()));
        for (int i = 0; i < RealmController.with(this).getMissedCalls().size(); i++) {
            //delete old missed calls
            RealmController.with(this).deleteOldMissedCalls(getTodaysDate());
        }

        SharedPreferences prefs = getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        boolean previouslyStarted = prefs.getBoolean("previously_started", false);
        if (!previouslyStarted) {
            frameLayout.setVisibility(View.VISIBLE);
            showStartPage();

        }

        // Create the Realm instance
        // realm = Realm.getDefaultInstance();
       /* TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(new TeleListener(),
                PhoneStateListener.LISTEN_CALL_STATE);*/
        // saveMissedCallToRealm("0715611306");
   /*     Intent i = new Intent(getApplicationContext(), SendSmsActivityDialog.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);*/
        ratingDialog();
    }

    private void showStartPage() {
        Fragment fragment = new SplashFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.launched_first_time, fragment);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCircularBarPager.getCircularBar().animateProgress(0, 100, 1000);
        setUpPagerAdapter();

    }

    private void initViews() {
        //set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // toolbar.setTitle(R.string.app_name);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Typeface khandBold = Typeface.createFromAsset(getResources().getAssets(), "fonts/Balkeno.ttf");

        toolbarTitle.setTypeface(khandBold);
        //view pager set up
        mCircularBarPager = (CircularBarPager) findViewById(R.id.circularBarPager);
        setUpPagerAdapter();
    }

    private void setUpPagerAdapter() {
        View[] views = new View[2];
        views[0] = new MainView(this, "");
        views[1] = new SecondView(this, "");

        mCircularBarPager.setViewPagerAdapter(new MainPagerAdapter(this, views));

        ViewPager viewPager = mCircularBarPager.getViewPager();
        viewPager.setClipToPadding(true);

        CirclePageIndicator circlePageIndicator = mCircularBarPager.getCirclePageIndicator();
        circlePageIndicator.setFillColor(getResources().getColor(R.color.light_grey));
        circlePageIndicator.setPageColor(getResources().getColor(R.color.very_light_grey));
        circlePageIndicator.setStrokeColor(getResources().getColor(R.color.transparent));

        //Do stuff based on animation
        mCircularBarPager.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //TODO do stuff
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        //Do stuff based on when pages change
        circlePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCircularBarPager != null && mCircularBarPager.getCircularBar() != null) {
                    switch (position) {
                        case 0:
                            // mCircularBarPager.getCircularBar().setClockwiseReachedArcColor(getResources().getColor(R.color.circular_anticlockwise_color));
                            // mCircularBarPager.getCircularBar().setCircleFillColor(getResources().getColor(R.color.circular_background_color2));
                            mCircularBarPager.getCircularBar().animateProgress(0, 100, BAR_ANIMATION_TIME);
                            break;
                        case 1:
                            // mCircularBarPager.getCircularBar().setCircleFillColor(getResources().getColor(R.color.circular_background_color));
                            // mCircularBarPager.getCircularBar().setCounterClockwiseArcColor(getResources().getColor(R.color.circular_clockwise_color));
                            mCircularBarPager.getCircularBar().animateProgress(100, -100, BAR_ANIMATION_TIME);
                            break;
                        default:
                            // mCircularBarPager.getCircularBar().setCounterClockwiseArcColor(getResources().getColor(R.color.circular_anticlockwise_color));
                            // mCircularBarPager.getCircularBar().setCircleFillColor(getResources().getColor(R.color.circular_background_color2));
                            mCircularBarPager.getCircularBar().animateProgress(0, 100, BAR_ANIMATION_TIME);
                            break;
                    }
                }
            }
        });
    }

    public int getNumberOfMissedCalls() {
        //get realm instance
        return RealmController.with(this).getMissedCalls().size();
    }

    public int getNumberOfSuccessfullSmsSent() {
        //get realm instance
        return RealmController.with(this).queryedSentSms().size();
    }

    public void saveMissedCallToRealm(final String phoneNumber, String message, final Context ctx, final Boolean sentSms) {

        //get contact number
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = ctx.getContentResolver();
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                String contactName = "Unknown";
                if (cursor == null) {
                    return;
                }

                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }

                if (!cursor.isClosed()) {
                    cursor.close();
                }
                Log.d("contact_name", contactName);

                //show Syncing Progress
                final String finalContactName = contactName;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("sms_track_e", "copy to realm");
                        //get realm instance
                        try {
                            Realm realm = RealmController.with(MainActivity.this).getRealm();
                            ArrayList<CallsModel> callsModelArrayList = new ArrayList<>();
                            callsModelArrayList.clear();
                            CallsModel callsModel = new CallsModel();
                            callsModel.setId((int) (1 + System.currentTimeMillis()));
                            callsModel.setNumberTimesMissed("1");
                            callsModel.setPhoneNumber(phoneNumber);
                            callsModel.setUserName(finalContactName);
                            callsModel.setSmsSent(sentSms);
                            callsModel.setMissedCallTime(getTime());
                            callsModel.setDateTimeMissed(getTodaysDate());
                            callsModelArrayList.add(callsModel);
                            // Persist your data easily
                            realm.beginTransaction();
                            realm.copyToRealmOrUpdate(callsModelArrayList);
                            realm.commitTransaction();
                            Log.d("my_realm_data", String.valueOf(RealmController.with(MainActivity.this).getMissedCalls()));
                        } catch (Exception e) {

                        }
                    }
                });
            }
        }).start();

        //new MySmsTask(phoneNumber, message, ctx).execute();
    }

    String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void addBlockedUserToRealm(final String phoneNumber, final Context ctx) {
        Toasty.success(ctx, ctx.getString(R.string.blocked), Toast.LENGTH_LONG, true).show();
        //get contact number
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = ctx.getContentResolver();
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
                Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                String contactName = "Unknown";
                if (cursor == null) {
                    return;
                }

                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }

                if (!cursor.isClosed()) {
                    cursor.close();
                }
                Log.d("contact_name", contactName);

                //show Syncing Progress
                final String finalContactName = contactName;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("sms_track_e", "copy to realm");
                        Realm realm = RealmController.with(MainActivity.this).getRealmBlockTable();
                        ArrayList<CallsModel> callsModelArrayList = new ArrayList<>();
                        callsModelArrayList.clear();
                        CallsModel callsModel = new CallsModel();
                        callsModel.setId((int) (1 + System.currentTimeMillis()));
                        callsModel.setNumberTimesMissed("1");
                        callsModel.setPhoneNumber(phoneNumber);
                        callsModel.setUserName(finalContactName);
                        callsModel.setSmsSent(false);
                        callsModel.setMissedCallTime(getTime());
                        callsModel.setDateTimeMissed(getTodaysDate());
                        callsModelArrayList.add(callsModel);
                        // Persist your data easily
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(callsModelArrayList);
                        realm.commitTransaction();
                        Log.d("my_realm_data", String.valueOf(RealmController.with(MainActivity.this).getBlockedContacts()));
                    }
                });
            }
        }).start();
        //startMissedCallsFragment();

    }

    public void removeBlockedUserToRealm(CallsModel callsModel, Context mContext) {
        Toasty.info(mContext, mContext.getString(R.string.unblocked), Toast.LENGTH_LONG, true).show();
        RealmController.with(MainActivity.this).removeUserFromBlockedTable(callsModel.getPhoneNumber());
        startMissedCallsFragment();
    }

    public void closeFragments() {
        showHomeView();
    }

    @OnClick(R.id.home)
    void showHomeView() {
        // Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        setIconStatusForNavigationMenu(R.drawable.ic_action_home_selected, R.drawable.ic_action_calls_default, R.drawable.ic_action_settings_default);
        Fragment fragment = new HomeFragment();
        createFragments(fragment);
    }

    private void setIconStatusForNavigationMenu(int homeIconInt, int callsIconInt, int settingsIconInt) {
        homeImgageView.setImageDrawable(getResources().getDrawable(homeIconInt));
        callsImgageView.setImageDrawable(getResources().getDrawable(callsIconInt));
        settingsImgageView.setImageDrawable(getResources().getDrawable(settingsIconInt));
    }

    @OnClick(R.id.missed_calls)
    void startMissedCallsFragment() {
        //Toast.makeText(this, "Missed calls", Toast.LENGTH_SHORT).show();
        setIconStatusForNavigationMenu(R.drawable.ic_action_home_default, R.drawable.ic_action_calls_selected, R.drawable.ic_action_settings_default);
/*
        settingsImgageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_settings_default));
        callsImgageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_calls_selected));
        homeImgageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home_default));*/
        Fragment fragment = new MissedCallsFragment();
        createFragments(fragment);
    }

    @OnClick(R.id.settings)
    void startSettingsFragment() {
        // Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        setIconStatusForNavigationMenu(R.drawable.ic_action_home_default, R.drawable.ic_action_calls_default, R.drawable.ic_action_settings_selected);
/*
        settingsImgageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_settings_selected));
        callsImgageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_calls_default));
        homeImgageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home_default));*/
        Fragment fragment = new Settings();
        createFragments(fragment);
    }

    private void createFragments(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.container_body, fragment);
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void hideSplashView() {
        frameLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            startActivity(Intent.createChooser(IntentUtils.shareContent("Automatically send SMS replies to missed calls. Checkout the app: " + getString(R.string.message_add_on), "YouMissedApp for Android"), "Share..."));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.containsKey("toStopSendingSms")) {
                Log.d("toStopSendingSms_1", String.valueOf(extras.getBoolean("toStopSendingSms")));
                // extract the extra-data in the Notification
                String contact_name = extras.getString("contact_name");
                this.toStopSendingSMS = true;
                Toasty.success(getApplicationContext(), "Cancelled Sending sms to " + contact_name, Toast.LENGTH_LONG, true).show();
                //Toast.makeText(getApplicationContext(), "Cancelled Sending sms to " + contact_name, Toast.LENGTH_LONG).show();
            }
        }
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(2989);
    }

    public Boolean toStopSendingSMSMethod() {
        return toStopSendingSMS;
    }


    private void ratingDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(MainActivity.this)
                .icon(getResources().getDrawable(R.drawable.ic_app_icon))
                .title("We're awesome, yes? Rate us")
                .titleTextColor(R.color.black)
                .threshold(5)
                .session(5)
                .formTitle("Submit Feedback")
                .formHint("Tell us where we can improve")
                .formSubmitText("Submit")
                .formCancelText("Cancel")
                .ratingBarColor(R.color.accent)
                .positiveButtonBackgroundColor(R.drawable.button_selector_positive)
                .negativeButtonBackgroundColor(R.drawable.button_selector_negative)
                .onRatingChanged(new RatingDialog.RatingDialogListener() {
                    @Override
                    public void onRatingSelected(float rating, boolean thresholdCleared) {

                    }
                })
                .onRatingBarFormSumbit(new RatingDialog.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        startActivity(Intent.createChooser(IntentUtils.sendEmail("steveKamau72@gmail.com", "FeedBack on YouMissed App for Android", feedback), "Email us"));
                    }
                }).build();

        ratingDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (sharedPreferences.getBoolean("new_update", false)) {
            showUpdateDialog();
        }
    }

    private void showUpdateDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("New Update Available!");
        alertDialog.setMessage("Upgrade to get the latest version of YouMissed!");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "UPDATE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(Intent.createChooser(IntentUtils.openPlayStore(getApplicationContext(), false), ""));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("new_update", false);
                        editor.apply();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
