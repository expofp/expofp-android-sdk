package com.expofp.fplan;

public class RouteLine {
    private RoutePoint startPoint;
    private RoutePoint endPoint;
    private int weight;

    public RouteLine(RoutePoint startPoint, RoutePoint endPoint, int weight){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.weight = weight;
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
