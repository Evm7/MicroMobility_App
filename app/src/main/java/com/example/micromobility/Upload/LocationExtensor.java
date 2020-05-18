package com.example.micromobility.Upload;

import android.location.Location;

import com.mapbox.geojson.Point;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class LocationExtensor {

    private ArrayList<ObjLocation> real_locations; // The expanded one
    private List<Point> locations;

    private ArrayList<Float> times, framing_times;  // The original and the expanded
    private float fps;
    private float duration;
    private int frame_number, num_interpolations, final_added;
    LocationCalculator locationCalculator;

    private int looper;


    public List<Point> initializeLocation(List<Point> locations, File videoFile) {
        this.locations = locations;
        File videofile = videoFile;
        String path = videoFile.getAbsolutePath();
        FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
        mmr.setDataSource(String.valueOf(path));
        this.fps = Float.parseFloat(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE));
        this.duration = Float.parseFloat(mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION));
        mmr.release();
        this.locationCalculator = new LocationCalculator();
        this.framing_times = new ArrayList<>();
        this.real_locations = new ArrayList<>();
        this.frame_number = (int) (this.duration / this.fps);


        this.num_interpolations = (int) (this.frame_number/this.locations.size());
        this.final_added = (int) (this.frame_number%this.locations.size());
        relate_file_video();
        return transformTo();
    }

    public float getFps() {
        return fps;
    }

    private List<Point> transformTo(){
        List<Point> finalList = new ArrayList<>();
        for (ObjLocation obj : real_locations){
            finalList.add(Point.fromLngLat(obj.longitude, obj.latitude));
        }
        return finalList;
    }

    private void relate_file_video() {
        // Id for looping over the times of the video but avoiding taking whole loop
        this.looper = 0;

        Point before_value = null;
        // Loops over the times of the location
        int i = 0;
        for (Point point : locations) {
            assign_location(before_value, point);
            before_value = point;
            i += 1;
        }
        // Check if we have covered all the end locations, there may be some frames without being marked
        while (this.looper < this.frame_number) {
            this.real_locations.add(this.looper, new ObjLocation(before_value.latitude(), before_value.longitude(), this.looper));
            this.looper += 1;
        }
    }


    private void assign_location(Point current_location, Point last_location) {
        // We start taking locations into account in the second location update
        if (current_location != null) {
            this.looper += this.num_interpolations;
            interpolate(current_location, last_location, this.num_interpolations);
        }

    }

    private void interpolate(Point current_location, Point last_location, int num) {
        ArrayList<LocationSimple> locs_interpol =this.locationCalculator.interpolateLocations(num,current_location,last_location);
        int i = this.looper - num;
        while (i<this.looper){
            LocationSimple current_loc =locs_interpol.get(i -(this.looper -num));
            this.real_locations.add(i, new ObjLocation(current_loc.latitude, current_loc.longitude, i));
            i +=1;
        }
    }

    public class ObjLocation {
        private Double longitude;
        private Double latitude;
        private Integer id;

        public ObjLocation(Double latitude, Double longitude, Integer id) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.id = id;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude(){
            return longitude;
        }

        public String toString()
        {
            return "Id "+ id+ " : lat " + latitude + "  and lon " + longitude +"\n";
        }

    }

    public class LocationCalculator{
        double radius = 6371;

        public LocationCalculator(){
        }

        //Calculate interpolation Locations
        public ArrayList<LocationSimple> interpolateLocations(int num_interpol, Point start, Point end){
            LocationSimple startPoint = new LocationSimple(start.latitude(), start.longitude());
            LocationSimple endPoint = new LocationSimple(end.latitude(), end.longitude());

            ArrayList<LocationSimple> locations = new ArrayList<>();
            // calculate Distnance between each frame
            Double dist = CalculateDistanceBetweenLocations(startPoint, endPoint);
            Double dist_interpol = dist/num_interpol;

            // Calculate Bearing
            Double bearning = CalculateBearing(startPoint, endPoint);
            for(int n=1; n <= num_interpol; n++) {
                locations.add(CalculateDestinationLocation(startPoint, bearning, n * dist_interpol));
            }
            return locations;
        }

        double DegToRad(double deg) {
            return (deg * Math.PI / 180);
        }

        double RadToDeg(double rad) {
            return (rad * 180 / Math.PI);
        }

        double CalculateBearing(LocationSimple startPoint, LocationSimple endPoint) {
            double lat1 = DegToRad(startPoint.latitude);
            double lat2 = DegToRad(endPoint.latitude);
            double deltaLon = DegToRad(endPoint.longitude - startPoint.longitude);
            double y = Math.sin(deltaLon) * Math.cos(lat2);
            double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
            double bearing = Math.atan2(y, x);
            return (RadToDeg(bearing) + 360) % 360;
        }

        LocationSimple CalculateDestinationLocation(LocationSimple point, double bearing, double distance) {
            distance = distance / radius;
            bearing = DegToRad(bearing);
            double lat1 = DegToRad(point.latitude);
            double lon1 = DegToRad(point.longitude);
            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance) + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing));
            double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat1),
                    Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2));
            lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
            return new LocationSimple(RadToDeg(lat2), RadToDeg(lon2));
        }

        double CalculateDistanceBetweenLocations(LocationSimple startPoint, LocationSimple endPoint) {
            double lat1 = DegToRad(startPoint.latitude);
            double lon1 = DegToRad(startPoint.longitude);
            double lat2 = DegToRad(endPoint.latitude);
            double lon2 = DegToRad(endPoint.longitude);
            double deltaLat = lat2 - lat1;
            double deltaLon = lon2 - lon1;
            double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return (radius * c);
        }


    }

    class LocationSimple {
        public double latitude, longitude;

        public LocationSimple(double lat, double lon) {
            latitude = lat;
            longitude = lon;
        }
    }

}

