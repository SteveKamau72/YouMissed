package com.youmissed.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.youmissed.R;
import com.youmissed.adapters.BlockedContactsAdapter;
import com.youmissed.app.RealmController;
import com.youmissed.utils.CallsModel;
import com.youmissed.utils.Toasty;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlockedContacts extends AppCompatActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view_layout)
    RelativeLayout emptyMissedCallsLayout;
    BlockedContactsAdapter blockedContactsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contacts);
        ButterKnife.bind(this);
        setUpViews();
    }

    public void setUpViews() {
        //set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blocked Contacts");

        setUpAdapter();
    }

    private void setUpAdapter() {
        if (RealmController.with(this).getBlockedContacts().size() > 0) {
            blockedContactsAdapter = new BlockedContactsAdapter(this, RealmController.with(this).getBlockedContacts());
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(blockedContactsAdapter);
            emptyMissedCallsLayout.setVisibility(View.INVISIBLE);
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
    public void onResume() {
        super.onResume();
        setUpAdapter();
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
            //Toast.makeText(getApplicationContext(),"TIS",Toast.LENGTH_SHORT).show();
            // Dialogs.showBlockDialog(getApplicationContext());
            showBlockDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public void showBlockDialog() {
        final SharedPreferences sharedPreferences = getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        // custom dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.block_user_info, null);
        dialogBuilder.setView(dialogView);

        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.checkbox);
        if (sharedPreferences.getBoolean("show_block_dialog", false)) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }

        dialogBuilder.setPositiveButton("Aye aye", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Boolean checked;
                if (checkBox.isChecked()) {
                    checked = false;
                } else {
                    checked = true;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("show_block_dialog", checked);
                editor.apply();
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }

    @OnClick(R.id.fab)
    void onFabClicked() {
        Intent i = new Intent(getApplicationContext(), ContactList.class);
        startActivity(i);
    }

}
