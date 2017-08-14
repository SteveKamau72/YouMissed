package com.youmissed.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.youmissed.R;
import com.youmissed.adapters.MissedCallsTimeAdapter;
import com.youmissed.app.CallUserIdentifier;
import com.youmissed.app.RealmController;
import com.youmissed.utils.CallsModel;
import com.youmissed.utils.Dialogs;
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

public class MissedCallDetail extends AppCompatActivity {

    @BindView(R.id.title)
    TextView titleText;
    @BindView(R.id.phone)
    TextView tvPhone;
    @BindView(R.id.number_of_calls)
    TextView tvNumberOfCalls;
    @BindView(R.id.avatar)
    ImageView avatarImageView;
    Realm realm;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    MissedCallsTimeAdapter missedCallsTimeAdapter;
    String phoneNumber, userName;
    SharedPreferences sharedPreferences;
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missed_call_detail);
        ButterKnife.bind(this);
        Intent i = getIntent();
        phoneNumber = i.getStringExtra("phoneNumber");
        userName = new CallUserIdentifier().getUserNameFromContacts(getApplicationContext(), phoneNumber);
        sharedPreferences = getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        setUpViews();
    }

    private void setUpViews() {
        //set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        missedCallsTimeAdapter = new MissedCallsTimeAdapter(this, RealmController.with(this).getTimesCallMissed(phoneNumber));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(missedCallsTimeAdapter);
        titleText.setText(userName);

        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
                .round();
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(userName.charAt(0)), mColorGenerator.getColor(userName));
        avatarImageView.setImageDrawable(drawable);
        tvPhone.setText(phoneNumber);
        int missedTimes = RealmController.with(this).getNumberOfTimesCallIsMissed(phoneNumber);
        if (missedTimes > 1) {
            tvNumberOfCalls.setText("Missed " + missedTimes + " calls");
        } else {
            tvNumberOfCalls.setText("Missed " + missedTimes + " call");
        }
    }

    @OnClick(R.id.call)
    void onPhoneImageClick() {
        Toasty.info(getApplicationContext(), "Calling " + userName + "...", Toast.LENGTH_SHORT, true).show();
        //make call
        startActivity(IntentUtils.callPhone(phoneNumber));
    }

    @OnClick(R.id.message)
    void onMessageImageClick() {
        startActivity(IntentUtils.sendSms(getApplicationContext(), phoneNumber, ""));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        userName = new CallUserIdentifier().getUserNameFromContacts(getApplicationContext(), phoneNumber);
        setUpViews();

    }


    public void removeBlockedUserToRealm() {
        Toasty.success(getApplicationContext(), getString(R.string.unblocked), Toast.LENGTH_LONG, true).show();
        RealmController.with(MissedCallDetail.this).removeUserFromBlockedTable(phoneNumber);
    }

    String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void addBlockedUserToRealm() {
        Toasty.success(getApplicationContext(), getString(R.string.blocked), Toast.LENGTH_LONG, true).show();
        //get contact number
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = getApplicationContext().getContentResolver();
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
                        Realm realm = RealmController.with(MissedCallDetail.this).getRealmBlockTable();
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
                        Log.d("my_realm_data", String.valueOf(RealmController.with(MissedCallDetail.this).getBlockedContacts()));
                    }
                });
            }
        }).start();
        //startMissedCallsFragment();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.details_menu, menu);
        if (userName.equals("Unknown")) {
            // menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_add_new).setVisible(true);
        } else {
            // menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_add_new).setVisible(false);
        }
        if (RealmController.getInstance().isNumberBlocked(phoneNumber)) {
            menu.findItem(R.id.action_add_block_list).setVisible(false);
            menu.findItem(R.id.action_remove_block_list).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_block_list).setVisible(true);
            menu.findItem(R.id.action_remove_block_list).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
       /* //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent intent = new Intent(Intent.ACTION_INSERT,
                    ContactsContract.Contacts.CONTENT_URI);
            startActivity(intent);
            return true;
        }*/

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_new) {
            Intent intent = new Intent(Intent.ACTION_INSERT,
                    ContactsContract.Contacts.CONTENT_URI);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
            startActivity(intent);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_block_list) {
            invalidateOptionsMenu();
            //Toasty.success(getApplicationContext(), getString(R.string.block_user), Toast.LENGTH_SHORT, true).show();
            if (sharedPreferences.getBoolean("show_block_dialog", true)) {
                Dialogs.showBlockDialog(getApplicationContext());
            }
            addBlockedUserToRealm();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_remove_block_list) {
            invalidateOptionsMenu();
            removeBlockedUserToRealm();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_copy) {
            Toasty.success(getApplicationContext(), getString(R.string.copied), Toast.LENGTH_SHORT, true).show();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("phone_number", phoneNumber);
            clipboard.setPrimaryClip(clip);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (RealmController.getInstance().isNumberBlocked(phoneNumber)) {
            menu.findItem(R.id.action_add_block_list).setVisible(false);
            menu.findItem(R.id.action_remove_block_list).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_block_list).setVisible(true);
            menu.findItem(R.id.action_remove_block_list).setVisible(false);
        }
        return true;
    }
}
