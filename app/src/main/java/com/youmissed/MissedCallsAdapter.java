package com.youmissed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by steve on 1/14/17.
 */
public class MissedCallsAdapter extends RecyclerView.Adapter<MissedCallsAdapter.ViewHolder> {

    private static final String TAG = MissedCallsAdapter.class.getSimpleName();
    RealmResults<MissedCallsModel> missedCalls;
    private Context mContext;
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    /**
     * Change {@link List} type according to your needs
     */
    public MissedCallsAdapter(Context context, RealmResults<MissedCallsModel> missedCalls) {
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.missed_calls_row, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // include binding logic here
        final MissedCallsModel missedCallsModel = missedCalls.get(position);
        if (missedCallsModel.getUserName().equals("Unknown")) {
            holder.title.setText(missedCallsModel.getPhoneNumber());
        } else {
            holder.title.setText(missedCallsModel.getUserName());
        }

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a");
        try {
            String timeInAmPm = outputFormat.format(inputFormat.parse(missedCallsModel.getMissedCallTime()));
            //check if missed more than once
            holder.subtext.setText("(" + missedCallsModel.getNumberTimesMissed() + ")  " + timeInAmPm);
            System.out.println(outputFormat.format(inputFormat.parse(missedCallsModel.getMissedCallTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
                .round();
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(missedCallsModel.getUserName().charAt(0)), mColorGenerator.getColor(missedCallsModel.getUserName()));
        holder.avatarImageView.setImageDrawable(drawable);
        holder.callActionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Calling " + missedCallsModel.getPhoneNumber() + "...", Toast.LENGTH_SHORT).show();
                //make call
                mContext.startActivity(IntentUtils.callPhone(missedCallsModel.getPhoneNumber()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return missedCalls.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // include {@link View} components here
        @BindView(R.id.avatar)
        ImageView avatarImageView;
        @BindView(R.id.call_action)
        ImageView callActionImageView;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.subtext)
        TextView subtext;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
} 