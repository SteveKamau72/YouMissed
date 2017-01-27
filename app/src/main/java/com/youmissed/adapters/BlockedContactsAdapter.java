package com.youmissed.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.youmissed.CallsModel;
import com.youmissed.R;
import com.youmissed.activities.BlockedContacts;
import com.youmissed.activities.MainActivity;
import com.youmissed.activities.MissedCallDetail;
import com.youmissed.app.IntentUtils;
import com.youmissed.utils.Toasty;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.library.MDDialog;
import io.realm.RealmResults;

/**
 * Created by steve on 1/25/17.
 */

public class BlockedContactsAdapter extends RecyclerView.Adapter<BlockedContactsAdapter.ViewHolder> {
    private static final String TAG = BlockedContactsAdapter.class.getSimpleName();
    SharedPreferences sharedPreferences;
    RealmResults<CallsModel> blockedContacts;
    private Context mContext;
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    /**
     * Change {@link List} type according to your needs
     */
    public BlockedContactsAdapter(Context context, RealmResults<CallsModel> blockedContacts) {
        if (context == null) {
            throw new NullPointerException("context can not be NULL");
        }

        if (blockedContacts == null) {
            throw new NullPointerException("blockedContacts list can not be NULL");
        }

        this.mContext = context;
        this.blockedContacts = blockedContacts;
        sharedPreferences = mContext.getSharedPreferences("general_settings", Context.MODE_PRIVATE);

    }


    /**
     * Change the null parameter to a layout resource {@code R.layout.example}
     */
    @Override
    public BlockedContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blocked_contacts_row, parent, false);

        return new BlockedContactsAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(BlockedContactsAdapter.ViewHolder holder, int position) {
        // include binding logic here
        final CallsModel callsModel = blockedContacts.get(position);
        if (callsModel.getUserName().equals("Unknown")) {
            holder.title.setText(callsModel.getPhoneNumber());
        } else {
            holder.title.setText(callsModel.getUserName());
        }

        holder.subtext.setText(callsModel.getPhoneNumber());
        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
                .round();
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(callsModel.getUserName().charAt(0)), mColorGenerator.getColor(callsModel.getUserName()));
        holder.avatarImageView.setImageDrawable(drawable);
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

    }

    private void showOptionsMenu(final CallsModel callsModel) {
        String blockString = "UnBlock Contact";
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
                            if (sharedPreferences.getBoolean("show_block_dialog", false)) {
                                showBlockDialog(callsModel);
                            } else {
                                //add to blocklist immediately
                                ((BlockedContacts) mContext).removeBlockedUserToRealm(callsModel, mContext);
                            }

                        } else if (index == 3) {
                            Toasty.success(mContext, mContext.getString(R.string.copied), Toast.LENGTH_SHORT, true).show();
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

    private void showBlockDialog(final CallsModel callsModel) {
        // custom dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.block_user_info, null);
        dialogBuilder.setView(dialogView);

        final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("show_block_dialog", b);
                editor.apply();
            }
        });
        dialogBuilder.setPositiveButton("Do it", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toasty.success(mContext, mContext.getString(R.string.blocked), Toast.LENGTH_SHORT, true).show();
                //add to blocklist immediately
                ((MainActivity) mContext).addBlockedUserToRealm(callsModel.getPhoneNumber(), mContext);
                dialog.cancel();
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

    @Override
    public int getItemCount() {
        return blockedContacts.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // include {@link View} components here
        @BindView(R.id.avatar)
        ImageView avatarImageView;
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
