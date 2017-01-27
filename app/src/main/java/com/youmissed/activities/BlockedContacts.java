package com.youmissed.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.youmissed.CallsModel;
import com.youmissed.R;
import com.youmissed.adapters.BlockedContactsAdapter;
import com.youmissed.app.RealmController;
import com.youmissed.utils.Toasty;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BlockedContacts extends AppCompatActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view_layout)
    RelativeLayout emptyMissedCallsLayout;
    BlockedContactsAdapter blockedContactsAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contacts);
        ButterKnife.bind(this);
        setUpViews();
        sharedPreferences = getSharedPreferences("general_settings", Context.MODE_PRIVATE);
    }

    public void setUpViews() {
        //set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blocked Contacts");

        if (RealmController.with(this).getBlockedContacts().size() > 0) {
            blockedContactsAdapter = new BlockedContactsAdapter(this, RealmController.with(this).getBlockedContacts());
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(blockedContactsAdapter);
        } else {
            //set empty view if there are no missed calls
            emptyMissedCallsLayout.setVisibility(View.VISIBLE);
        }
    }

    public void removeBlockedUserToRealm(CallsModel callsModel, Context mContext) {
        Toasty.success(mContext, mContext.getString(R.string.unblocked), Toast.LENGTH_LONG, true).show();
        RealmController.with(BlockedContacts.this).removeUserFromBlockedTable(callsModel.getPhoneNumber());
        setUpViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.blocked_menu, menu);
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
        if (id == R.id.action_info) {
            showBlockDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void showBlockDialog() {
        // custom dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.block_user_info, null);
        dialogBuilder.setView(dialogView);

        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.checkbox);
        if (sharedPreferences.getBoolean("show_block_dialog", false)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("show_block_dialog", b);
                editor.apply();
            }
        });
        dialogBuilder.setNeutralButton("Got it", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }
}
