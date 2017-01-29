package com.youmissed.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.youmissed.R;
import com.youmissed.utils.ContactModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 1/29/17.
 */
public class AllContactsAdapter extends RecyclerView.Adapter<AllContactsAdapter.ViewHolder> {

    private static final String TAG = AllContactsAdapter.class.getSimpleName();
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private Context mContext;
    private List<ContactModel> mData;

    /**
     * Change {@link List} type according to your needs
     */
    public AllContactsAdapter(Context context, List<ContactModel> data) {
        if (context == null) {
            throw new NullPointerException("context can not be NULL");
        }

        if (data == null) {
            throw new NullPointerException("data list can not be NULL");
        }

        this.mContext = context;
        this.mData = data;
    }


    /**
     * Change the null parameter to a layout resource {@code R.layout.example}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_contacts_row, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // include binding logic here
        // include binding logic here
        final ContactModel contactModel = mData.get(position);
        holder.title.setText(contactModel.getUserName());
        holder.subtext.setText(contactModel.getPhoneNumber());

        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
                .round();
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(contactModel.getUserName().charAt(0)), mColorGenerator.getColor(contactModel.getUserName()));
        holder.avatarImageView.setImageDrawable(drawable);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // include {@link View} components here 
        @BindView(R.id.avatar)
        ImageView avatarImageView;
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