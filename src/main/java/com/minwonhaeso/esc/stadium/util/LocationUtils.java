package com.minwonhaeso.esc.stadium.util;

import lombok.Builder;
import lombok.Data;

public class LocationUtils {
    @Data
    @Builder
    public static class Location {
        private Double lat;
        private Double lnt;

        public Double getDistance(Double toLat, Double toLnt) {
            double theta = toLnt - this.lnt;
            double dist = Math.sin(deg2rad(toLat)) * Math.sin(deg2rad(this.lat))
                    + Math.cos(deg2rad(toLat)) * Math.cos(deg2rad(this.lat)) * Math.cos(deg2rad(theta));
            dist = rad2deg(Math.acos(dist)) * 111.189557;
            return Math.round(dist * 10000) / 10000.0;
        }

        // This function converts decimal degrees to radians
        private Double deg2rad(Double deg) {
            return (deg * Math.PI / 180.0);
        }

        // This function converts radians to decimal degrees
        private Double rad2deg(Double rad) {
            return (rad * 180 / Math.PI);
        }
    }
}
