package com.youmissed;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by steve on 1/11/17.
 */

public class SecondView extends LinearLayout {
    /**
     * TAG for logging
     */
    private static final String TAG = "HomeUserView";
    String numberOfMissedCalls;
    Context ctx;

    public SecondView(Context context, String numberOfMissedCalls) {
        super(context);
        this.numberOfMissedCalls = numberOfMissedCalls;
        this.ctx = context;
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout mainV = (LinearLayout) inflater.inflate(R.layout.view_second_user_info, this);

        TextView tvNoMissedCalls = (TextView) mainV.findViewById(R.id.value_info_textview);

        MainActivity mainActivity = new MainActivity();

        tvNoMissedCalls.setText(mainActivity.getNumberOfSuccessfullSmsSent() + "/" + mainActivity.getNumberOfMissedCalls());

        //TODO init view
    }

}
