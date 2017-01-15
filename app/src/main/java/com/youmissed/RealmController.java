package com.youmissed;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by steve on 1/11/17.
 */
public class RealmController {

    private static RealmController instance;
    private final Realm realm;

    public RealmController(Application application) {
        // realm = Realm.getDefaultInstance();
        // Or you can add the migration code to the configuration. This will run the migration code without throwing
        // a RealmMigrationNeededException.
        RealmConfiguration config1 = new RealmConfiguration.Builder()
                .name("default1.realm")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build();

        realm = Realm.getInstance(config1); // Automatically run migration if needed
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {

        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {

        return instance;
    }

    public Realm getRealm() {

        return realm;
    }

    //Refresh the realm istance
    public void refresh() {

        realm.setAutoRefresh(true);
    }

    //clear all objects from MissedCallsModels
    public void clearAll() {

    }

    //find all objects in the Book.class
    public RealmResults<MissedCallsModel> getMissedCalls() {

        return realm.where(MissedCallsModel.class).findAll();
    }


    //check if MissedCallsModel.class is empty
    public boolean hasMissedCalls() {
        return realm.isEmpty();
    }

    //query example
    public RealmResults<MissedCallsModel> queryedMissedCalls() {

        return realm.where(MissedCallsModel.class)
                .contains("author", "Author 0")
                .or()
                .contains("title", "Realm")
                .findAll();

    }

    //delete old missed calls
    public void deleteOldMissedCalls(String dateToday) {
        realm.beginTransaction();
        realm.where(MissedCallsModel.class).notEqualTo("dateTimeMissed", dateToday).findAll().deleteAllFromRealm();
        realm.commitTransaction();

    }

    //get sms sent
    public RealmResults<MissedCallsModel> queryedSentSms() {

        return realm.where(MissedCallsModel.class)
                .equalTo("smsSent", true)
                .findAll();

    }

    //update sms sent
    public void updateSmsSentStatus(String number) {
        MissedCallsModel toEdit = realm.where(MissedCallsModel.class)
                .equalTo("phoneNumber", number).findFirst();
        realm.beginTransaction();
        toEdit.setSmsSent(true);
        realm.commitTransaction();
    }
}
