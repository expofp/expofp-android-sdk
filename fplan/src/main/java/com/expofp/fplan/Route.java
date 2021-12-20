package com.expofp.fplan;

import java.time.Duration;

public class Route {
    private String distance; //свойство
    private Duration time; //свойство

    public String getDistance() { //геттер
        return this.distance;
    }

    public void setDistance(String distance) { //сеттер
        this.distance = distance;
    }

    public Duration getDuration() { //геттер
        return this.time;
    }

    public void setDuration(Duration time) { //сеттер
        this.time = time;
    }
}
