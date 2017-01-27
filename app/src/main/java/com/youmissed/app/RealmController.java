package com.youmissed.app;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.youmissed.CallsModel;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by steve on 1/11/17.
 */
public class RealmController {

    private static RealmController instance;
    Realm realm, blockedRealmUsers;

    public RealmController(Application application) {
        // realm = Realm.getDefaultInstance();
        // Or you can add the migration code to the configuration. This will run the migration code without throwing
        // a RealmMigrationNeededException.
        RealmConfiguration config1 = new RealmConfiguration.Builder()
                .name("default1.realm")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build();
        RealmConfiguration config2 = new RealmConfiguration.Builder()
                .name("blocked-users.realm")
                .schemaVersion(3)
                .deleteRealmIfMigrationNeeded()
                .build();

        realm = Realm.getInstance(config1); // Automatically run migration if needed
        blockedRealmUsers = Realm.getInstance(config2); // Automatically run migration if needed
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

    public Realm getRealmBlockTable() {

        return blockedRealmUsers;
    }

    //Refresh the realm istance
    public void refresh() {

        realm.setAutoRefresh(true);
    }

    //clear all objects from MissedCallsModels
    public void clearAll() {

    }

    //find all objects in the MissedCalls.class
    public RealmResults<CallsModel> getMissedCalls() {
        RealmResults<CallsModel> sorted = realm.where(CallsModel.class).findAllSorted("missedCallTime", Sort.DESCENDING);
        return sorted;
    }

    //find all objects in the MissedCalls.class
    public RealmResults<CallsModel> getDistinctMissedCalls() {
  /*      RealmResults<CallsModel> allCalls = realm.where(CallsModel.class).findAllSorted("missedCallTime", Sort.DESCENDING);
//        RealmResults<CallsModel> distinctCalls = realm.where(CallsModel.class).distinct("phoneNumber");
        RealmResults<CallsModel> sorted = allCalls.distinct("phoneNumber");
        for (CallsModel distinctCall : allCalls) {
            // customer.getCountry() will return a few country for every iteration
            //customers.add(customer);
            Log.d("sms_track_missed", distinctCall.getMissedCallTime());

        }*/
        RealmResults<CallsModel> allCalls = realm.where(CallsModel.class).
                findAllSorted("missedCallTime", Sort.DESCENDING);
        RealmResults<CallsModel> sorted = allCalls.distinct("phoneNumber");
        for (int i = 0; i < sorted.size(); i++) {
//            Log.d("sms_track_missed", sorted.get(i).getMissedCallTime());

        }
        return allCalls.distinct("phoneNumber");
    }

    //find all objects in the MissedCalls.class
    public int getNumberOfTimesCallIsMissed(String phoneNumber) {
        RealmResults<CallsModel> distinctCalls = realm.where(CallsModel.class).equalTo("phoneNumber", phoneNumber).findAll().sort("missedCallTime", Sort.DESCENDING);
        return distinctCalls.size();
    }    //find all objects in the MissedCalls.class

    public String getLastTimeCallMissed(String phoneNumber) {
        RealmResults<CallsModel> allCalls = realm.where(CallsModel.class).equalTo("phoneNumber", phoneNumber).findAll().sort("missedCallTime", Sort.DESCENDING);

        String timeMissed = allCalls.first().getMissedCallTime();
        Log.d("sms_track_missed", timeMissed);
        return timeMissed;
    }

    //find all objects in the MissedCalls.class
    public RealmResults<CallsModel> getTimesCallMissed(String phoneNumber) {
        RealmResults<CallsModel> sorted = realm.where(CallsModel.class).equalTo("phoneNumber", phoneNumber).findAllSorted("missedCallTime", Sort.DESCENDING);
        return sorted;
    }

    //check if CallsModel.class is empty
    public boolean hasMissedCalls() {
        return realm.isEmpty();
    }

    //query example
    public RealmResults<CallsModel> queryedMissedCalls() {

        return realm.where(CallsModel.class)
                .contains("author", "Author 0")
                .or()
                .contains("title", "Realm")
                .findAll();

    }

    //delete old missed calls
    public void deleteOldMissedCalls(String dateToday) {
        realm.beginTransaction();
        realm.where(CallsModel.class).notEqualTo("dateTimeMissed", dateToday).findAll().deleteAllFromRealm();
        realm.commitTransaction();

    }

    public void deleteAllMissedCalls() {
        realm.beginTransaction();
        realm.where(CallsModel.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    //get sms sent
    public RealmResults<CallsModel> queryedSentSms() {

        return realm.where(CallsModel.class)
                .equalTo("smsSent", true)
                .findAll();

    }

    //update sms sent
    public void updateSmsSentStatus(String number) {
        CallsModel toEdit = realm.where(CallsModel.class)
                .equalTo("phoneNumber", number).findFirst();
        realm.beginTransaction();
        toEdit.setSmsSent(true);
        realm.commitTransaction();
    }

    public RealmResults<CallsModel> getBlockedContacts() {
        RealmResults<CallsModel> sorted = blockedRealmUsers.where(CallsModel.class).findAllSorted("missedCallTime", Sort.DESCENDING);
        return sorted;
    }

    public void removeUserFromBlockedTable(String phoneNumber) {
        blockedRealmUsers.beginTransaction();
        blockedRealmUsers.where(CallsModel.class).equalTo("phoneNumber", phoneNumber).findAll().deleteAllFromRealm();
        blockedRealmUsers.commitTransaction();
    }

    public boolean isNumberBlocked(String phoneNumber) {
        Boolean isNumberBlocked;
        RealmResults<CallsModel> allCalls = blockedRealmUsers.where(CallsModel.class).equalTo("phoneNumber", phoneNumber).findAll().sort("missedCallTime", Sort.DESCENDING);
        if (allCalls.size() > 0) {
            isNumberBlocked = true;
        } else {
            isNumberBlocked = false;
        }
        return isNumberBlocked;
    }
}
