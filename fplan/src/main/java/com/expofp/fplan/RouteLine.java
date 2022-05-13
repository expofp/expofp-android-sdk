package com.expofp.fplan;

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
}
