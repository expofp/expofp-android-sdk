package com.expofp.fplan;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;

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
            JSONObject jObject = new JSONObject(directionJson);
            Route route = new Route();
            route.setDistance(jObject.getString("distance"));
            route.setDuration(Duration.ofSeconds(jObject.getInt("time")));
            _callback.onRouteCreated(route);
        } catch (JSONException e) {
            throw e;
        }
    }
}

public class FplanView extends FrameLayout {

    private WebView _webView;

    public FplanView(Context context) {
        super(context);
        initView(context);
    }

    public FplanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public FplanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void init(String url, @Nullable BoothSelectedCallback boothSelectedCallback,
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

            _webView.loadUrl(url);
        });
    }

    public void selectBooth(String boothName) {
        _webView.post(() -> {
            String js = String.format("selectBooth('%s')", boothName);
            _webView.evaluateJavascript(js, null);
        });
    }

    public void buidRoute(String from, String to){
        buidRoute(from, to, false);
    }

    public void buidRoute(String from, String to, boolean exceptUnaccessible) {
        _webView.post(() -> {
            String js = String.format("selectRoute('%s', '%s', %b)", from, to, exceptUnaccessible);
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