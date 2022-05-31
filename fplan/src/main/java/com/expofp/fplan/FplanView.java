package com.expofp.fplan;

import static android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.GeolocationPermissions;
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
import java.net.URL;
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

    private Thread initThread;
    private Thread[] downloadFileThreads;

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
        this.eventListener = eventListener;

        initThread = new Thread(() -> {
            init(url, true, (Configuration)null);
            initThread = null;
        });
        initThread.start();
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

        initThread = new Thread(() -> {
            init(url, noOverlay, (Configuration)null);
            initThread = null;
        });
        initThread.start();
    }

    /**
     * Initializing a view
     *
     * @param url           Expo plan URL
     * @param noOverlay     True - Hides the panel with information about exhibitors
     * @param configuration Fplan configuration
     * @param eventListener Events listener
     */
    public void init(String url, Boolean noOverlay, @Nullable Configuration configuration, @Nullable FplanEventListener eventListener) {
        this.eventListener = eventListener;

        initThread = new Thread(() -> {
            init(url, noOverlay, configuration);
            initThread = null;
        });

        initThread.start();
    }

    private void init(String url, Boolean noOverlay, @Nullable Configuration configuration){
        String eventId = getEventId(url);
        String baseUrl = getBaseUrl(url);
        String configUrl = baseUrl + "/" + Constants.fplanConfigPath;

        File cacheDir = getContext().getFilesDir();
        File fPlanCacheDir = new File(cacheDir, Constants.fplanDirPath);
        File expoCacheDir = new File(fPlanCacheDir, eventId);
        File indexFilePath = new File(expoCacheDir, "index.html");

        String params = "";
        if (url.contains("?")) {
            params = url.substring(url.indexOf("?"));
        }

        String finalParams = params;
        Context context = this.getContext();

        boolean online = connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();

        if(online){

            Configuration config = configuration != null ? configuration : loadConfiguration(configUrl, baseUrl);
            if (fPlanCacheDir.exists()) {
                Helper.removeDirectory(fPlanCacheDir);
            }

            createHtmlFile(config, eventId, noOverlay, expoCacheDir, indexFilePath);

            initOnlineMode(config, expoCacheDir, indexFilePath, baseUrl, finalParams, context);
        }
        else {
            initOfflineMode(indexFilePath, finalParams, context);
        }
    }

    /**
     * Stop FplanView
     */
    public void stop() {
        if (initThread != null && initThread.isAlive()) {
            try {
                initThread.interrupt();
            }
            catch (Exception ex) {
            }
        }

        if (downloadFileThreads != null && downloadFileThreads.length > 0) {
            for (Thread thread : downloadFileThreads) {
                if (thread != null && thread.isAlive()) {
                    try {
                        thread.interrupt();
                    }
                    catch (Exception ex) {
                    }
                }
            }
        }
    }

    /**
     * Select booth
     *
     * @param boothName Booth name or externalID
     */
    public void selectBooth(String boothName) {
        webView.post(() -> {
            String js = String.format("window.floorplan?.selectBooth('%s');", boothName);
            webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Deselects the selected booth
     */
    public void clearBooth() {
        selectBooth("");
    }

    /**
     * Set current position(Blue-Dot) on route
     *
     * @param x X
     * @param y Y
     */
    public void setCurrentPosition(int x, int y) {
        setCurrentPosition(x, y, null, null, false);
    }

    /**
     * Set current position(Blue-Dot) on route
     *
     * @param x     X
     * @param y     Y
     * @param z     Floor
     * @param angle Arrow direction
     * @param focus True - focus on a point
     */
    public void setCurrentPosition(int x, int y, @Nullable String z, @Nullable Integer angle, boolean focus) {
        webView.post(() -> {
            String zString = z != null ? "'" + z + "'" : "null";
            String angleString = angle != null ? angle.toString() : "null";
            String js = String.format("window.floorplan?.selectCurrentPosition({ x: %d, y: %d, z: %s, angle: %s }, %b);", x, y, zString, angleString, focus);
            webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Deletes the blue-dot point
     */
    public void clearCurrentPosition() {
        webView.post(() -> {
            String js = String.format("window.floorplan?.selectCurrentPosition(null, false)");
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
            String js = String.format("window.floorplan?.selectRoute('%s', '%s', %b)", from, to, exceptInaccessible);
            webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Deletes the built route
     */
    public void clearRoute() {
        webView.post(() -> {
            String js = String.format("window.floorplan?.selectRoute(null, null, false)");
            webView.evaluateJavascript(js, null);
        });
    }

    /**
     * Clear floor plan
     */
    public void clear() {
        clearBooth();
        clearRoute();
        clearCurrentPosition();
    }

    private Configuration loadConfiguration(String configUrl, String expoUrl) {
        Configuration configuration = null;

        if(configUrl != null && !configUrl.equalsIgnoreCase("")){
            try {
                InputStream in = new URL(configUrl).openStream();
                String json = Helper.convertStreamToString(in);

                configuration = Configuration.parseJson(json);
                Log.d(Constants.fplanLogTag, "Configuration file loaded from " + configUrl);
            }
            catch (Exception ex) { }
        }

        if(configuration == null){
            Log.d(Constants.fplanLogTag, "Failed to load configuration file from " + configUrl + " and from cache. The default configuration file will be loaded");
            configuration = Helper.getDefaultConfiguration(expoUrl, true);
        }

        return configuration;
    }

    private void createHtmlFile(Configuration configuration, String eventId, Boolean noOverlay, File expoCacheDir, File indexFilePath){
        String html = "";
        if(configuration.getAndroidHtmlUrl() != null && !configuration.getAndroidHtmlUrl().equalsIgnoreCase("")){
            try {
                html = Helper.httpGet(new URL(configuration.getAndroidHtmlUrl()));
                Log.d(Constants.fplanLogTag, "Html file loaded from " + configuration.getAndroidHtmlUrl());
            }
            catch (Exception ex){
                html = "";
                Log.d(Constants.fplanLogTag, "Failed to load html file from " + configuration.getAndroidHtmlUrl() + ". The default html file will be loaded");
            }
        }

        if(html == null || html.equalsIgnoreCase("")){
            try {
                InputStream inputStream = this.getContext().getAssets().open("index.html");
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                html = new String(buffer);
                Log.d(Constants.fplanLogTag, "Default html file loaded from Assets");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            html = html.replace("$url#", "file:///" + expoCacheDir.getAbsolutePath())
                    .replace("$eventId#", eventId)
                    .replace("$noOverlay#", noOverlay.toString());
            Helper.writeToFile(indexFilePath, html.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initOnlineMode(Configuration configuration, File expoCacheDir, File indexFilePath, String baseUrl, String params, Context context) {
        ReentrantLock locker = new ReentrantLock();
        AtomicBoolean cached = new AtomicBoolean(false);
        AtomicBoolean loaded = new AtomicBoolean(false);

        webView.post(() -> {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.setWebViewClient(new WebViewClient() {
                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (request.getUrl().getScheme().equalsIgnoreCase("file")) {
                        String reqPath = request.getUrl().getPath().replace(expoCacheDir.getAbsolutePath(), "").substring(1);

                        if(!reqPath.equalsIgnoreCase("index.html") && !Helper.containFile(reqPath, configuration.getFiles())) {
                            String reqUrl = baseUrl + "/" + reqPath;
                            Helper.downloadFile(reqUrl, new File(request.getUrl().getPath()));
                        }
                    }

                    WebResourceResponse response = super.shouldInterceptRequest(view, request);
                    return response;
                }

                @Nullable
                @Override
                public void onPageFinished(WebView view, String url) {
                    locker.lock();

                    loaded.set(true);
                    if (cached.get() && loaded.get()) {
                        initFloorplan();
                    }

                    locker.unlock();
                }

                @Nullable
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
            });

            webView.setWebChromeClient(new WebChromeClient(){
                @Nullable
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, false);
                }
            });

            webView.loadUrl("file:///" + indexFilePath.getAbsolutePath() + params);
        });

        downloadFileThreads = Helper.downloadFiles(expoCacheDir, configuration.getFiles(), () -> {
            locker.lock();

            cached.set(true);
            if (cached.get() && loaded.get()) {
                webView.post(() -> {
                    initFloorplan();
                });
            }

            locker.unlock();

            downloadFileThreads = null;
        });
    }

    private void  initOfflineMode(File indexFilePath, String params, Context context) {
        webView.post(() -> {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);

            webView.setWebViewClient(new WebViewClient() {
                @Nullable
                @Override
                public void onPageFinished(WebView view, String url) {
                    initFloorplan();
                }

                @Nullable
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
            });

            webView.setWebChromeClient(new WebChromeClient(){
                @Nullable
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, false);
                }
            });

            webView.loadUrl("file:///" + indexFilePath.getAbsolutePath() + params);
        });
    }

    private void initFloorplan() {
        String js = String.format("init()");
        webView.evaluateJavascript(js, null);
    }

    private String getBaseUrl(String url) {
        String baseUrl = url.substring(0, url.indexOf("." + Constants.expoFpDomain) + Constants.expoFpDomain.length() + 1);
        return baseUrl;
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
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setMixedContentMode(MIXED_CONTENT_ALWAYS_ALLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        File dir = context.getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        webView.getSettings().setAppCachePath(dir.getPath());

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void callOnFpConfigured(String message) {
                if (eventListener != null) {
                    try {
                        eventListener.onFpConfigured();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
            @JavascriptInterface
            public void callOnBoothClick(String boothName) {
                if (eventListener != null) {
                    try {
                        eventListener.onBoothSelected(boothName);
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            @JavascriptInterface
            public void callOnDirection(String directionJson) throws JSONException {
                if (eventListener != null) {
                    Route route = Route.parseJson(directionJson);
                    if(route != null){
                        eventListener.onRouteCreated(route);
                    }
                }
            }

            @JavascriptInterface
            public String readFile(String filePath) throws Exception {
                String result = null;

                try {
                    result = Helper.readFile(filePath.replace("file:///", ""));
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

                return result;
            }

        }, "fplanView");

        this.addView(webView);
    }
}