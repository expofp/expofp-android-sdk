package com.expofp.fplan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Helper {

    private static final Map<String, String> cachingFiles = new HashMap<String, String>();

    static {
        cachingFiles.put("data/fp.svg.js", "data/fp.svg.js");
        cachingFiles.put("data/wf.data.js", "data/wf.data.js");
        cachingFiles.put("data/data.js", "data/data.js");
        cachingFiles.put("data/demo.png", "data/demo.png");

        cachingFiles.put("packages/master/vendors~floorplan.js", "vendors~floorplan.js");
        cachingFiles.put("packages/master/floorplan.js", "floorplan.js");
        cachingFiles.put("packages/master/expofp.js", "expofp.js");
        cachingFiles.put("packages/master/free.js", "free.js");
        cachingFiles.put("packages/master/slider.js", "slider.js");
        cachingFiles.put("packages/master/expofp-overlay.png", "expofp-overlay.png");

        cachingFiles.put("packages/master/fonts/oswald-v17-cyrillic_latin-300.woff2", "fonts/oswald-v17-cyrillic_latin-300.woff2");
        cachingFiles.put("packages/master/fonts/oswald-v17-cyrillic_latin-500.woff2", "fonts/oswald-v17-cyrillic_latin-500.woff2");

        cachingFiles.put("packages/master/vendor/fa/css/fontawesome-all.min.css", "vendor/fa/css/fontawesome-all.min.css");
        cachingFiles.put("packages/master/vendor/fa/webfonts/fa-brands-400.woff2", "vendor/fa/webfonts/fa-brands-400.woff2");
        cachingFiles.put("packages/master/vendor/fa/webfonts/fa-light-300.woff2", "vendor/fa/webfonts/fa-light-300.woff2");
        cachingFiles.put("packages/master/vendor/fa/webfonts/fa-regular-400.woff2", "vendor/fa/webfonts/fa-regular-400.woff2");
        cachingFiles.put("packages/master/vendor/fa/webfonts/fa-solid-900.woff2", "vendor/fa/webfonts/fa-solid-900.woff2");
        cachingFiles.put("packages/master/vendor/perfect-scrollbar/css/perfect-scrollbar.css", "vendor/perfect-scrollbar/css/perfect-scrollbar.css");
        cachingFiles.put("packages/master/vendor/sanitize-css/sanitize.css", "vendor/sanitize-css/sanitize.css");

        cachingFiles.put("packages/master/locales/ar.json", "locales/ar.json");
        cachingFiles.put("packages/master/locales/de.json", "locales/de.json");
        cachingFiles.put("packages/master/locales/es.json", "locales/es.json");
        cachingFiles.put("packages/master/locales/fr.json", "locales/fr.json");
        cachingFiles.put("packages/master/locales/it.json", "locales/it.json");
        cachingFiles.put("packages/master/locales/ko.json", "locales/ko.json");
        cachingFiles.put("packages/master/locales/nl.json", "locales/nl.json");
        cachingFiles.put("packages/master/locales/pt.json", "locales/pt.json");
        cachingFiles.put("packages/master/locales/ru.json", "locales/ru.json");
        cachingFiles.put("packages/master/locales/sv.json", "locales/sv.json");
        cachingFiles.put("packages/master/locales/th.json", "locales/th.json");
        cachingFiles.put("packages/master/locales/tr.json", "locales/tr.json");
        cachingFiles.put("packages/master/locales/vi.json", "locales/vi.json");
        cachingFiles.put("packages/master/locales/zh.json", "locales/zh.json");
    }

    public static void updateCache(File expoCacheDir, String baseUrl, Runnable callback) {
        ReentrantLock locker = new ReentrantLock();
        AtomicInteger count = new AtomicInteger();

        for (String key : cachingFiles.keySet()) {
            new Thread(() -> {
                updateFile(baseUrl + "/" + key, new File(expoCacheDir, cachingFiles.get(key)));

                locker.lock();
                count.getAndIncrement();
                if(count.get() == cachingFiles.size()){
                    callback.run();
                    count.set(0);
                }
                locker.unlock();

            }).start();
        }
    }

    public static void updateFile(String url, File filePath) {
        try {
            if(!filePath.exists()) {
                filePath.getParentFile().mkdirs();
                filePath.createNewFile();
            }

            InputStream in = new URL(url).openStream();
            Files.copy(in, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(File file, byte[] data) throws IOException {
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileOutputStream fs = new FileOutputStream(file);
        fs.write(data);
        fs.close();
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String readFile (String filePath) throws Exception {
        File file = new File(filePath);
        FileInputStream fiStream = new FileInputStream(file);
        String result = convertStreamToString(fiStream);
        fiStream.close();
        return result;
    }

    public static Route parseRoute(String json) throws JSONException {
        JSONObject jObject = new JSONObject(json);

        RouteBooth from = parseRouteBooth(jObject.getJSONObject("from"));
        RouteBooth to = parseRouteBooth(jObject.getJSONObject("to"));
        RouteLine[] lines = parseRouteLines(jObject.getJSONArray("lines"));

        Route route = new Route(from, to, jObject.getString("distance"), Duration.ofSeconds(jObject.getInt("time")), lines);
        return route;
    }

    private static RouteBooth parseRouteBooth(JSONObject jObject) throws JSONException {
        RouteBooth routeBooth = new RouteBooth(jObject.getString("id"), jObject.getString("name"));
        return routeBooth;
    }

    private static RouteLine[] parseRouteLines(JSONArray array) throws JSONException {
        RouteLine[] lines = new RouteLine[array.length()];

        for (int i = 0; i < lines.length; i++){
            lines[i] = parseRouteLine(array.getJSONObject(i));
        }

        return lines;
    }

    private static RouteLine parseRouteLine(JSONObject jObject) throws JSONException {
        RoutePoint startPoint = parseRoutePoint(jObject.getJSONObject("p0"));
        RoutePoint endPoint = parseRoutePoint(jObject.getJSONObject("p1"));
        RouteLine line = new RouteLine(startPoint, endPoint, jObject.getInt("weight"));
        return line;
    }

    private static RoutePoint parseRoutePoint(JSONObject jObject) throws JSONException {
        RoutePoint point = new RoutePoint(jObject.getInt("x"), jObject.getInt("y"));
        return point;
    }
}
