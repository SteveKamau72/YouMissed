package com.youmissed.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.youmissed.utils.CallsModel;
import com.youmissed.R;
import com.youmissed.activities.MainActivity;
import com.youmissed.activities.MissedCallDetail;
import com.youmissed.app.RealmController;
import com.youmissed.utils.Dialogs;
import com.youmissed.utils.IntentUtils;
import com.youmissed.utils.Toasty;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.library.MDDialog;
import io.realm.RealmResults;

/**
 * Created by steve on 1/14/17.
 */
public class MissedCallsAdapter extends RecyclerView.Adapter<MissedCallsAdapter.ViewHolder> {
    private static final String TAG = MissedCallsAdapter.class.getSimpleName();
    SharedPreferences sharedPreferences;
    RealmResults<CallsModel> missedCalls;
    private Context mContext;
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    /**
     * Change {@link List} type according to your needs
     */
    public MissedCallsAdapter(Context context, RealmResults<CallsModel> missedCalls) {
        if (context == null) {
            throw new NullPointerException("context can not be NULL");
        }

        if (missedCalls == null) {
            throw new NullPointerException("missedCalls list can not be NULL");
        }

        this.mContext = context;
        this.missedCalls = missedCalls;
        sharedPreferences = mContext.getSharedPreferences("general_settings", Context.MODE_PRIVATE);

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
        final CallsModel callsModel = missedCalls.get(position);
        if (callsModel.getUserName().equals("Unknown")) {
            holder.title.setText(callsModel.getPhoneNumber());
        } else {
            holder.title.setText(callsModel.getUserName());
        }

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("KK:mm a");
        try {
            String timeInAmPm = outputFormat.format(inputFormat.parse(RealmController.getInstance()
                    .getLastTimeCallMissed(callsModel.getPhoneNumber())));
            //check if missed more than once
            holder.subtext.setText("(" + RealmController.getInstance()
                    .getNumberOfTimesCallIsMissed(callsModel.getPhoneNumber()) + ")  " + timeInAmPm);
            System.out.println(outputFormat.format(inputFormat.parse(callsModel.getMissedCallTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
                .round();
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(callsModel.getUserName().charAt(0)), mColorGenerator.getColor(callsModel.getUserName()));
        holder.avatarImageView.setImageDrawable(drawable);
        holder.callActionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Calling " + callsModel.getPhoneNumber() + "...", Toast.LENGTH_SHORT).show();
                //make call
                mContext.startActivity(IntentUtils.callPhone(callsModel.getPhoneNumber()));
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, MissedCallDetail.class);
                i.putExtra("phoneNumber", callsModel.getPhoneNumber());
                mContext.startActivity(i);
            }
        });
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showOptionsMenu(callsModel);
                return false;
            }
        });
        if (callsModel.getSmsSent()) {
            holder.smsStatusImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sms_sent));
        } else {
            holder.smsStatusImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_sms_failed));

        }
        if (RealmController.getInstance().isNumberBlocked(callsModel.getPhoneNumber())) {
            holder.blockedImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_blocked_error));
            holder.blockedImageView.setVisibility(View.VISIBLE);
        } else {
            holder.blockedImageView.setVisibility(View.INVISIBLE);
        }
    }

    private void showOptionsMenu(final CallsModel callsModel) {
        String blockString = "Block Number";
        if (RealmController.getInstance().isNumberBlocked(callsModel.getPhoneNumber())) {
            blockString = "UnBlock Contact";
        } else {
            blockString = "Block Contact";
        }
        String[] options = new String[]{"Call", "Message", blockString, "Copy Number"};
        new MDDialog.Builder(mContext)
                .setMessages(options)
                .setTitle("Options")
                .setShowTitle(true)
                .setShowButtons(false)
                .setOnItemClickListener(new MDDialog.OnItemClickListener() {
                    @Override
                    public void onItemClicked(int index) {
                        if (index == 0) {
                            mContext.startActivity(IntentUtils.callPhone(callsModel.getPhoneNumber()));
                        } else if (index == 1) {
                            mContext.startActivity(IntentUtils.sendSms(mContext, callsModel.getPhoneNumber(), ""));
                        } else if (index == 2) {
                            if (sharedPreferences.getBoolean("show_block_dialog", true)) {
                                Dialogs.showBlockDialog(mContext);
                            }
                            if (RealmController.getInstance().isNumberBlocked(callsModel.getPhoneNumber())) {
                                ((MainActivity) mContext).removeBlockedUserToRealm(callsModel, mContext);
                            } else {
                                //add to blocklist immediately
                                ((MainActivity) mContext).addBlockedUserToRealm(callsModel.getPhoneNumber(), mContext);


                            }

                        } else if (index == 3) {
                            Toasty.success(mContext, mContext.getString(R.string.copied), Toast.LENGTH_LONG, true).show();
                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("phone_number", callsModel.getPhoneNumber());
                            clipboard.setPrimaryClip(clip);
                        }

                    }
                })
                .setWidthMaxDp(600)
                .create()
                .show();
    }


    @Override
    public int getItemCount() {
        return missedCalls.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // include {@link View} components here
        @BindView(R.id.avatar)
        ImageView avatarImageView;
        @BindView(R.id.sms_status)
        ImageView smsStatusImageView;
        @BindView(R.id.call_action)
        ImageView callActionImageView;
        @BindView(R.id.blocked_icon)
        ImageView blockedImageView;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.subtext)
        TextView subtext;
        @BindView(R.id.layout_clicl)
        RelativeLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
} 