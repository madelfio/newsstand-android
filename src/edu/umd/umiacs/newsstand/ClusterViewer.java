package edu.umd.umiacs.newsstand;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ClusterViewer extends Activity {
    private WebView mWebView;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Let's display the progress in the activity title bar, like the
        // browser app does.
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.webview);

        mActivity = this;
        String gaz_id = this.getIntent().getStringExtra("gaz_id");


        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebChromeClient(new NewsStandWebChromeClient());
        mWebView.setWebViewClient(new NewsStandWebViewClient());

        // Include required JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("http://newsstand.umiacs.umd.edu/news/top_locations3?gaz_id=" + gaz_id + "&num_images=1");
        String map_js_filename = "http://newsstand.umiacs.umd.edu/javascripts/map.js";
        mWebView.loadUrl("javascript:var fileref=document.createElement('script');fileref.setAttribute('type','text/javascript');fileref.setAttribute('src', '" + map_js_filename + "');");
        String prototype_js_filename = "http://newsstand.umiacs.umd.edu/javascripts/prototype.js";
        mWebView.loadUrl("javascript:var fileref=document.createElement('script');fileref.setAttribute('type','text/javascript');fileref.setAttribute('src', '" + prototype_js_filename + "');");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class NewsStandWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(mActivity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }
    }

    private class NewsStandWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int progress) {
            // Activities and WebViews measure progress with different scales.
            // The progress meter will automatically disappear when we reach 100%
            mActivity.setProgress(progress * 100);
        }
    }
}