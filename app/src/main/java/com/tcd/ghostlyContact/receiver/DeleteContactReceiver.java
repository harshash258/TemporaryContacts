package com.tcd.ghostlyContact.receiver;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.mycompany.createtemporarycontact.R;
import com.tcd.ghostlyContact.database.ContactDatabase;
import com.tcd.ghostlyContact.models.Contacts;


public class DeleteContactReceiver extends BroadcastReceiver {

    String phone, contactName;
    long contactId;

    int rowId;

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            SharedPreferences prefs = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
//            contactId = prefs.getLong("id", 0);
//            contactName = prefs.getString("name", null);
//            phone = prefs.getString("phone", null);
//            long time = prefs.getLong("time", 0);
//
//            intent = new Intent(context, DeleteContactReceiver.class);
//            intent.putExtra("name", contactName);
//            intent.putExtra("phone", phone);
//            intent.putExtra("id", contactId);
//            PendingIntent pendingIntent;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                pendingIntent = PendingIntent.getBroadcast(
//                        context,
//                        (int) System.currentTimeMillis(),
//                        intent,
//                        PendingIntent.FLAG_MUTABLE);
//            } else {
//                pendingIntent = PendingIntent.getBroadcast(
//                        context,
//                        (int) System.currentTimeMillis(),
//                        intent,
//                        0);
//            }
//            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
//                    time,
//                    pendingIntent);
//        }
        contactName = intent.getStringExtra("name");
        contactId = intent.getLongExtra("id", 0);
        phone = intent.getStringExtra("phone");
        rowId = (int) intent.getLongExtra("rowId", 0);
        Log.d("RowId", String.valueOf(rowId));
        deleteContact(context, contactId, rowId);
    }

    private void deleteContact(Context ctx, long contactId, int rowId) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, contactId);
        ctx.getContentResolver().delete(uri, null, null);
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        updateDatabase(ctx, rowId);
        showNotification(ctx);
    }

    private void updateDatabase(Context ctx, int rowId) {
        new Thread(() -> {
            ContactDatabase database = ContactDatabase.getInstance(ctx);
            Contacts contact = database.getContactsDAO().getContactById(rowId);
            if (contact != null) {
                contact.setDeleted(true);
                database.getContactsDAO().update(contact);
                Log.d("Database Updated for " + contact.getName(), " True");
            } else {
                Log.d("Contact is null", "rigt");
            }
        }).start();
    }

    @SuppressLint("MissingPermission")
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
                .setAutoCancel(true)
                .setContentText("Contact Deleted:\nName: " + contactName + "\nPhone Number: " + phone)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Contact Deleted:\nName: " + contactName + "\nPhone Number: " + phone))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(1, builder.build());
    }
}
