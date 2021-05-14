package com.mycompany.createtemporarycontact;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;

public class CreateContact extends AppCompatActivity {

    EditText name, phoneNumber;
    Spinner time;
    Button create, permanent;
    InterstitialAd mInterstitialAd;
    String displayName, displayPhone, fullNumber;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        setTitle("ConTemp - Create Contact");
        loadAd();

        name = findViewById(R.id.name);
        ccp = findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.phoneNumber);
        time = findViewById(R.id.time);
        create = findViewById(R.id.create);
        permanent = findViewById(R.id.permanent);
        ccp.registerCarrierNumberEditText(phoneNumber);

        fillSpinner();

        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        create.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                        1);
            } else {
                int selected;
                if (time.getSelectedItem().equals("1 Day"))
                    selected = 1440;
                else if (time.getSelectedItem().equals("1 Week"))
                    selected = 10080;
                else
                    selected = Integer.parseInt(time.getSelectedItem().toString());
                displayPhone = phoneNumber.getText().toString();
                displayName = name.getText().toString();
                fullNumber = ccp.getFormattedFullNumber();

                if (!displayName.equals("") && (!displayPhone.equals(""))) {
                    checkData(ops, displayName, fullNumber);
                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        Intent intent = new Intent(CreateContact.this, DeleteContactReceiver.class);
                        intent.putExtra("name", name.getText().toString());
                        intent.putExtra("phone", ccp.getFullNumberWithPlus());
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                   /* if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                                + (selected * 60000), pendingIntent);*/

                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                                + (selected * 60000), pendingIntent);


                        long time = selected * 60000;
                        int minutes = (int) (time / 1000) / 60;
                        Toast.makeText(CreateContact.this, "Contact will be deleted in "
                                + minutes + " Minutes", Toast.LENGTH_SHORT).show();

                        name.setText("");
                        phoneNumber.setText("");
                        if (mInterstitialAd != null)
                            showInterstitialAd();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CreateContact.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateContact.this, "Name or Phone Number Missing", Toast.LENGTH_SHORT).show();
                }
            }
        });

        permanent.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                        1);
            } else {

                displayPhone = phoneNumber.getText().toString();
                displayName = name.getText().toString();
                fullNumber = ccp.getFormattedFullNumber();

                if (!displayName.equals("") && (!displayPhone.equals(""))) {
                    checkData(ops, displayName, "+91" + fullNumber);
                    try {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        Toast.makeText(CreateContact.this, "Contact Created", Toast.LENGTH_SHORT).show();
                        name.setText("");
                        phoneNumber.setText("");
                        if (mInterstitialAd != null)
                            showInterstitialAd();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CreateContact.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateContact.this, "Name or Phone Number Missing", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void loadAd() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }

                });
       /* mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitialAd));
        mInterstitialAd.loadAd(adRequest);*/

    }


    private void fillSpinner() {
        ArrayList<String> timeSpinner = new ArrayList<>();
        timeSpinner.add("1");
        timeSpinner.add("10");
        timeSpinner.add("30");
        timeSpinner.add("60");
        timeSpinner.add("1 Day");
        timeSpinner.add("1 Week");


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeSpinner);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time.setAdapter(arrayAdapter);
    }

    private void checkData(ArrayList<ContentProviderOperation> ops, String displayName, String displayPhone) {
        if (!displayName.equals("")) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            this.displayName).build());
        }

        if (!displayPhone.equals("")) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, ccp.getFormattedFullNumber())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }
    }


    private void showInterstitialAd() {
        mInterstitialAd.show(this);
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                mInterstitialAd = null;
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (mInterstitialAd != null) {
            showInterstitialAd();
        } else {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
/*        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    Intent a = new Intent(Intent.ACTION_MAIN);
                    a.addCategory(Intent.CATEGORY_HOME);
                    a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(a);
                }
            });

        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.view_Contacts) {
            Intent intent = new Intent(CreateContact.this, DisplayContactList.class);
            startActivity(intent);
        } else if (id == R.id.sendMessage) {
            Intent intent = new Intent(CreateContact.this, WhatsAppMessage.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}