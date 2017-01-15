package com.youmissed;

/**
 * Created by steve on 1/10/17.
 */

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MainPagerAdapter extends PagerAdapter {

    private Context mContext;
    private View[] mViews;

    public MainPagerAdapter(Context context, View... views) {
        this.mContext = context;
        this.mViews = views;
    }

    @Override
    public int getCount() {
        return mViews.length;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View currentView = mViews[position];
        ((ViewPager) collection).addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        ((ViewPager) collection).removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
