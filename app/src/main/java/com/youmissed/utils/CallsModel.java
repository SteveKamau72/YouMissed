package com.youmissed.utils;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by steve on 1/10/17.
 */

public class CallsModel extends RealmObject {
    @PrimaryKey
    private int id;

    private String userName;
    @Index
    private String phoneNumber;
    private String dateTimeMissed;
    private String numberTimesMissed;
    private Boolean smsSent;
    private String missedCallTime;

    // Standard getters & setters generated by your IDE…
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateTimeMissed() {
        return dateTimeMissed;
    }

    public void setDateTimeMissed(String dateTimeMissed) {
        this.dateTimeMissed = dateTimeMissed;
    }

    public String getNumberTimesMissed() {
        return numberTimesMissed;
    }

    public void setNumberTimesMissed(String numberTimesMissed) {
        this.numberTimesMissed = numberTimesMissed;
    }

    public Boolean getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(Boolean smsSent) {
        this.smsSent = smsSent;
    }

    public String getMissedCallTime() {
        return missedCallTime;
    }

    public void setMissedCallTime(String missedCallTime) {
        this.missedCallTime = missedCallTime;
    }
}
