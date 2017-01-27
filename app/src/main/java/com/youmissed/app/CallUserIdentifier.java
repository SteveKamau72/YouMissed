package com.youmissed.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by steve on 1/20/17.
 */

public class CallUserIdentifier {


    public CallUserIdentifier(Context context) {
    }

    public CallUserIdentifier() {

    }

    public String getUserNameFromContacts(Context context, String phoneNumber) {
        ContentResolver cr = context.getApplicationContext().getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        String contactName = "Unknown";
        if (cursor == null) {
            contactName = "Unknown";
        }

        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }
}
