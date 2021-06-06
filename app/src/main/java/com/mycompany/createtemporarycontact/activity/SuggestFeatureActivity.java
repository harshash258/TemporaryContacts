package com.mycompany.createtemporarycontact.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.mycompany.createtemporarycontact.R;

public class SuggestFeatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_feature);

        setTitle("ConTemp - Suggest Feature");
        WebView webView = findViewById(R.id.webView);
        webView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSdL4z4TgaFYFezQW7uck4azNnC2J-0KRcLBbchR1cW7SoBtBQ/viewform?usp=sf_link");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SuggestFeatureActivity.this, CreateContactActivity.class));
    }
}