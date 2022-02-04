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

class BoothClickJSInterface {
    private BoothSelectedCallback _callback;

    public BoothClickJSInterface(BoothSelectedCallback callback) {
        _callback = callback;
    }

    @JavascriptInterface
    public void postMessage(String boothName) {
        _callback.onBoothSelected(boothName);
    }
}

class FpReadyJSInterface {
    private FpConfiguredCallback _callback;

    public FpReadyJSInterface(FpConfiguredCallback callback) {
        _callback = callback;
    }

    @JavascriptInterface
    public void postMessage(String message) {
        _callback.onFpConfigured();
    }
}

class DirectionJSInterface {
    private RouteCreatedCallback _callback;

    public DirectionJSInterface(RouteCreatedCallback callback) {
        _callback = callback;
    }

    @JavascriptInterface
    public void postMessage(String directionJson) throws JSONException {
        try {
            Route route = Helper.parseRoute(directionJson);
            _callback.onRouteCreated(route);
        } catch (JSONException e) {
            throw e;
        }
    }
}

/**
 * View for displaying expo plans
 */
public class FplanView extends FrameLayout {

    private WebView _webView;

    /**
     * Constructor
     * @param context
     */
    public FplanView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * Constructor
     * @param context
     * @param attrs
     */
    public FplanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * Constructor
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public FplanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * Initializing a view
     * @param url expo plan URL
     * @param boothSelectedCallback Callback on booth selection
     * @param fpConfiguredCallback Callback when the view has finished initializing
     * @param routeCreatedCallback Callback when building a route
     */
    public void init(String url, @Nullable BoothSelectedCallback boothSelectedCallback,
                     @Nullable FpConfiguredCallback fpConfiguredCallback,
                     @Nullable RouteCreatedCallback routeCreatedCallback) {
        String eventId = url.substring(8, url.indexOf('.'));
        init(url, eventId, boothSelectedCallback, fpConfiguredCallback, routeCreatedCallback);
    }

    /**
     * Initializing a view
     * @param url expo plan URL
     * @param eventId event ID
     * @param boothSelectedCallback Callback on booth selection
     * @param fpConfiguredCallback Callback when the view has finished initializing
     * @param routeCreatedCallback Callback when building a route
     */
    public void init(String url, String eventId, @Nullable BoothSelectedCallback boothSelectedCallback,
                     @Nullable FpConfiguredCallback fpConfiguredCallback,
                     @Nullable RouteCreatedCallback routeCreatedCallback) {

        _webView.post(() -> {
            if (boothSelectedCallback != null) {
                _webView.addJavascriptInterface(new BoothClickJSInterface(boothSelectedCallback), "onBoothClickHandler");
            }

            if (fpConfiguredCallback != null) {
                _webView.addJavascriptInterface(new FpReadyJSInterface(fpConfiguredCallback), "onFpConfiguredHandler");
            }

            if (routeCreatedCallback != null) {
                _webView.addJavascriptInterface(new DirectionJSInterface(routeCreatedCallback), "onDirectionHandler");
            }

            String html = "";
            try {
                InputStream inputStream = this.getContext().getAssets().open("index.html");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                html = new String(buffer);
                html = html.replace("$url#", url).replace("$eventId#", eventId);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ConnectivityManager cm = (ConnectivityManager) this.getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);
            if(cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()){
                _webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            }
            else{
                _webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }
            _webView.loadDataWithBaseURL(url, html, "text/html", "en_US", null);
        });
    }

    /**
     * Select booth
     * @param boothName Booth name or externalID
     */
    public void selectBooth(String boothName) {
        _webView.post(() -> {
            String js = String.format("selectBooth('%s')", boothName);
            _webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Set current position(Blu Dot) on route
     * @param x X
     * @param y Y
     */
    public void setCurrentPosition(int x, int y) {
        setCurrentPosition(x, y, false);
    }

    /**
     * Set current position(Blu Dot) on route
     * @param x X
     * @param y Y
     * @param focus True - focus on a point
     */
    public void setCurrentPosition(int x, int y, boolean focus) {
        _webView.post(() -> {
            String js = String.format("selectRoute(%d, %d, %b)", x, y, focus);
            _webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Build a route from one booth to another
     * @param from Start booth
     * @param to End booth
     */
    public void buildRoute(String from, String to){
        buildRoute(from, to, false);
    }

    /**
     * Build a route from one booth to another
     * @param from Start booth
     * @param to End booth
     * @param exceptInaccessible True - exclude routes that are inaccessible to people with disabilities, False - include all routes
     */
    public void buildRoute(String from, String to, boolean exceptInaccessible) {
        _webView.post(() -> {
            String js = String.format("selectRoute('%s', '%s', %b)", from, to, exceptInaccessible);
            _webView.evaluateJavascript(js, null);
        });
    }

    private void initView(Context context) {
        _webView = new WebView(this.getContext());

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT | Gravity.BOTTOM | Gravity.LEFT;

        _webView.setLayoutParams(layoutParams);

        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.getSettings().setDomStorageEnabled(true);
        _webView.getSettings().setAllowFileAccess( true );
        _webView.getSettings().setAppCacheEnabled( true );

        this.addView(_webView);
    }
}