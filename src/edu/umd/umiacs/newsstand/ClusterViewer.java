package edu.umd.umiacs.newsstand;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ClusterViewer extends Activity {
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("http://www.google.com");
    }
}