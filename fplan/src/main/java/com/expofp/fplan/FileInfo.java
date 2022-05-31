package com.expofp.fplan;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information about the file to be cached
 */
public class FileInfo {
    private final String name;
    private final String serverUrl;
    private final String cachePath;
    private final String version;

    /**
     * Constructor
     * @param name      File name
     * @param serverUrl URL address of the file on the server
     * @param cachePath The path to the file in the cache
     * @param version   File version
     */
    public FileInfo(String name, String serverUrl, String cachePath, String version) {
        this.name = name;
        this.serverUrl = serverUrl;
        this.cachePath = cachePath;
        this.version = version;
    }

    /**
     * Returns the file name
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the URL address of the file on the server
     * @return String
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Returns the path to the file in the cache
     * @return String
     */
    public String getCachePath() {
        return cachePath;
    }

    /**
     * Returns the file version
     * @return String
     */
    public String getVersion() {
        return version;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("serverUrl", serverUrl);
        jsonObject.put("cachePath", cachePath);
        jsonObject.put("version", version);
        return jsonObject;
    }

    public String toJson() throws JSONException {
        JSONObject jsonObject = toJsonObject();
        return jsonObject.toString();
    }

    public static FileInfo parseJson(JSONObject jObject) throws JSONException {
        String name = jObject.has("name") ? jObject.getString("name") : null;
        String serverUrl = jObject.has("serverUrl") ? jObject.getString("serverUrl") : null;
        String cachePath = jObject.has("cachePath") ? jObject.getString("cachePath") : null;
        String version = jObject.has("version") ? jObject.getString("version") : null;
        FileInfo fileInfo = new FileInfo(name, serverUrl, cachePath, version);
        return fileInfo;
    }
}
