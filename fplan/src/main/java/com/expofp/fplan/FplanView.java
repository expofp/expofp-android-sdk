package com.expofp.fplan;

import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * View for displaying expo plans
 */
public class FplanView extends FrameLayout {

    private WebView webView;
    private FplanEventListener eventListener;
    private ConnectivityManager connectivityManager;

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
        this.eventListener = eventListener;

        String eventId = getEventId(url);
        String baseUrl = url.substring(0, url.indexOf(".expofp.com") + 11);
        String params = "";
        if(url.contains("?")){
            params = url.substring(url.indexOf("?"));
        }

        File cacheDir = getContext().getFilesDir();
        File fPlanCacheDir = new File(cacheDir, "fplan");
        if(fPlanCacheDir.exists()){
            fPlanCacheDir.delete();
        }

        File expoCacheDir = new File(fPlanCacheDir, eventId);
        File indexFilePath = new File(expoCacheDir, "index.html");

        String html = "";
        try {
            InputStream inputStream = this.getContext().getAssets().open("index.html");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            html = new String(buffer);
            html = html.replace("$url#", "file:///" + expoCacheDir.getAbsolutePath()).replace("$eventId#", eventId).replace("$noOverlay#", noOverlay.toString());
            Helper.writeToFile(indexFilePath, html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String finalParams = params;
        Context context = this.getContext();

        if (connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            ReentrantLock locker = new ReentrantLock();
            AtomicBoolean cached = new AtomicBoolean(false);
            AtomicBoolean loaded = new AtomicBoolean(false);

            webView.post(() -> {
                webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                webView.setWebViewClient(new WebViewClient(){
                    @Nullable
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if(request.getUrl().getScheme().equalsIgnoreCase("file")){
                            String reqPath = request.getUrl().getPath().replace(expoCacheDir.getAbsolutePath(), "");
                            if(reqPath.startsWith("/data/exhibitors/")){
                                String reqUrl = baseUrl + reqPath;
                                Helper.updateFile(reqUrl, new File(request.getUrl().getPath()));
                            }
                        }

                        return super.shouldInterceptRequest(view, request);
                    }

                    @Nullable
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        locker.lock();

                        loaded.set(true);
                        if(cached.get() && loaded.get()) {
                            initFloorplan();
                        }

                        locker.unlock();
                    }

                    @Nullable
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                        return true;
                    }
                });
                webView.setWebChromeClient(new WebChromeClient());
                webView.loadUrl("file:///" + indexFilePath.getAbsolutePath() + finalParams);
            });

            Helper.updateCache(expoCacheDir, baseUrl, () -> {
                locker.lock();

                cached.set(true);
                if(cached.get() && loaded.get()){
                    webView.post(() -> {
                        initFloorplan();
                    });
                }

                locker.unlock();
            });
        }
        else {
            webView.post(() -> {
                webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                webView.setWebViewClient(new WebViewClient(){
                    @Nullable
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        initFloorplan();
                    }

                    @Nullable
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
                        try {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                        return true;
                    }
                });
                webView.setWebChromeClient(new WebChromeClient());
                webView.loadUrl("file:///" + indexFilePath.getAbsolutePath() + finalParams);
            });
        }
    }

    /**
     * Select booth
     *
     * @param boothName Booth name or externalID
     */
    public void selectBooth(String boothName) {
        webView.post(() -> {
            String js = String.format("selectBooth('%s')", boothName);
            webView.evaluateJavascript(js, null);
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
        webView.post(() -> {
            String js = String.format("setCurrentPosition(%d, %d, %b)", x, y, focus);
            webView.evaluateJavascript(js, null);
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
        webView.post(() -> {
            String js = String.format("selectRoute('%s', '%s', %b)", from, to, exceptInaccessible);
            webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Clear floor plan
     */
    public void clear() {
        webView.post(() -> {
            String js = String.format("selectRoute(null, null, false)");
            webView.evaluateJavascript(js, null);

            js = String.format("setCurrentPosition(null, null, false)");
            webView.evaluateJavascript(js, null);
        });
    }

    private void initFloorplan() {
        String js = String.format("init()");
        webView.evaluateJavascript(js, null);
    }

    private String getEventId(String url) {
        String baseUrl = url.replace("http://", "")
                .replace("https://", "").replace("www.", "");

        String eventId = baseUrl.substring(0, baseUrl.indexOf('.'));
        return eventId;
    }

    private void initView(Context context) {
        webView = new WebView(this.getContext());
        connectivityManager = (ConnectivityManager) this.getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT | Gravity.BOTTOM | Gravity.LEFT;

        webView.setLayoutParams(layoutParams);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);

        webView.getSettings().setAppCacheEnabled(true);
        File dir = context.getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        webView.getSettings().setAppCachePath(dir.getPath());
        webView.getSettings().setDatabaseEnabled(true);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void postMessage(String message) {
                if (eventListener != null) {
                    eventListener.onFpConfigured();
                }
            }
        }, "onFpConfiguredHandler");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void postMessage(String boothName) {
                if (eventListener != null) {
                    eventListener.onBoothSelected(boothName);
                }
            }
        }, "onBoothClickHandler");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void postMessage(String directionJson) throws JSONException {
                if (eventListener != null) {
                    try {
                        Route route = Helper.parseRoute(directionJson);
                        eventListener.onRouteCreated(route);
                    } catch (JSONException e) {
                        throw e;
                    }
                }
            }
        }, "onDirectionHandler");

        this.addView(webView);
    }
}