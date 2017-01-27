package com.youmissed.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.youmissed.R;
import com.youmissed.adapters.MissedCallsAdapter;
import com.youmissed.app.RealmController;
import com.youmissed.utils.Toasty;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 */
public class MissedCallsFragment extends Fragment {
    @BindView(R.id.empty_view_layout)
    RelativeLayout emptyMissedCallsLayout;
    @BindView(R.id.fragment_title)
    TextView pageTitleText;
    @BindView(R.id.today)
    TextView todayTitleText;
    Realm realm;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.smsSwitch)
    SwitchCompat smsSwitch;
    @BindView(R.id.overflow_icon)
    ImageView overFlowIcon;
    MissedCallsAdapter missedCallsAdapter;

    public MissedCallsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_missed_calls, container, false);
        ButterKnife.bind(this, v);
        pageTitleText.setText(R.string.missed_calls);
        //get realm instance
        realm = RealmController.with(this).getRealm();
        setUpMissedCallsView();
        // Inflate the layout for this fragment
        return v;
    }

    private void setUpMissedCallsView() {

        smsSwitch.setVisibility(View.GONE);
        overFlowIcon.setVisibility(View.VISIBLE);
        setUpAdapter();
    }

    private void setUpAdapter() {
        if (RealmController.with(this).getMissedCalls().size() > 0) {
            //set missed call list
            emptyMissedCallsLayout.setVisibility(View.INVISIBLE);
            todayTitleText.setVisibility(View.VISIBLE);
            missedCallsAdapter = new MissedCallsAdapter(getActivity(), RealmController.with(this).getDistinctMissedCalls());
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(missedCallsAdapter);

        } else {
            //set empty view if there are no missed calls

            emptyMissedCallsLayout.setVisibility(View.VISIBLE);
            todayTitleText.setVisibility(View.INVISIBLE);
        }
        overFlowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopUp(view);
            }
        });
    }

    private void showPopUp(View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.blocked_list:
                        startActivity(new Intent(getActivity(), BlockedContacts.class));
                        return true;
                    case R.id.delete:
                        Toasty.success(getActivity(), getString(R.string.delete_missed_calls), Toast.LENGTH_LONG, true).show();
                        RealmController.with(getActivity()).deleteAllMissedCalls();
                        setUpAdapter();
                        return true;

                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }

    @OnClick(R.id.cancel)
    void closeThisFragment() {
        //close fragments
        //Toast.makeText(getActivity(), "Close MissedCall", Toast.LENGTH_SHORT).show();
        ((MainActivity) getActivity()).closeFragments();
    }


    @Override
    public void onResume() {
        super.onResume();
        setUpMissedCallsView();
    }
}
