package com.expofp.fplan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;

public class Route {
    private String distance;
    private int time;
    private RouteBooth from;
    private RouteBooth to;
    private RouteLine[] lines;

    public String getDistance() {
        return this.distance;
    }

    public int getTime() {
        return this.time;
    }

    public RouteBooth getBoothFrom() {
        return this.from;
    }

    public RouteBooth getBoothTo() {
        return this.to;
    }

    public RouteLine[] getLines() {
        return this.lines;
    }

    public Route(RouteBooth from, RouteBooth to, String distance, int time, RouteLine[] lines){
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.time = time;
        this.lines = lines;
    }

    public static Route parseJson(String json) throws JSONException {
        JSONObject jObject = new JSONObject(json);
        RouteBooth from = RouteBooth.parseJson(jObject.getJSONObject("from"));
        RouteBooth to = RouteBooth.parseJson(jObject.getJSONObject("to"));
        RouteLine[] lines = parseLines(jObject.getJSONArray("lines"));
        Route route = new Route(from, to, jObject.getString("distance"), jObject.getInt("time"), lines);
        return route;
    }

    private static RouteLine[] parseLines(JSONArray array) throws JSONException {
        RouteLine[] lines = new RouteLine[array.length()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = RouteLine.parseJson(array.getJSONObject(i));
        }
        return lines;
    }
}
