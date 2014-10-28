package x40240.shaun.adriano.a4.app;

import java.util.ArrayList;
import x40240.shaun.adriano.a4.app.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity
    extends Activity
{
    public static final String LOGTAG = "WebViewExample";
    public static final String MAPS_BASE_URL = "http://maps.google.com?z=15&q=";
    
    private int      index;
    private String[] urls;
    private String[] locations;
    
    private WebView webView;
    private final ArrayList<String> allUrls = new ArrayList<String>();
    
    @SuppressLint ("SetJavaScriptEnabled")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.web_view);
        webView.setKeepScreenOn(true);
        webView.setInitialScale(100);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new MyWebViewClient());
        
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        
        final Resources res = this.getResources();
        urls = res.getStringArray(R.array.urls);
        locations = res.getStringArray(R.array.locations);
        
        for (String url: urls)
            allUrls.add(url);
        
        final StringBuilder sb = new StringBuilder();
        for (String loc: locations) {
            sb.append(MAPS_BASE_URL);
            sb.append(loc);
            allUrls.add(sb.toString());
            sb.delete(0, sb.length());
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onNextButtonClick (View view) {
        if (index > (allUrls.size()-1))
            index = 0;
        final String url = allUrls.get(index);
        webView.loadUrl(url);
        index++;
    }
    
    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e(LOGTAG, error.toString());
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
    }
}
