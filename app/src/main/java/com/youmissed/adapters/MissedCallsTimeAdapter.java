package com.youmissed.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.youmissed.CallsModel;
import com.youmissed.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by steve on 1/20/17.
 */

public class MissedCallsTimeAdapter extends RecyclerView.Adapter<MissedCallsTimeAdapter.ViewHolder> {

    private static final String TAG = MissedCallsTimeAdapter.class.getSimpleName();
    RealmResults<CallsModel> missedCalls;
    private Context mContext;
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    /**
     * Change {@link List} type according to your needs
     */
    public MissedCallsTimeAdapter(Context context, RealmResults<CallsModel> missedCalls) {
        if (context == null) {
            throw new NullPointerException("context can not be NULL");
        }

        if (missedCalls == null) {
            throw new NullPointerException("missedCalls list can not be NULL");
        }

        this.mContext = context;
        this.missedCalls = missedCalls;
    }


    /**
     * Change the null parameter to a layout resource {@code R.layout.example}
     */
    @Override
    public MissedCallsTimeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.missed_calls_time_row, parent, false);

        return new MissedCallsTimeAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MissedCallsTimeAdapter.ViewHolder holder, int position) {
        // include binding logic here
        final CallsModel callsModel = missedCalls.get(position);

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a");
        try {
            String timeInAmPm = outputFormat.format(inputFormat.parse(callsModel.getMissedCallTime()));
            //check if missed more than once
            holder.time.setText(timeInAmPm);
            System.out.println(outputFormat.format(inputFormat.parse(callsModel.getMissedCallTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (callsModel.getSmsSent()) {
            holder.sms_status.setText("SMS sent successfully");
            holder.sms_status.setTextColor(mContext.getResources().getColor(R.color.green));
        } else {
            holder.sms_status.setText("Failed to send SMS");
            holder.sms_status.setTextColor(mContext.getResources().getColor(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return missedCalls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // include {@link View} components here
        @BindView(R.id.subtext)
        TextView time;
        @BindView(R.id.sms_status)
        TextView sms_status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
} 