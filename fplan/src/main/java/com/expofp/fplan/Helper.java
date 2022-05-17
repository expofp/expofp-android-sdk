package com.expofp.fplan;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Helper {

    public static Configuration getDefaultConfiguration(String expoUrl, boolean noOverlay){
        List<FileInfo> files = new ArrayList<FileInfo>();

        files.add(new FileInfo("fp.svg.js", expoUrl + "/" + "data/fp.svg.js" , "data/fp.svg.js", "1"));
        files.add(new FileInfo("wf.data.js", expoUrl + "/" + "data/wf.data.js" , "data/wf.data.js", "1"));
        files.add(new FileInfo("data.js", expoUrl + "/" + "data/data.js" , "data/data.js", "1"));
        files.add(new FileInfo("demo.png", expoUrl + "/" + "data/demo.png" , "data/demo.png", "1"));

        files.add(new FileInfo("vendors~floorplan.js", expoUrl + "/" + "packages/master/vendors~floorplan.js" , "vendors~floorplan.js", "1"));
        files.add(new FileInfo("floorplan.js", expoUrl + "/" + "packages/master/floorplan.js" , "floorplan.js", "1"));
        files.add(new FileInfo("expofp.js", expoUrl + "/" + "packages/master/expofp.js" , "expofp.js", "1"));
        files.add(new FileInfo("free.js", expoUrl + "/" + "packages/master/free.js" , "free.js", "1"));
        files.add(new FileInfo("slider.js", expoUrl + "/" + "packages/master/slider.js" , "slider.js", "1"));
        files.add(new FileInfo("expofp-overlay.png", expoUrl + "/" + "packages/master/expofp-overlay.png" , "expofp-overlay.png", "1"));

        files.add(new FileInfo("oswald-v17-cyrillic_latin-300.woff2", expoUrl + "/" + "packages/master/fonts/oswald-v17-cyrillic_latin-300.woff" , "fonts/oswald-v17-cyrillic_latin-300.woff2", "1"));
        files.add(new FileInfo("oswald-v17-cyrillic_latin-500.woff2", expoUrl + "/" + "packages/master/fonts/oswald-v17-cyrillic_latin-500.woff2" , "fonts/oswald-v17-cyrillic_latin-500.woff2", "1"));

        files.add(new FileInfo("fontawesome-all.min.css", expoUrl + "/" + "packages/master/vendor/fa/css/fontawesome-all.min.css" , "vendor/fa/css/fontawesome-all.min.css", "1"));
        files.add(new FileInfo("fa-brands-400.woff2", expoUrl + "/" + "packages/master/vendor/fa/webfonts/fa-brands-400.woff2" , "vendor/fa/webfonts/fa-brands-400.woff2", "1"));
        files.add(new FileInfo("fa-light-300.woff2", expoUrl + "/" + "packages/master/vendor/fa/webfonts/fa-light-300.woff2" , "vendor/fa/webfonts/fa-light-300.woff2", "1"));
        files.add(new FileInfo("fa-regular-400.woff2", expoUrl + "/" + "packages/master/vendor/fa/webfonts/fa-regular-400.woff2" , "vendor/fa/webfonts/fa-regular-400.woff2", "1"));
        files.add(new FileInfo("fa-solid-900.woff2", expoUrl + "/" + "packages/master/vendor/fa/webfonts/fa-solid-900.woff2" , "vendor/fa/webfonts/fa-solid-900.woff2", "1"));
        files.add(new FileInfo("perfect-scrollbar.css", expoUrl + "/" + "packages/master/vendor/perfect-scrollbar/css/perfect-scrollbar.css" , "vendor/perfect-scrollbar/css/perfect-scrollbar.css", "1"));
        files.add(new FileInfo("sanitize.css", expoUrl + "/" + "packages/master/vendor/sanitize-css/sanitize.css" , "vendor/sanitize-css/sanitize.css", "1"));

        files.add(new FileInfo("ar.json", expoUrl + "/" + "packages/master/locales/ar.json" , "locales/ar.json", "1"));
        files.add(new FileInfo("de.json", expoUrl + "/" + "packages/master/locales/de.json" , "locales/de.json", "1"));
        files.add(new FileInfo("es.json", expoUrl + "/" + "packages/master/locales/es.json" , "locales/es.json", "1"));
        files.add(new FileInfo("fr.json", expoUrl + "/" + "packages/master/locales/fr.json" , "locales/fr.json", "1"));
        files.add(new FileInfo("it.json", expoUrl + "/" + "packages/master/locales/it.json" , "locales/it.json", "1"));
        files.add(new FileInfo("ko.json", expoUrl + "/" + "packages/master/locales/ko.json" , "locales/ko.json", "1"));
        files.add(new FileInfo("nl.json", expoUrl + "/" + "packages/master/locales/nl.json" , "locales/nl.json", "1"));
        files.add(new FileInfo("pt.json", expoUrl + "/" + "packages/master/locales/pt.json" , "locales/pt.json", "1"));
        files.add(new FileInfo("ru.json", expoUrl + "/" + "packages/master/locales/ru.json" , "locales/ru.json", "1"));
        files.add(new FileInfo("sv.json", expoUrl + "/" + "packages/master/locales/sv.json" , "locales/sv.json", "1"));
        files.add(new FileInfo("th.json", expoUrl + "/" + "packages/master/locales/th.json" , "locales/th.json", "1"));
        files.add(new FileInfo("tr.json", expoUrl + "/" + "packages/master/locales/tr.json" , "locales/tr.json", "1"));
        files.add(new FileInfo("vi.json", expoUrl + "/" + "packages/master/locales/vi.json" , "locales/vi.json", "1"));
        files.add(new FileInfo("zh.json", expoUrl + "/" + "packages/master/locales/zh.json" , "locales/zh.json", "1"));

        return new Configuration(noOverlay, null, null, files.toArray(files.toArray(new FileInfo[0])));
    }

    public static boolean containFile(String cacheFilePath, FileInfo[] files) {
        return Arrays.stream(files).anyMatch(p -> p.getCachePath().equalsIgnoreCase(cacheFilePath));
    }

    public static Thread[] downloadFiles(File expoCacheDir, FileInfo[] files, Runnable callback) {
        List<Thread> threads = new ArrayList<>();

        ReentrantLock locker = new ReentrantLock();
        AtomicInteger count = new AtomicInteger();
        Thread thread;

        for (FileInfo file : files) {
            thread = new Thread(() -> {
                downloadFile(file.getServerUrl(), new File(expoCacheDir, file.getCachePath()));

                locker.lock();
                count.getAndIncrement();
                if (count.get() == files.length) {
                    callback.run();
                    count.set(0);
                }
                locker.unlock();

            });
            threads.add(thread);
            thread.start();
        }

        return threads.toArray(new Thread[0]);
    }

    public static void downloadFile(String url, File filePath) {
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
                downloadFileLegacy(url, filePath);
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
        return readFile(file);
    }

    public static String readFile(File file) throws Exception {
        FileInputStream fiStream = new FileInputStream(file);
        String result = convertStreamToString(fiStream);
        fiStream.close();
        return result;
    }

    public static String httpGet(URL url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder result = new StringBuilder();

        String buffer;
        while ((buffer = in.readLine()) != null)
            result.append(buffer + "\n");

        in.close();
        return  result.toString();
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

    private static void downloadFileLegacy(String uri, File filePath) {
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
