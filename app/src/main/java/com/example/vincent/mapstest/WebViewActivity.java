package com.example.vincent.mapstest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity
{
    String url;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = (WebView)findViewById(R.id.qrWebView);
        webView.setWebViewClient(new WebViewClient());

       Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            url = extras.getString("url");
            webView.loadUrl(url);
        }
    }
}
