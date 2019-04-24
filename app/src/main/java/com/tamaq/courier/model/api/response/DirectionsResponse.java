package com.tamaq.courier.model.api.response;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class DirectionsResponse {

    public String status;
    public List<Route> routes;

    public class Route {
        @SerializedName("overview_polyline")
        public Polyline overviewPolyline;
        public List<Leg> legs;

        public String getFullPolyline() {
            List<LatLng> allPoints = new ArrayList<>();
            for (DirectionsResponse.Leg leg : legs) {
                for (DirectionsResponse.Step step : leg.steps) {
                    allPoints.addAll(PolyUtil.decode(step.polyline.points));
                }
            }
            return PolyUtil.encode(allPoints);
        }
    }

    public class Polyline {
        public String points;
    }

    public class Leg {
        public List<Step> steps;
        public Distance distance;
    }

    public class Step {
        @SerializedName("start_location")
        public Location startLocation;
        @SerializedName("end_location")
        public Location endLocation;
        public Polyline polyline;
        public Distance distance;
    }

    public class Location {
        public double lat;
        public double lng;
    }

    public class Distance {
        public long value; // in meters
    }
}
