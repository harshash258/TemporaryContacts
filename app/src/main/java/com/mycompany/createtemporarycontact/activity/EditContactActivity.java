package com.mycompany.createtemporarycontact.activity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.hbb20.CountryCodePicker;
import com.mycompany.createtemporarycontact.R;

import java.util.ArrayList;

public class EditContactActivity extends AppCompatActivity {

    private final static String[] DATA_COLS = {

            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.CONTACT_ID
    };
    EditText name, phone;
    Button submit;
    CountryCodePicker ccp;
    InterstitialAd mInterstitialAd;

    public static void updateNameAndNumber(final Context context, String number, String newName, String newNumber) {

        if (context == null || number == null || number.trim().isEmpty()) return;

        if (newNumber != null && newNumber.trim().isEmpty()) newNumber = null;

        if (newNumber == null) return;


        String contactId = getContactId(context, number);

        if (contactId == null) return;

        String where = String.format(
                "%s = '%s' AND %s = ?",
                DATA_COLS[0],
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                DATA_COLS[2]);

        String[] args = {contactId};

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, args)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, newName)
                        .build()
        );

        where = String.format(
                "%s = '%s' AND %s = ?",
                DATA_COLS[0],
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                DATA_COLS[1]);

        args[0] = number;

        operations.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, args)
                        .withValue(DATA_COLS[1]/*number*/, newNumber)
                        .build()
        );

        try {

            ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
            for (ContentProviderResult result : results) {

                Log.d("Update Result", result.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getContactId(Context context, String number) {

        if (context == null) return null;
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                new String[]{number},
                null
        );

        if (cursor == null || cursor.getCount() == 0) return null;
        cursor.moveToFirst();

        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

        cursor.close();
        return id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phoneNumber);
        submit = findViewById(R.id.updateContact);
        ccp = findViewById(R.id.ccp);

        ccp.registerCarrierNumberEditText(phone);
        loadAd();

        Intent intent = getIntent();
        String oldNumber = intent.getStringExtra("phone");
        String oldName = intent.getStringExtra("name");

        setTitle(oldName + " - Edit Contact");

        name.setText(oldName);
        phone.setText(oldNumber);

        submit.setOnClickListener(v -> {
            String names = " ";
            names = name.getText().toString();
            updateNameAndNumber(this, oldNumber, names, ccp.getFormattedFullNumber());
            showSnakBar();
            if (mInterstitialAd != null)
                showInterstitialAd();

        });
    }

    private void loadAd() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(this, "ca-app-pub-3914175453073115/1949477542", adRequest,
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
    }

    private void showSnakBar() {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, "Contact Updated", Snackbar.LENGTH_LONG);

        snackbar.setAction("OK", view -> {
            snackbar.dismiss();
        });
        snackbar.setTextColor(Color.parseColor("#ffffff"));
        snackbar.setActionTextColor(Color.parseColor("#ffffff"));
        snackbar.show();
    }

    private void showInterstitialAd() {
        mInterstitialAd.show(this);
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                Intent intent1 = new Intent(EditContactActivity.this, DisplayContactListActivity.class);
                startActivity(intent1);
            }

            @Override
            public void onAdShowedFullScreenContent() {
                loadAd();
            }
        });

    }
}