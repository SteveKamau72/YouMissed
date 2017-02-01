package com.youmissed.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.youmissed.R;
import com.youmissed.activities.ContactList;
import com.youmissed.app.RealmController;
import com.youmissed.utils.Dialogs;
import com.youmissed.utils.ContactModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by steve on 1/29/17.
 */
public class AllContactsAdapter extends RecyclerView.Adapter<AllContactsAdapter.ViewHolder> implements Filterable {

    private static final String TAG = AllContactsAdapter.class.getSimpleName();
    SharedPreferences sharedPreferences;
    // declare the color generator and drawable builder
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private Context mContext;
    private List<ContactModel> mData;
    private List<ContactModel> filteredData;
    private ItemFilter mFilter = new ItemFilter();

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
        this.filteredData = data;
        sharedPreferences = mContext.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
    }


    /**
     * Change the null parameter to a layout resource {@code R.layout.example}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      /*  View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_contacts_row, parent, false);
        return new ViewHolder(view);

*/
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_contacts_row, parent, false);
        ViewHolder mViewHold = new ViewHolder(mView);
        return mViewHold;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // include binding logic here
        // include binding logic here
        final ContactModel contactModel = mData.get(position);
        holder.title.setText(contactModel.getUserName());
        holder.subtext.setText(contactModel.getPhoneNumber());
        // holder.avatarImageView.setTag(position);
        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder()
                .round();
        final TextDrawable drawable = mDrawableBuilder.build(String.valueOf(contactModel.getUserName().charAt(0)), mColorGenerator.getColor(contactModel.getUserName()));

        if (RealmController.getInstance().isNumberBlocked(contactModel.getPhoneNumber())) {

            holder.avatarImageView.getTag();
            holder.avatarImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_selected));
        } else {
            holder.avatarImageView.getTag();
            holder.avatarImageView.setImageDrawable(drawable);
        }


        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (RealmController.getInstance().isNumberBlocked(contactModel.getPhoneNumber())) {
                    //unblock number
                    holder.avatarImageView.getTag();
                    holder.avatarImageView.setImageDrawable(drawable);
                    ((ContactList) mContext).removeBlockedUserToRealm(contactModel.getPhoneNumber(), mContext);

                } else {
                    //block number
                    if (sharedPreferences.getBoolean("show_block_dialog", true)) {
                        Dialogs.showBlockDialog(mContext);
                    }
                    ((ContactList) mContext).addBlockedUserToRealm(contactModel.getPhoneNumber(), mContext);
                    holder.avatarImageView.getTag();
                    holder.avatarImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_selected));
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
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
            avatarImageView.setTag(this);
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                List<ContactModel> filterList = new ArrayList<>();
                for (int i = 0; i < filteredData.size(); i++)
                    if ((filteredData.get(i).getUserName().toUpperCase())
                            .contains(constraint.toString().toUpperCase()) || (filteredData.get(i).getUserName().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

// TODO: 9/17/16 add  the real image path
                        ContactModel m = new ContactModel(filteredData.get(i)
                                .getUserName(), filteredData.get(i)
                                .getPhoneNumber());

                        filterList.add(filteredData.get(i));
                    }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = filteredData.size();
                results.values = filteredData;

                //show no results were picked
                //(myActivityInterface).onSearchEmpty("No results found");
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // filteredData = (ArrayList<String>) results.values;
            //if(results)
            mData = (List<ContactModel>) results.values;
            if (mData.size() > 0) {
                // (myActivityInterface).onSearchEmpty("Results found");
                notifyDataSetChanged();
            } else {
                // (myActivityInterface).onSearchEmpty("No results found");
            }
            notifyDataSetChanged();
        }

    }
} 