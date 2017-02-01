package com.youmissed.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.youmissed.utils.CallsModel;
import com.youmissed.R;
import com.youmissed.adapters.AllContactsAdapter;
import com.youmissed.app.RealmController;
import com.youmissed.utils.ContactModel;
import com.youmissed.utils.Toasty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ContactList extends AppCompatActivity {
    List<ContactModel> contactList = new ArrayList<>();
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.search)
    EditText editTextSearchView;
    ProgressDialog pd;
    AllContactsAdapter allContactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        ButterKnife.bind(this);
        setUpViews();
        setContactList();

    }

    private void setUpViews() {
        //set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);

        editTextSearchView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text [" + s + "]");
                allContactsAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setContactList() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                pd = ProgressDialog.show(ContactList.this,
                        "Loading..", "You got alot of contacts buddy..", true, false);
            }// End of onPreExecute method

            @Override
            protected Void doInBackground(Void... params) {
                ContentResolver cr = getContentResolver(); //Activity/Application android.content.Context
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                        if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                            while (pCur.moveToNext()) {
                                ContactModel cm = new ContactModel();
                                String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                String name = pCur.getString(pCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                cm.setPhoneNumber(contactNumber);
                                cm.setUserName(name);
                                Log.d("conatcs_", name);
                                contactList.add(cm);
                                break;
                            }

                            pCur.close();
                        }

                    } while (cursor.moveToNext());
                }

                return null;
            }// End of doInBackground method

            @Override
            protected void onPostExecute(Void result) {
                setUpAdapter();
                pd.dismiss();

            }//End of onPostExecute method
        }.execute((Void[]) null);


    }

    private void setUpAdapter() {
        Collections.sort(contactList, new Comparator<ContactModel>() {
            @Override
            public int compare(ContactModel contactModel, ContactModel t1) {
                return contactModel.getUserName().compareTo(t1.getUserName());
            }
        });
        allContactsAdapter = new AllContactsAdapter(this, contactList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setItemViewCacheSize(contactList.size());
        recyclerView.setAdapter(allContactsAdapter);
    }

    public void removeBlockedUserToRealm(String phoneNumber, Context mContext) {
        Toasty.success(mContext, mContext.getString(R.string.unblocked), Toast.LENGTH_LONG, true).show();
        RealmController.with(ContactList.this).removeUserFromBlockedTable(phoneNumber);
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
                        Realm realm = RealmController.with(ContactList.this).getRealmBlockTable();
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
                        Log.d("my_realm_data", String.valueOf(RealmController.with(ContactList.this).getBlockedContacts()));
                    }
                });
            }
        }).start();
        //startMissedCallsFragment();

    }

    String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
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

        return super.onOptionsItemSelected(item);

    }
}
