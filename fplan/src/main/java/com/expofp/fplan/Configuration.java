package com.expofp.fplan;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fplan configuration
 */
public class Configuration {
    private final Boolean noOverlay;
    private final @Nullable String androidHtmlUrl;
    private final @Nullable String iosHtmlUrl;
    private final FileInfo[] files;

    /**
     * Constructor
     * @param noOverlay         True - Hides the panel with information about exhibitors
     * @param androidHtmlUrl    URL index.html for Android version
     * @param iosHtmlUrl        URL index.html for iOS version
     * @param files             Array of cached files
     */
    public Configuration(Boolean noOverlay, @Nullable String androidHtmlUrl, @Nullable String iosHtmlUrl, FileInfo[] files) {
        this.noOverlay = noOverlay;
        this.androidHtmlUrl = androidHtmlUrl;
        this.iosHtmlUrl = iosHtmlUrl;
        this.files = files;
    }

    /**
     * Returns the value of the parameter noOverlay
     * @return Boolean
     */
    public Boolean getNoOverlay() {
        return noOverlay;
    }

    /**
     * Returns an array of cached files
     * @return FileInfo array
     */
    public FileInfo[] getFiles() {
        return files;
    }

    /**
     * Returns an URL index.html for Android
     * @return String
     */
    public String getAndroidHtmlUrl(){
        return androidHtmlUrl;
    }

    /**
     * Returns an URL index.html for iOS
     * @return String
     */
    public String getIosHtmlUrl(){
        return iosHtmlUrl;
    }

    public static Configuration parseJson(String json) throws JSONException {
        JSONObject jObject = new JSONObject(json);
        boolean noOverlay = jObject.has("noOverlay") ? jObject.getBoolean("noOverlay") : true;
        String androidHtmlUrl = jObject.has("androidHtmlUrl") ? jObject.getString("androidHtmlUrl") : null;
        String iosHtmlUrl = jObject.has("iosHtmlUrl") ? jObject.getString("iosHtmlUrl") : null;
        FileInfo[] files = jObject.has("files") ? parseFiles(jObject.getJSONArray("files")) : null;

        Configuration configuration = new Configuration(noOverlay, androidHtmlUrl, iosHtmlUrl, files);
        return configuration;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("noOverlay", noOverlay);
        jsonObject.put("androidHtmlUrl", androidHtmlUrl);
        jsonObject.put("iosHtmlUrl", iosHtmlUrl);

        if (files != null) {
            JSONArray array = new JSONArray();
            for (FileInfo file : files) {
                array.put(file.toJsonObject());
            }

            jsonObject.put("files", array);
        }

        return jsonObject;
    }

    public String toJson() throws JSONException {
        JSONObject jsonObject = toJsonObject();
        return jsonObject.toString();
    }

    private static FileInfo[] parseFiles(JSONArray array) throws JSONException {
        FileInfo[] files = new FileInfo[array.length()];

        for (int i = 0; i < files.length; i++) {
            files[i] = FileInfo.parseJson(array.getJSONObject(i));
        }

        return files;
    }
}
