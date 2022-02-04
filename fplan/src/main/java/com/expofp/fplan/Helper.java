package com.expofp.fplan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;

public class Helper {
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

        for (int i = 0; i < 10; i++){
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
