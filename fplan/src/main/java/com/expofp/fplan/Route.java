package com.expofp.fplan;

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
}
