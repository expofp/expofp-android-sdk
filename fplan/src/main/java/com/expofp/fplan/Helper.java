package com.expofp.fplan;

import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Helper {

    public static final Map<String, String> cachingFiles = new HashMap<String, String>();

    static {
        cachingFiles.put("data/fp.svg.js", "data/fp.svg.js");
        cachingFiles.put("data/wf.data.js", "data/wf.data.js");
        cachingFiles.put("data/data.js", "data/data.js");
        cachingFiles.put("data/demo.png", "data/demo.png");

        cachingFiles.put("vendors~floorplan.js", "packages/master/vendors~floorplan.js");
        cachingFiles.put("floorplan.js", "packages/master/floorplan.js");
        cachingFiles.put("expofp.js", "packages/master/expofp.js");
        cachingFiles.put("free.js", "packages/master/free.js");
        cachingFiles.put("slider.js", "packages/master/slider.js");
        cachingFiles.put("expofp-overlay.png", "packages/master/expofp-overlay.png");

        cachingFiles.put("fonts/oswald-v17-cyrillic_latin-300.woff2", "packages/master/fonts/oswald-v17-cyrillic_latin-300.woff2");
        cachingFiles.put("fonts/oswald-v17-cyrillic_latin-500.woff2", "packages/master/fonts/oswald-v17-cyrillic_latin-500.woff2");

        cachingFiles.put("vendor/fa/css/fontawesome-all.min.css", "packages/master/vendor/fa/css/fontawesome-all.min.css");
        cachingFiles.put("vendor/fa/webfonts/fa-brands-400.woff2", "packages/master/vendor/fa/webfonts/fa-brands-400.woff2");
        cachingFiles.put("vendor/fa/webfonts/fa-light-300.woff2", "packages/master/vendor/fa/webfonts/fa-light-300.woff2");
        cachingFiles.put("vendor/fa/webfonts/fa-regular-400.woff2", "packages/master/vendor/fa/webfonts/fa-regular-400.woff2");
        cachingFiles.put("vendor/fa/webfonts/fa-solid-900.woff2", "packages/master/vendor/fa/webfonts/fa-solid-900.woff2");
        cachingFiles.put("vendor/perfect-scrollbar/css/perfect-scrollbar.css", "packages/master/vendor/perfect-scrollbar/css/perfect-scrollbar.css");
        cachingFiles.put("vendor/sanitize-css/sanitize.css", "packages/master/vendor/sanitize-css/sanitize.css");

        cachingFiles.put("locales/ar.json", "packages/master/locales/ar.json");
        cachingFiles.put("locales/de.json", "packages/master/locales/de.json");
        cachingFiles.put("locales/es.json", "packages/master/locales/es.json");
        cachingFiles.put("locales/fr.json", "packages/master/locales/fr.json");
        cachingFiles.put("locales/it.json", "packages/master/locales/it.json");
        cachingFiles.put("locales/ko.json", "packages/master/locales/ko.json");
        cachingFiles.put("locales/nl.json", "packages/master/locales/nl.json");
        cachingFiles.put("locales/pt.json", "packages/master/locales/pt.json");
        cachingFiles.put("locales/ru.json", "packages/master/locales/ru.json");
        cachingFiles.put("locales/sv.json", "packages/master/locales/sv.json");
        cachingFiles.put("locales/th.json", "packages/master/locales/th.json");
        cachingFiles.put("locales/tr.json", "packages/master/locales/tr.json");
        cachingFiles.put("locales/vi.json", "packages/master/locales/vi.json");
        cachingFiles.put("locales/zh.json", "packages/master/locales/zh.json");
    }

    public static void updateCache(File expoCacheDir, String baseUrl, Runnable callback) {
        ReentrantLock locker = new ReentrantLock();
        AtomicInteger count = new AtomicInteger();

        for (String key : cachingFiles.keySet()) {
            new Thread(() -> {
                updateFile(baseUrl + "/" + cachingFiles.get(key), new File(expoCacheDir, key));

                locker.lock();
                count.getAndIncrement();
                if (count.get() == cachingFiles.size()) {
                    callback.run();
                    count.set(0);
                }
                locker.unlock();

            }).start();
        }
    }

    public static void updateFile(String url, File filePath) {
        try {
            if (!filePath.exists()) {
                filePath.getParentFile().mkdirs();
                filePath.createNewFile();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                InputStream in = new URL(url).openStream();
                Files.copy(in, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            else {
                saveFile(url, filePath);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(File file, byte[] data) throws IOException {
        if (!file.exists()) {
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

    public static String readFile(String filePath) throws Exception {
        File file = new File(filePath);
        FileInputStream fiStream = new FileInputStream(file);
        String result = convertStreamToString(fiStream);
        fiStream.close();
        return result;
    }

    public static Route parseRoute(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            RouteBooth from =  parseRouteBooth(jObject.getJSONObject("from"));
            RouteBooth to = parseRouteBooth(jObject.getJSONObject("to"));
            RouteLine[] lines = parseRouteLines(jObject.getJSONArray("lines"));
            Route route = new Route(from, to, jObject.getString("distance"), jObject.getInt("time"), lines);
            return route;
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean removeDirectory(File directory) {
        if (directory == null)
            return false;
        if (!directory.exists())
            return true;
        if (!directory.isDirectory())
            return false;

        String[] list = directory.list();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(directory, list[i]);
                if (entry.isDirectory())
                {
                    if (!removeDirectory(entry))
                        return false;
                }
                else
                {
                    if (!entry.delete())
                        return false;
                }
            }
        }

        return directory.delete();
    }

    private static RouteBooth parseRouteBooth(JSONObject jObject) throws JSONException {
        RouteBooth routeBooth = new RouteBooth(jObject.getString("id"), jObject.getString("name"));
        return routeBooth;
    }

    private static RouteLine[] parseRouteLines(JSONArray array) throws JSONException {
        RouteLine[] lines = new RouteLine[array.length()];

        for (int i = 0; i < lines.length; i++) {
            lines[i] = parseRouteLine(array.getJSONObject(i));
        }

        return lines;
    }

    private static RouteLine parseRouteLine(JSONObject jObject) throws JSONException {
        RoutePoint startPoint = parseRoutePoint(jObject.getJSONObject("p0"));
        RoutePoint endPoint = parseRoutePoint(jObject.getJSONObject("p1"));

        int weight = jObject.has("weight") ? jObject.getInt("weight") : 0;
        boolean ended = jObject.has("ended") ? jObject.getBoolean("ended") : false;

        RouteLine line = new RouteLine(startPoint, endPoint, weight, ended);
        return line;
    }

    private static RoutePoint parseRoutePoint(JSONObject jObject) throws JSONException {
        RoutePoint point = new RoutePoint(jObject.getInt("x"), jObject.getInt("y"));
        return point;
    }

    private static void saveFile(String uri, File filePath) {
        URL url = null;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        InputStream input = null;
        OutputStream output = null;
        try {
            input = new BufferedInputStream(url.openStream(), 8192);
            output = new FileOutputStream(filePath);

            byte data[] = new byte[1024];

            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

        } catch (Exception ex) {

        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception exc) { }
        }
    }
}
