package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

/**
 * Created by yy on 2017-10-17.
 */

public class Circle {
    public float radius;
    public Location midpoint;

    public Circle(float radius, float longitude, float latitude){
        this.radius = radius;
        Location loc = new Location(" ");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        this.midpoint = loc;

    }
    public Location get_midpoint(){
        return midpoint;
    }
    public float get_radius(){
        return radius;
    }
}
