package com.mycompany.createtemporarycontact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

public class DeleteContactReceiver extends BroadcastReceiver {

    String phone, name;


    @Override
    public void onReceive(Context context, Intent intent) {

        phone = intent.getStringExtra("phone");
        name = intent.getStringExtra("name");
        deleteContact(context, phone, name);
    }

    private void deleteContact(Context ctx, String phone, String name) {
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        try (Cursor cur = ctx.getContentResolver().query(contactUri, null, null, null, null)) {
            if (cur.moveToFirst()) {
                do {
                    if (cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)).equalsIgnoreCase(name)) {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        ctx.getContentResolver().delete(uri, null, null);
                        Toast.makeText(ctx, "Contact Deleted", Toast.LENGTH_SHORT).show();
                    }

                } while (cur.moveToNext());

            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}
