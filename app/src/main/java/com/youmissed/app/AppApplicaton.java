package com.youmissed.app;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by steve on 1/10/17.
 */

public class AppApplicaton extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
       // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this);

    }
}