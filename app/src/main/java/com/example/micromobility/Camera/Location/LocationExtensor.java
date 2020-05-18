package com.example.micromobility.Camera.Location;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class LocationExtensor {

    private ArrayList<ObjLocation> real_locations; // The expanded one
    private ArrayList<Location> locations; // The original
    private ArrayList<Float> times, framing_times;  // The original and the expanded
    private float fps;
    private float duration;
    private int frame_number;
    LocationCalculator locationCalculator;

    private int looper;


    public ArrayList<ObjLocation> initializeLocation(ArrayList<Location> locations, ArrayList<Float> times, File videoFile) {
        this.locations = locations;
        this.times = times;
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

        // We create the array of all times for each frame
        for (int i = 0; i < this.frame_number; i++) {
            framing_times.add((i / this.fps));
        }
        relate_file_video();

        return real_locations;
    }

    public float getFps() {
        return fps;
    }

    private void relate_file_video() {
        // Id for looping over the times of the video but avoiding taking whole loop
        this.looper = 0;

        Location before_value = null;
        // Loops over the times of the location
        int i = 0;
        for (Location loc : locations) {
            Float key = times.get(i);
            assign_location(key, loc, before_value);
            before_value = loc;
            i += 1;
        }
        // Check if we have covered all the end locations, there may be some frames without being marked
        while (this.looper < this.frame_number) {
            this.real_locations.add(this.looper, new ObjLocation(this.framing_times.get(this.looper), before_value.getLatitude(), before_value.getLongitude(), this.looper));
            this.looper += 1;
        }

        this.real_locations.removeAll(Collections.singleton(null));
    }


    private void assign_location(Float key, Location current_location, Location last_location) {
        int num = 0;

        //Loops over the times of the video
        while ((this.looper <= this.frame_number) && (this.framing_times.get(this.looper) < key + 0.1)) {
            //This means that first location was taken not while start recording
            if (last_location == null) {
                this.real_locations.add(this.looper, new ObjLocation(this.framing_times.get(this.looper), current_location.getLatitude(), current_location.getLongitude(), this.looper));
            }
            //This means that last location was taken not while end recording
            else if (current_location == null) {
                this.real_locations.add(this.looper, new ObjLocation(this.framing_times.get(this.looper), last_location.getLatitude(), last_location.getLongitude(), this.looper));
            }
            //This means we have found a relation in location file.We set the relation and interpolate
            // the previous results not found
            else if (((this.framing_times.get(this.looper)) < (key + 0.1)) && ((this.framing_times.get(this.looper)) > (key - 0.1))) {
                this.real_locations.add(this.looper, new ObjLocation(this.framing_times.get(this.looper), current_location.getLatitude(), current_location.getLongitude(), this.looper));
                if (num > 0) {
                    interpolate(current_location, last_location, num);
                    num = 0;
                }
            }
            //This means that no relation has been found, we just create the orderedDict and count
            // number of not related frames
            else {
                this.real_locations.add(this.looper, null);
                num += 1;

            }
            this.looper += 1;
        }
    }

    private void interpolate(Location current_location, Location last_location, int num) {
        ArrayList<LocationSimple> locs_interpol =this.locationCalculator.interpolateLocations(num,current_location,last_location);
        int i = this.looper - num;


        while (i<this.looper){
            LocationSimple current_loc =locs_interpol.get(i -(this.looper -num));
            this.real_locations.add(i, new ObjLocation(this.framing_times.get(i), current_loc.latitude, current_loc.longitude, i));
            i +=1;
        }
    }

    public class ObjLocation {
        private Float time;
        private Double longitude;
        private Double latitude;
        private Integer id;

        public ObjLocation(Float time, Double latitude, Double longitude, Integer id) {
            this.time = time;
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

        public Float getTime(){
            return time;
        }
        public String toString()
        {
            return "Id "+ id+ " at time "+ time + " : lat " + latitude + "  and lon " + longitude +"\n";
        }

    }

    public class LocationCalculator{
        double radius = 6371;

        public LocationCalculator(){
        }

        //Calculate interpolation Locations
        public ArrayList<LocationSimple> interpolateLocations(int num_interpol, Location start, Location end){
            LocationSimple startPoint = new LocationSimple(start.getLatitude(), start.getLongitude());
            LocationSimple endPoint = new LocationSimple(end.getLatitude(), end.getLongitude());

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

