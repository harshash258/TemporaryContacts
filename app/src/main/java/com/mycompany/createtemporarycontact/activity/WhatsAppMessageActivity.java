package com.mycompany.createtemporarycontact.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hbb20.CountryCodePicker;
import com.mycompany.createtemporarycontact.R;
import com.mycompany.createtemporarycontact.adapter.LogsAdapter;
import com.mycompany.createtemporarycontact.model.Logs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WhatsAppMessageActivity extends AppCompatActivity {

    EditText number;
    Button send;
    CountryCodePicker countryCodePicker;
    TextView callLogs;
    RecyclerView recyclerView;
    List<Logs> mLogs;
    LogsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app_message);

        setTitle("Send WhatsApp Message");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        number = findViewById(R.id.phoneNumber);
        callLogs = findViewById(R.id.permission);
        send = findViewById(R.id.sendMessage);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(number);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        recyclerView = findViewById(R.id.logRecyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLogs = new ArrayList<>();
        adapter = new LogsAdapter(mLogs, position -> {
            String tempNumber = mLogs.get(position).getNumber();
            String ccp = tempNumber.substring(0, 3);
            Log.d("CCP", ccp);
            if (tempNumber.startsWith("+91")) {
                number.setText(tempNumber.substring(3, 13));
            } else {
                number.setText(tempNumber);
            }
        });
        recyclerView.setAdapter(adapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 1000);
        } else {
            callLogs.setVisibility(View.GONE);
            new Thread(this::getCallDetails).start();

        }

        callLogs.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 1000);
            } else {
                callLogs.setVisibility(View.GONE);
                getCallDetails();
            }
        });

        send.setOnClickListener(v -> {
            String phoneNumber = countryCodePicker.getFullNumberWithPlus();
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callLogs.setVisibility(View.GONE);
                getCallDetails();
            }
        }
    }

    private void getCallDetails() {

        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            Logs logs = new Logs(phNumber, callDayTime.toString(), dir);
            mLogs.add(logs);
        }
        adapter.notifyDataSetChanged();
        managedCursor.close();
    }
}