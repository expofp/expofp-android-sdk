package com.expofp.fplan;

import org.json.JSONException;
import org.json.JSONObject;

public class RouteLine {
    private RoutePoint startPoint;
    private RoutePoint endPoint;
    private int weight;
    private boolean ended;

    public RouteLine(RoutePoint startPoint, RoutePoint endPoint, int weight, boolean ended){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.weight = weight;
        this.ended = ended;
    }

    public RoutePoint getStartPoint() {
        return this.startPoint;
    }

    public RoutePoint getEndPoint() {
        return this.endPoint;
    }

    public int getWeight() {
        return this.weight;
    }

    public static RouteLine parseJson(JSONObject jObject) throws JSONException {
        RoutePoint startPoint = RoutePoint.parseJson(jObject.getJSONObject("p0"));
        RoutePoint endPoint = RoutePoint.parseJson(jObject.getJSONObject("p1"));

        int weight = jObject.has("weight") ? jObject.getInt("weight") : 0;
        boolean ended = jObject.has("ended") ? jObject.getBoolean("ended") : false;

        RouteLine line = new RouteLine(startPoint, endPoint, weight, ended);
        return line;
    }
}
