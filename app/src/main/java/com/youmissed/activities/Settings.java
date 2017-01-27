package com.youmissed.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.youmissed.R;
import com.youmissed.app.IntentUtils;
import com.youmissed.utils.Toasty;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbs.android.library.MDDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class Settings extends Fragment {
    @BindView(R.id.fragment_title)
    TextView pageTitleText;
    @BindView(R.id.custom_message)
    TextView tvMessage;
    @BindView(R.id.delay_time)
    TextView tvDelayTime;
    @BindView(R.id.enableDisableFeature)
    TextView enableDisableFeatureText;
    EditText edMessage;
    SharedPreferences sharedPref;
    @BindView(R.id.smsSwitch)
    SwitchCompat smsSwitch;
    @BindView(R.id.unknown_callers_switch)
    SwitchCompat unknownCallersSwitch;
    @BindView(R.id.unknown_callers_layout)
    RelativeLayout unknownCallersLayout;
    SharedPreferences.Editor editor;

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        ButterKnife.bind(this, v);
        pageTitleText.setText(R.string.settings);
        sharedPref = getActivity().getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        tvMessage.setText(sharedPref.getString("sms_message", "Hey, I'm currently not near my phone, but i'll get back to you as soon as i can :)"
        ));
        //tvDelayTime.setText("Set to " + sharedPref.getInt("delay_time", 10) + " secs");
        tvDelayTime.setText("Set to " + sharedPref.getString("delay_time_to_display", "10 secs"));
        editor = sharedPref.edit();
        Boolean isSwitchChecked = sharedPref.getBoolean("sms_switch", true);
        if (isSwitchChecked) {
            enableDisableFeatureText.setVisibility(View.GONE);
        } else {
            enableDisableFeatureText.setVisibility(View.VISIBLE);
        }

        smsSwitch.setChecked(isSwitchChecked);
        smsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    enableDisableFeatureText.setVisibility(View.GONE);
                } else {
                    enableDisableFeatureText.setVisibility(View.VISIBLE);
                }
                editor.putBoolean("sms_switch", b);
                editor.apply();
            }
        });
        setStateForUnknownCallers(sharedPref.getBoolean("sms_unknown_callers", true));
        unknownCallersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setStateForUnknownCallers(b);
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    @OnClick(R.id.custom_message)
    void showDialog() {
        showEditMessageDialog();
    }

    private void showEditMessageDialog() {
        // custom dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_message_dialog, null);
        dialogBuilder.setView(dialogView);

        edMessage = (EditText) dialogView.findViewById(R.id.ed_message);
        edMessage.setText(sharedPref.getString("sms_message", "Hey, I'm currently not near my phone, but i'll get back to you as soon as i can :)"
        ));
        edMessage.setSelection(edMessage.getText().length());

        dialogBuilder.setTitle("Edit Message");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String message = edMessage.getText().toString();
                //do something with edt.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toasty.error(getActivity(), getString(R.string.empty), Toast.LENGTH_SHORT, true).show();
                } else {
                    editor.putString("sms_message", message);
                    editor.apply();
                    tvMessage.setText(message);
                    Toasty.success(getActivity(), getString(R.string.message_saved), Toast.LENGTH_SHORT, true).show();
                }

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @OnClick(R.id.cancel)
    void closeThisFragment() {
        //close fragments
        //Toast.makeText(getActivity(), "Close MissedCall", Toast.LENGTH_SHORT).show();
        ((MainActivity) getActivity()).closeFragments();
    }

    @OnClick(R.id.feedback)
    void sendFeedBack() {
        //Toast.makeText(getActivity(), "Close MissedCall", Toast.LENGTH_SHORT).show();
        startActivity(Intent.createChooser(IntentUtils.sendEmail("steveKamau72@gmail.com", "FeedBack on YouMissed App for Android", ""), "Send via email"));
    }

    @OnClick(R.id.contact)
    void contactDeveloper() {
        //Toast.makeText(getActivity(), "Close MissedCall", Toast.LENGTH_SHORT).show();
        startActivity(Intent.createChooser(IntentUtils.sendEmail("steveKamau72@gmail.com", "Contact Developer: REF: YouMissed App", ""), "Send via email"));

    }

    @OnClick(R.id.about)
    void about() {
        new MDDialog.Builder(getActivity())
                .setContentView(R.layout.about_layout)
                .setTitle("About")
                .setShowTitle(true)
                .setShowButtons(false)
                .setWidthMaxDp(300)
                .create()
                .show();
    }

    @OnClick(R.id.delayTime)
    void startDelayTimerDialog() {
        final String timeToDelay[] = new String[]{"No delay", "10 secs", "30 secs", "1 min", "5 mins", "10 mins"};
        new MDDialog.Builder(getActivity())
                .setMessages(timeToDelay)
                .setTitle("Select delay time")
                .setShowTitle(true)
                .setShowButtons(false)
                .setOnItemClickListener(new MDDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(int index) {
                        int selectedTime = 0;
                        if (index == 0) {
                            selectedTime = 0;
                        } else if (index == 1) {
                            selectedTime = 10;
                            // filter_amount
                        } else if (index == 2) {
                            selectedTime = 30;
                        } else if (index == 3) {
                            selectedTime = 60;
                        } else if (index == 4) {
                            selectedTime = 300;
                        } else if (index == 4) {
                            selectedTime = 600;
                        }

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt("delay_time", selectedTime * 1000);
                        editor.putString("delay_time_to_display", Arrays.asList(timeToDelay).get(index));
                        editor.apply();
                        tvDelayTime.setText("Set to " + Arrays.asList(timeToDelay).get(index));
                        //tvDelayTime.setText(timeToDelay.)
                    }
                })
                .setWidthMaxDp(300)
                .create()
                .show();
    }

    @OnClick(R.id.unknown_callers_layout)
    void onUnknownCallersViewClicked() {
        if (unknownCallersSwitch.isChecked()) {
            setStateForUnknownCallers(false);
        } else {
            unknownCallersSwitch.setChecked(true);
        }
    }

    public void setStateForUnknownCallers(Boolean state) {
        unknownCallersSwitch.setChecked(state);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("sms_unknown_callers", state);
        editor.apply();
    }
}
