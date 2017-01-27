package com.youmissed.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.youmissed.R;

/**
 * Created by steve on 1/10/17.
 */

public class MainView extends LinearLayout {
    /**
     * TAG for logging
     */
    private static final String TAG = "HomeUserView";
    String numberOfMissedCalls;
    Context ctx;

    public MainView(Context context, String numberOfMissedCalls) {
        super(context);
        this.numberOfMissedCalls = numberOfMissedCalls;
        this.ctx = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout mainV = (LinearLayout) inflater.inflate(R.layout.view_user_info, this);

        TextView tvNoMissedCalls = (TextView) mainV.findViewById(R.id.value_info_textview);
        MainActivity mainActivity = new MainActivity();

        tvNoMissedCalls.setText("" + mainActivity.getNumberOfMissedCalls());

        //TODO init view
    }

}
