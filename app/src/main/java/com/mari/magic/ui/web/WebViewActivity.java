package com.mari.magic.ui.web;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.mari.magic.R;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewQC";
    private WebView webView;
    private String initialUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webView);
        initialUrl = getIntent().getStringExtra("url");

        // ===== WebView Settings =====
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBlockNetworkImage(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        // ===== Domains to block (QC / ads / popups / trackers) =====
        List<String> blockDomains = Arrays.asList(
                // Google Ads & Analytics
                "doubleclick.net",
                "googlesyndication.com",
                "adservice.google.com",
                "admob.com",
                "googleadservices.com",
                "google-analytics.com",
                "stats.g.doubleclick.net",
                "pagead2.googlesyndication.com",
                // Ad Networks / Popups
                "taboola.com",
                "outbrain.com",
                "pubmatic.com",
                "adsystem.com",
                "adform.net",
                "mediaplex.com",
                "criteo.com",
                "adroll.com",
                "revcontent.com",
                "propellerads.com",
                "mgid.com",
                "zucks.net",
                "popads.net",
                "popcash.net",
                "adcash.com",

                // Social trackers / marketing
                "facebook.net",
                "facebook.com",
                "connect.facebook.net",
                "twitter.com",
                "platform.twitter.com",
                "linkedin.com",
                "analytics.twitter.com",
                "instagram.com",

                // Video / popup ads
                "adcolony.com",
                "vungle.com",
                "chartboost.com",
                "unityads.unity3d.com",
                "ironSource.com",
                "yad2.co.il"
        );

        // ===== WebViewClient =====
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if(url.startsWith("http://") || url.startsWith("https://")){
                    return false; // load bình thường
                } else {
                    Log.d(TAG, "Blocked external scheme: " + url);
                    return true; // chặn mọi scheme khác (intent://, shopeevn:// ...)
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                if (host != null) {
                    for (String domain : blockDomains) {
                        if (host.contains(domain)) {
                            Log.d(TAG, "Blocked URL: " + request.getUrl());
                            return new WebResourceResponse(
                                    "text/plain",
                                    "utf-8",
                                    new ByteArrayInputStream("".getBytes())
                            );
                        }
                    }
                    // Chặn Shopee redirect
                    if(host.contains("shopee")){
                        Log.d(TAG,"Blocked Shopee redirect: "+request.getUrl());
                        return new WebResourceResponse(
                                "text/plain",
                                "utf-8",
                                new ByteArrayInputStream("".getBytes())
                        );
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Inject JS:
                // - chặn window.open
                // - chặn location.assign sang scheme khác
                // - ẩn popup modal 18+ Doujindesu
                String js = ""
                        + "window.open = function(url){console.log('Blocked popup: '+url);};"
                        + "var originalAssign = window.location.assign;"
                        + "window.location.assign = function(url){"
                        + " if(url.startsWith('http')){originalAssign(url);} else{console.log('Blocked redirect: '+url);}};"
                        + "var popups = document.querySelectorAll('.popup-confirm, .popup-overlay, #popupModal, .modal');"
                        + "popups.forEach(function(p){p.style.display='none';});"
                        + "var overlays = document.querySelectorAll('.overlay, .modal-backdrop');"
                        + "overlays.forEach(function(o){o.style.display='none';});";

                view.evaluateJavascript(js,null);
            }
        });

        // ===== WebChromeClient: chặn pop-up window =====
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg){
                Log.d(TAG, "Blocked window.open attempt");
                return false;
            }
        });

        // ===== Load URL doujin =====
        webView.loadUrl(initialUrl);

        // ===== Handle back gestures + button =====
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed(){
                if(webView.canGoBack()){
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}