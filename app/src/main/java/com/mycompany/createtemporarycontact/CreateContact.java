package com.mycompany.createtemporarycontact;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;

public class CreateContact extends AppCompatActivity {

    EditText name, phoneNumber;
    Spinner time;
    Button create, permanent;
    InterstitialAd mInterstitialAd;
    String displayName, displayPhone, fullNumber;
    CountryCodePicker ccp;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contact);

        loadAd();
        askPermissions();

        name = findViewById(R.id.name);
        ccp = findViewById(R.id.ccp);
        phoneNumber = findViewById(R.id.phoneNumber);
        time = findViewById(R.id.time);
        create = findViewById(R.id.create);
        permanent = findViewById(R.id.permanent);


        fillSpinner();
        ccp.registerCarrierNumberEditText(phoneNumber);

        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        create.setOnClickListener(v -> {
            int selected = Integer.parseInt(time.getSelectedItem().toString());
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

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                                + (selected * 60000), pendingIntent);
                    else
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                                + (selected * 60000), pendingIntent);


                    long time = selected * 60000;
                    int minutes = (int) (time / 1000) / 60;
                    Toast.makeText(CreateContact.this, "Contact will be deleted in "
                            + minutes + " Minutes", Toast.LENGTH_SHORT).show();
                    recreate();

                    name.setText("");
                    phoneNumber.setText("");

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(CreateContact.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CreateContact.this, "Name or Phone Number Missing", Toast.LENGTH_SHORT).show();
            }
        });

        permanent.setOnClickListener(v -> {
            displayPhone = phoneNumber.getText().toString();
            displayName = name.getText().toString();
            fullNumber = ccp.getFormattedFullNumber();

            if (!displayName.equals("") && (!displayPhone.equals(""))) {
                checkData(ops, displayName, "+91" + fullNumber);
                try {
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    Toast.makeText(CreateContact.this, "Contact Created", Toast.LENGTH_SHORT).show();
                    recreate();

                    name.setText("");
                    phoneNumber.setText("");

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(CreateContact.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CreateContact.this, "Name or Phone Number Missing", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAd() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitialAd));
        mInterstitialAd.loadAd(adRequest);

    }

    private void askPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    1);
        }
    }

    private void fillSpinner() {
        ArrayList<String> timeSpinner = new ArrayList<>();
        timeSpinner.add("1");
        timeSpinner.add("10");
        timeSpinner.add("30");
        timeSpinner.add("60");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeSpinner);
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


    @Override
    public void onBackPressed() {

        if (mInterstitialAd.isLoaded()) {
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
        }
        super.onBackPressed();
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