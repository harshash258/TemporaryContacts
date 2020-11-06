package com.mycompany.createtemporarycontact;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hbb20.CountryCodePicker;

public class WhatsAppMessage extends AppCompatActivity {

    EditText number;
    Button send;
    CountryCodePicker countryCodePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app_message);

        setTitle("Send WhatsApp Message");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        number = findViewById(R.id.phoneNumber);
        send = findViewById(R.id.sendMessage);
        countryCodePicker = findViewById(R.id.ccp);

        number.requestFocus();
        countryCodePicker.registerCarrierNumberEditText(number);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        send.setOnClickListener(v -> {
            String phoneNumber = countryCodePicker.getFullNumberWithPlus();
            Log.d("TAG", "onClick: " + phoneNumber);
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

}