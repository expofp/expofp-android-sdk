package com.expofp.fplan;

import org.json.JSONException;
import org.json.JSONObject;

public class RoutePoint {
    private int x;
    private int y;

    public RoutePoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public static RoutePoint parseJson(JSONObject jObject) throws JSONException {
        RoutePoint point = new RoutePoint(jObject.getInt("x"), jObject.getInt("y"));
        return point;
    }
}
