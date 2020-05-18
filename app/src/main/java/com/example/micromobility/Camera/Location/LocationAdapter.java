package com.example.micromobility.Camera.Location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.geojson.GeoJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LocationAdapter {
        private FusedLocationProviderClient mfusedLocationClient;
        private double wayLatitude = 0.0, wayLongitude = 0.0;
        private LocationRequest locationRequest;
        private LocationCallback locationCallback;
        private boolean isGPS = false;
        private boolean stopupdating = false, startUpdating=false;
        private Context context;
        private ArrayList<Location> locations = new ArrayList<>();
        private ArrayList<Float> times = new ArrayList<>();
        private Long firstTime;
        private LocationExtensor locationExtensor;



    public void initializeLocation(Context context){
                this.context = context;
                mfusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(500); // Half a second
                locationRequest.setFastestInterval(100); // Each milessim of second

                new GpsUtils(context).turnGPSOn(new GpsUtils.onGpsListener() {
                        @Override
                        public void gpsStatus(boolean isGPSEnable) {
                                // turn on GPS
                                isGPS = isGPSEnable;
                        }
                });

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            SettingsClient settingsClient = LocationServices.getSettingsClient(context);
            settingsClient.checkLocationSettings(locationSettingsRequest);

            locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if(startUpdating==true) {
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    }
            };

        mfusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


    }


    public void onLocationChanged(Location location) {
        if(stopupdating){
            System.out.println("I do not know why you are here. Stopped Updating theorically");
        }
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        System.out.println(msg);
        // You can now create a LatLng Object for use with maps
        locations.add(location);
        times.add(computeTime(times.isEmpty()));
    }

    private Float computeTime(Boolean bool){
        //Getting the current date
        Date date = new Date();
        //This method returns the time in millis
        long timeMilli = date.getTime();

        //If bool is True means we are taking the first location and time, so we capture first time
        if(bool){
            this.firstTime = timeMilli;
        }
        long exact_time = timeMilli-this.firstTime;
        return Float.valueOf(TimeUnit.MILLISECONDS.toMillis(exact_time)) /1000;
    }




        public void startLocations() {
            startUpdating=true;
        }

        public void stopLocation(){
                stopupdating=true;
                mfusedLocationClient.removeLocationUpdates(locationCallback);
        }

        public JSONArray getFirstLocation(){
                Location location = this.locations.get(0);
                JSONArray locations = new JSONArray();
                JSONObject locations2 = new JSONObject();
                try {
                    locations2.put("Longitude", location.getLongitude());
                    locations2.put("Latitude", location.getLatitude());
                    locations.put(locations2);

                }catch (JSONException ex){
                    ex.printStackTrace();
                }

            return locations;
        }

        public Float getFps(){
                return locationExtensor.getFps();
        }

        public JSONArray getLastLocation(){
            Location location = this.locations.get(this.locations.size()-1);;
            JSONArray locations = new JSONArray();
            JSONObject locations2 = new JSONObject();
            try {
                locations2.put("Longitude", location.getLongitude());
                locations2.put("Latitude", location.getLatitude());
                locations.put(locations2);

            }catch (JSONException ex){
                ex.printStackTrace();
            }

            return locations;
        }

        public boolean writeLocations(File video, String path){
                File file = new File(path, "map_information.geojson");

                if(!file.exists()){
                        try{
                                file.createNewFile();
                                FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
                                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                JSONObject jsonObject = getFeatureCollection(video);
                                bufferedWriter.write(jsonObject.toString(5));
                                bufferedWriter.close();
                        }catch (IOException | JSONException ex){
                                ex.printStackTrace();
                                return false;
                        }
                }
                else{
                        return false;
                }
                return true;
        }

        private JSONObject getFeatureCollection(File videoFile) {
                locationExtensor = new LocationExtensor();
                ArrayList<LocationExtensor.ObjLocation> definitive_loc = locationExtensor.initializeLocation(locations, times, videoFile);
                JSONObject featureCollection = new JSONObject();
                JSONArray feature1 = new JSONArray();
                JSONObject feature = new JSONObject();
                JSONObject geometry = new JSONObject();
                JSONObject properties = new JSONObject();
                JSONArray coordinates = new JSONArray();
                JSONArray location;
                JSONArray time = new JSONArray();
                try {
                        for (LocationExtensor.ObjLocation object : definitive_loc) {
                                location = new JSONArray();
                                location.put(object.getLongitude());
                                location.put(object.getLatitude());
                                coordinates.put(location);
                                time.put(object.getTime());
                        }

                        properties.put("Times", time);

                        geometry.put("coordinates", coordinates);
                        geometry.put("type", "LineString");

                        feature.put("type", "Feature");
                        feature.put("properties",properties);
                        feature.put("geometry", geometry);

                        feature1.put(feature);

                        featureCollection.put("type", "FeatureCollection");
                        featureCollection.put("features", feature1);
                } catch (JSONException ex) {
                        ex.printStackTrace();
                        return null;
                }

                return featureCollection;
        }

}
