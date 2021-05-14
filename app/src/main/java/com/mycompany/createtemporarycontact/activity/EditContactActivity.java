package com.mycompany.createtemporarycontact.activity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ccp.registerCarrierNumberEditText(phone);

        Intent intent = getIntent();
        String oldNumber = intent.getStringExtra("phone");
        String oldName = intent.getStringExtra("name");

        Log.d("Name: ", oldName + "------" + oldNumber);

        setTitle(oldName +  " - Edit Contact");

        name.setText(oldName);
        phone.setText(oldNumber);

        submit.setOnClickListener(v -> {
            String names = " ";
            names = name.getText().toString();
            Log.d("TAG", "onCreate: " + names);
            updateNameAndNumber(this, oldNumber, names, ccp.getFormattedFullNumber());
            Toast.makeText(this, "Contact Updated", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(this, DisplayContactListActivity.class);
            startActivity(intent1);
        });
    }
}