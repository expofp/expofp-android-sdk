package com.expofp.fplan;

import org.json.JSONException;
import org.json.JSONObject;

public class RouteBooth {
    private String id;
    private String name;

    public RouteBooth(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static RouteBooth parseJson(JSONObject jObject) throws JSONException {
        RouteBooth routeBooth = new RouteBooth(jObject.getString("id"), jObject.getString("name"));
        return routeBooth;
    }
}
