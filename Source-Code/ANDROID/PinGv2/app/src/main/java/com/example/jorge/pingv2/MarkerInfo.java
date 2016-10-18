package com.example.jorge.pingv2;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jorge on 10/13/2016.
 */

public class MarkerInfo {
    private LatLng eventLocation;
    private String  eventName,
                    eventTime,
                    eventDesc;

    public LatLng getEventLocation() {
        return eventLocation;
    }

    public String getEventDesc() {
        return eventDesc;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventTime() {
        return eventTime;
    }
}
