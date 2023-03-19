package com.tcd.ghostlyContact.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.hbb20.CountryCodePicker;
import com.mycompany.createtemporarycontact.R;
import com.tcd.ghostlyContact.database.ContactDatabase;
import com.tcd.ghostlyContact.models.Contacts;
import com.tcd.ghostlyContact.receiver.DeleteContactReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CreateContactActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};
    EditText name, phoneNumber;
    Spinner time;
    Button temporaray, permanent;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = findViewById(R.id.name);
        ccp = findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.phoneNumber);
        time = findViewById(R.id.time);
        temporaray = findViewById(R.id.create);
        permanent = findViewById(R.id.permanent);
        ccp.registerCarrierNumberEditText(phoneNumber);

        fillSpinner();

        temporaray.setOnClickListener(v -> {

            if (name.getText().toString().isEmpty() || phoneNumber.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Name or Phone Number missing.", Toast.LENGTH_SHORT).show();
                return;
            }
            createTempContact(name.getText().toString(), ccp.getFullNumberWithPlus());

        });

        permanent.setOnClickListener(v -> {

            if (name.getText().toString().isEmpty() || phoneNumber.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Name or Phone Number missing.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                        2);
            } else {
                createContact(name.getText().toString(), ccp.getFullNumberWithPlus());
                Toast.makeText(this, "Permanent Contact Created", Toast.LENGTH_SHORT).show();
                name.setText("");
                phoneNumber.setText("");
            }
        });
    }

    private long createContact(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = 0;

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .build());

        try {
            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            return ContentUris.parseId(results[0].uri);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void createTempContact(String displayName, String displayPhoneNumber) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_DENIED)
                deniedPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        StringBuilder title = new StringBuilder();
        boolean isContactPermissionDenied = false;
        if (!deniedPermissions.isEmpty()) {
            StringBuilder message = new StringBuilder("We need ");
            for (String permission : deniedPermissions) {
                switch (permission) {
                    case Manifest.permission.WRITE_CONTACTS:
                        isContactPermissionDenied = true;
                        title.append("Contact ");
                        message.append("Contact permission to create temporary contact on your device ");
                        break;
                    case Manifest.permission.POST_NOTIFICATIONS:
                        if (isContactPermissionDenied) {
                            title.append("and Notification Permission Required");
                            message.append("and Notification permission to notify when the contact is deleted");

                        } else {
                            title.append("Notification Permission Required");
                            message.append("Notification permission to notify when the contact is deleted");
                        }
                        break;
                    default:
                        break;
                }
            }
            String[] per;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                per = new String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.POST_NOTIFICATIONS};
            } else {
                per = new String[]{Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS};
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title.toString());
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setMessage(message.toString())
                    .setPositiveButton("Grant Permission", (dialog, which) -> {
                        requestPermissions(per, PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Deny", (dialog, which) -> {
                        Toast.makeText(this, "please grant permission to continue...", Toast.LENGTH_SHORT).show();
                    })
                    .create()
                    .show();
        } else {
            long contactId = createContact(displayName, displayPhoneNumber);

            int selected;
            String msg = "Contact will auto destruct in ";
            if (time.getSelectedItem().equals("1 Day")) {
                selected = 1440;
                msg += "1 Day.";
            } else if (time.getSelectedItem().equals("1 Week")) {
                selected = 10080;
                msg += "1 Week.";
            } else {
                String[] parts = time.getSelectedItem().toString().split(" ");
                String number = parts[0];
                selected = Integer.parseInt(number);
                long time = selected * 60000L;
                int minutes = (int) (time / 1000) / 60;
                msg += minutes + " Minutes.";
            }

            try {
                long rowId = storeDataInDatabase(contactId, displayName, displayPhoneNumber,
                        System.currentTimeMillis() + (selected * 60000L));
                Log.d("RowId", String.valueOf(rowId));
                Intent intent = new Intent(CreateContactActivity.this, DeleteContactReceiver.class);
                intent.putExtra("name", displayName);
                intent.putExtra("phone", displayPhoneNumber);
                intent.putExtra("id", contactId);
                intent.putExtra("rowId", rowId);
                PendingIntent pendingIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    pendingIntent = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            (int) System.currentTimeMillis(),
                            intent,
                            PendingIntent.FLAG_MUTABLE);
                } else {
                    pendingIntent = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            (int) System.currentTimeMillis(),
                            intent,
                            0);
                }
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + (selected * 60000L),
                        pendingIntent);


                Toast.makeText(CreateContactActivity.this, msg, Toast.LENGTH_SHORT).show();

                name.setText("");
                phoneNumber.setText("");

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CreateContactActivity.this, "Something went wrong!!, Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public long storeDataInDatabase(long id, String name, String phoneNumber, long time) {
        Contacts contacts = new Contacts(id, name, phoneNumber, time, false);
        ContactDatabase database = ContactDatabase.getInstance(this);
        AtomicLong rowId = new AtomicLong();
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            rowId.set(database.getContactsDAO().insert(contacts));
            Log.d("Contact Added:", name);
            latch.countDown();
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rowId.get();
    }

    public void storeData(long id, String name, String phoneNumber, long time) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("id", id);
        editor.putString("name", name);
        editor.putString("phone", phoneNumber);
        editor.putLong("time", time);
        editor.apply();
    }

    private void fillSpinner() {
        ArrayList<String> timeSpinner = new ArrayList<>();
        timeSpinner.add("1 min");
        timeSpinner.add("5 min");
        timeSpinner.add("10 min");
        timeSpinner.add("30 min");
        timeSpinner.add("60 min");
        timeSpinner.add("1 day");
        timeSpinner.add("1 Week");


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeSpinner);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                createTempContact(name.getText().toString(), ccp.getFullNumberWithPlus());
            } else {
                new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Permission Required!!!")
                        .setMessage("You have denied permission more than once, Tap Settings > Permission and allow Contacts and Notification Permission to continue")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                createContact(name.getText().toString(), ccp.getFullNumberWithPlus());
        }
    }
}