package com.expofp.fplan;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

/**
 * View for displaying expo plans
 */
public class FplanView extends FrameLayout {

    private WebView _webView;
    private FplanEventListener _eventListener;

    /**
     * Constructor
     *
     * @param context Context
     */
    public FplanView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs   Attributes
     */
    public FplanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context      Context
     * @param attrs        Attributes
     * @param defStyleAttr Style attribute
     */
    public FplanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * Initializing a view
     *
     * @param url           Expo plan URL
     * @param eventListener Events listener
     */
    public void init(String url, @Nullable FplanEventListener eventListener) {
        init(url, true, eventListener);
    }

    /**
     * Initializing a view
     *
     * @param url           Expo plan URL
     * @param noOverlay     True - Hides the panel with information about exhibitors
     * @param eventListener Events listener
     */
    public void init(String url, Boolean noOverlay, @Nullable FplanEventListener eventListener) {
        _eventListener = eventListener;

        String eventId = getEventId(url);
        String baseUrl = url.substring(0, url.indexOf(".expofp.com") + 11);

        _webView.post(() -> {
            String html = "";
            try {
                InputStream inputStream = this.getContext().getAssets().open("index.html");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                html = new String(buffer);
                html = html.replace("$url#", baseUrl).replace("$eventId#", eventId).replace("$noOverlay#", noOverlay.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            ConnectivityManager cm = (ConnectivityManager) this.getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);
            if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                _webView.clearCache(true);
                _webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            } else {
                _webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }
            _webView.loadDataWithBaseURL(url, html, "text/html", "en_US", null);
        });
    }

    /**
     * Select booth
     *
     * @param boothName Booth name or externalID
     */
    public void selectBooth(String boothName) {
        _webView.post(() -> {
            String js = String.format("selectBooth('%s')", boothName);
            _webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Set current position(Blue-Dot) on route
     *
     * @param x X
     * @param y Y
     */
    public void setCurrentPosition(int x, int y) {
        setCurrentPosition(x, y, false);
    }

    /**
     * Set current position(Blue-Dot) on route
     *
     * @param x     X
     * @param y     Y
     * @param focus True - focus on a point
     */
    public void setCurrentPosition(int x, int y, boolean focus) {
        _webView.post(() -> {
            String js = String.format("setCurrentPosition(%d, %d, %b)", x, y, focus);
            _webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Build a route from one booth to another
     *
     * @param from Start booth
     * @param to   End booth
     */
    public void buildRoute(String from, String to) {
        buildRoute(from, to, false);
    }

    /**
     * Build a route from one booth to another
     *
     * @param from               Start booth
     * @param to                 End booth
     * @param exceptInaccessible True - exclude routes that are inaccessible to people with disabilities, False - include all routes
     */
    public void buildRoute(String from, String to, boolean exceptInaccessible) {
        _webView.post(() -> {
            String js = String.format("selectRoute('%s', '%s', %b)", from, to, exceptInaccessible);
            _webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Clear floor plan
     */
    public void clear() {
        _webView.post(() -> {
            String js = String.format("selectRoute(null, null, false)");
            _webView.evaluateJavascript(js, null);

            js = String.format("setCurrentPosition(null, null, false)");
            _webView.evaluateJavascript(js, null);
        });
    }

    private  String getEventId(String url) {
        String baseUrl = url.replace("http://", "")
                .replace("https://", "").replace("www.", "");

        String eventId = baseUrl.substring(0, baseUrl.indexOf('.'));
        return eventId;
    }

    private void initView(Context context) {
        _webView = new WebView(this.getContext());

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT | Gravity.BOTTOM | Gravity.LEFT;

        _webView.setLayoutParams(layoutParams);

        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.getSettings().setDomStorageEnabled(true);
        _webView.getSettings().setAllowFileAccess(true);
        _webView.getSettings().setAppCacheEnabled(true);

        _webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void postMessage(String message) {
                if (_eventListener != null) {
                    _eventListener.onFpConfigured();
                }
            }
        }, "onFpConfiguredHandler");

        _webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void postMessage(String boothName) {
                if (_eventListener != null) {
                    _eventListener.onBoothSelected(boothName);
                }
            }
        }, "onBoothClickHandler");

        _webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void postMessage(String directionJson) throws JSONException {
                if (_eventListener != null) {
                    try {
                        Route route = Helper.parseRoute(directionJson);
                        _eventListener.onRouteCreated(route);
                    } catch (JSONException e) {
                        throw e;
                    }
                }
            }
        }, "onDirectionHandler");

        this.addView(_webView);
    }
}