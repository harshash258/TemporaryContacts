package com.mycompany.createtemporarycontact.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mycompany.createtemporarycontact.R;

import java.util.Arrays;

public class DeleteContactReceiver extends BroadcastReceiver {

    String phone, contactName;

    @Override
    public void onReceive(Context context, Intent intent) {

        phone = intent.getStringExtra("phone");
        contactName = intent.getStringExtra("name");
        deleteContact(context, phone, contactName);
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
//                        Toast.makeText(ctx, "Contact Deleted", Toast.LENGTH_SHORT).show();
                        showNotification(ctx);
                    }

                } while (cur.moveToNext());

            }

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void showNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String name = "delete";
            String description = "Inform about contact that has been deleted.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(name, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                "delete")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Contact Deleted:\n  Name: " + contactName + "\n  Phone Number: " + phone)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Contact Deleted:\n  Name: " + contactName + "\n  Phone Number: " + phone))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
