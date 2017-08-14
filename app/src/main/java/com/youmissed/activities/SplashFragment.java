package com.youmissed.activities;

import android.Manifest;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.youmissed.R;
import com.youmissed.utils.Toasty;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by steve on 1/15/17.
 */
public class SplashFragment extends Fragment {
    private static final String TAG = SplashFragment.class.getSimpleName();
    @BindView(R.id.get_started)
    Button btn;
    @BindView(R.id.app_title)
    TextView titleText;
    String permission;

    public SplashFragment() {
    }


    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: hit");
        super.onCreate(savedInstanceState);
    }

    /**
     * Change the null parameter in {@code inflater.inflate()}
     * to a layout resource {@code R.layout.example}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: hit");
        View rootView = inflater.inflate(R.layout.splash_fragment, container, false);
        ButterKnife.bind(this, rootView);
        Typeface khandBold = Typeface.createFromAsset(getResources().getAssets(), "fonts/Balkeno.ttf");
        titleText.setTypeface(khandBold);
        titleText.setText("YouMissed");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if we're running on Android 6.0 or higher
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Call some material design APIs here
                    setSMSPermissions();
                } else {
                    // Implement this feature without material design
                    ((MainActivity) getActivity()).hideSplashView();
                }

                //
            }
        });

        return rootView;
    }

    private void setSMSPermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.SEND_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setContactsPermissions();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toasty.error(getActivity(), "YouMissed app cannot work without the requested permissions", Toast.LENGTH_LONG, true).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();
    }

    private void setContactsPermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setCallPermissions();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toasty.error(getActivity(), "YouMissed app cannot work without the requested permissions", Toast.LENGTH_LONG, true).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();
    }

    private void setCallPermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        ((MainActivity) getActivity()).hideSplashView();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toasty.error(getActivity(), "YouMissed app cannot work without the requested permissions", Toast.LENGTH_LONG, true).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: hit");
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume: hit");
        super.onResume();
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause: hit");
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: hit");
        super.onDestroyView();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: hit");
        super.onDestroy();
    }
} 