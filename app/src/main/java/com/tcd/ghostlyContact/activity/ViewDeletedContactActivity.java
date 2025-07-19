package com.tcd.ghostlyContact.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.mycompany.createtemporarycontact.R;
import com.tcd.ghostlyContact.ContactsViewModel;
import com.tcd.ghostlyContact.adapter.DeletedContactAdapter;
import com.tcd.ghostlyContact.database.ContactDatabase;
import com.tcd.ghostlyContact.models.Contacts;
import com.tcd.ghostlyContact.receiver.DeleteContactReceiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class ViewDeletedContactActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};
    private static final String TAG = "ViewDeletedContactActivity";
    RecyclerView recyclerView;
    List<Contacts> deletedContacts;
    DeletedContactAdapter adapter;
    ContactsViewModel viewModel;
    String fullName, fullPhoneNumber, fullTime;
    LinearLayout noContacts;
    AdView adView;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deleted_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        noContacts = findViewById(R.id.noContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deletedContacts = new ArrayList<>();

        adView = findViewById(R.id.banner_ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        InterstitialAd.load(this, "ca-app-pub-3914175453073115/7307516125", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

        adapter = new DeletedContactAdapter(deletedContacts, new DeletedContactAdapter.ClickListener() {
            @Override
            public void createTemporary(int position, @NonNull String name, @NonNull String phoneNumber, @NonNull String time) {
                fullName = name;
                fullPhoneNumber = phoneNumber;
                fullTime = time;
                createTempContact(name, phoneNumber, time);
            }

            @Override
            public void createPermanent(int position, @NonNull String name, @NonNull String phoneNumber) {
                fullName = name;
                fullPhoneNumber = phoneNumber;
                if (ContextCompat.checkSelfPermission(ViewDeletedContactActivity.this, android.Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                            2);
                } else {
                    createContact(name, phoneNumber);
                    Toast.makeText(ViewDeletedContactActivity.this, "Permanent Contact Created", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        viewModel.getGetAllContacts().observe(this, contacts -> {
            if (!contacts.isEmpty()) {
                noContacts.setVisibility(View.GONE);
            } else {
                noContacts.setVisibility(View.VISIBLE);
            }
            adapter.setData(contacts);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(ViewDeletedContactActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

            }
        });
    }

    public void createTempContact(String displayName, String displayPhoneNumber, String time) {
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
            if (time.equals("1 Day")) {
                selected = 1440;
                msg += "1 Day.";
            } else {
                String[] parts = time.split(" ");
                String number = parts[0];
                selected = Integer.parseInt(number);
                long totalTime = selected * 60000L;
                int minutes = (int) (totalTime / 1000) / 60;
                msg += minutes + " Minutes.";
            }

            try {
                long rowId = storeDataInDatabase(contactId, displayName, displayPhoneNumber,
                        System.currentTimeMillis() + (selected * 60000L));
                Intent intent = new Intent(ViewDeletedContactActivity.this, DeleteContactReceiver.class);
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


                Toast.makeText(ViewDeletedContactActivity.this, msg, Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ViewDeletedContactActivity.this, "Something went wrong!!, Please try again.", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deleted_contacts_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(ViewDeletedContactActivity.this);
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.");
            }
            finish();
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            recreate();
        }
        return super.onOptionsItemSelected(item);
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
                createTempContact(fullName, fullPhoneNumber, fullTime);
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
                createContact(fullName, fullPhoneNumber);
        }
    }
}