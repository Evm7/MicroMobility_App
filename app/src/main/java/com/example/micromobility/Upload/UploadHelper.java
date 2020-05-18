package com.example.micromobility.Upload;

import android.content.Context;

import android.graphics.Bitmap;

import android.media.MediaMetadataRetriever;

import android.os.FileUtils;
import android.util.Log;


import com.coremedia.iso.IsoFile;
import com.google.android.gms.common.util.IOUtils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import androidx.appcompat.app.AlertDialog;
public class UploadHelper {
    private String username;

    private static final String GENERAL_DIRECTORY_NAME = "MicroMobility";
    private static String VIDEO_DIRECTORY_NAME;
    private String video_name;

    private File realVideo;
    private InputStream sourceVideo;
    private InputStream geoJsonFile;
    private File realGeoJsonFile;
    private FeatureCollection featureCollection;
    private LocationExtensor locationExtensor;


    private Context context;
    private String TAG;

    private String type;
    private List<Point> pointList;


    public UploadHelper(String username, Context context, String TAG){
        this.username=username;
        this.context = context;
        this.TAG = TAG;
    }

    public void setVideoDirectoryName(String video_name){
        this.VIDEO_DIRECTORY_NAME=video_name;
    }

    public void setVideo_name(String video_name) {
        this.video_name = video_name;
    }

    public void setType(String type){
        this.type=type;
    }

    public void setSourceVideo(InputStream inputStream){
        this.sourceVideo=inputStream;
    }

    public boolean setGeoJsonFile(InputStream inputStream){
        this.geoJsonFile=inputStream;
        if (checkVeracity()){
            return true;
        }
        return false;
    }

    public void setFeatureCollection(FeatureCollection featureCollection){
        this.featureCollection =featureCollection;
    }

    public void setPointList(List<Point> pointList){
        this.pointList=pointList;
    }

    public void clearCacheGeoJson(){
        this.type="";
        this.pointList = null;
        this.geoJsonFile=null;
    }
    private boolean checkVeracity(){
        featureCollection = FeatureCollection.fromJson(convertStreamToString(this.geoJsonFile));
        if (featureCollection.features() != null) {
            LineString lineString = (LineString) featureCollection.features().get(0).geometry();
            if (lineString != null){
                pointList = lineString.coordinates();
                return true;
            }
        }
        return false;
    }
    static String convertStreamToString(InputStream is) {
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void setFeatures(){
        locationExtensor = new LocationExtensor();
        pointList = locationExtensor.initializeLocation(pointList, realVideo);
        featureCollection = insertPointList(pointList);
    }
    private FeatureCollection insertPointList(List<Point> listpoint){
        List<Feature> directionsRouteFeatureList = new ArrayList<>();
        directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(listpoint)));
        FeatureCollection dashedLineDirectionsFeatureCollection = FeatureCollection.fromFeatures(directionsRouteFeatureList);
        return dashedLineDirectionsFeatureCollection;
    }



    public void writeAll(){
        saveVideoFile();
        writeFirstFrame();
        if (this.type.equals("Upload")){
            writeGeoJsonFile();
        }else if(this.type.equals("Map")){
            writeFeatureCollectionFile();
        }
        writeSummary();
    }

    public File writeFeatureCollectionFile() {

        File destFile;
        // Video saved on MICROMOBILITY/username/video_title/video_title.mp4
        File dir = context.getExternalFilesDir(VIDEO_DIRECTORY_NAME);

        destFile = new File(dir, "map_information.geojson");
        if(!destFile.exists()) {
            try {
                destFile.createNewFile();
                FileWriter fileWriter = new FileWriter(destFile.getAbsolutePath());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                setFeatures();
                String features = featureCollection.toJson();
                bufferedWriter.write(features);
                bufferedWriter.close();
                return destFile;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
            return null;
    }

    public File writeGeoJsonFile(){
        File destFile = writeFeatureCollectionFile();
        realGeoJsonFile = destFile;
        return destFile;
    }

    private void writeFirstFrame(){
        Bitmap bmp = getFirstFrame();
        File dir = context.getExternalFilesDir(VIDEO_DIRECTORY_NAME);
        if(!dir.exists())
            System.out.println("Directory did no exist?");
            dir.mkdirs();
        File file = new File(dir, "first_frame.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap getFirstFrame(){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(realVideo.getAbsolutePath());
        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(0);
        return bmFrame;
    }

    public void clearCache(){
        clearCacheGeoJson();
        this.featureCollection=null;
        this.realGeoJsonFile=null;
        this.realVideo=null;
        this.sourceVideo=null;
        this.VIDEO_DIRECTORY_NAME="None";
    }

    private File saveVideoFile() {
        // External sdcard file location --> MICROMOBILITY
        File generalStorageDir = context.getExternalFilesDir(GENERAL_DIRECTORY_NAME);
        // Create storage directory if it does not exist
        if (!generalStorageDir.exists()) {
            if (!generalStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + GENERAL_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // External sdcard file location --> MICROMOBILITY/username
        String userdirectoy = GENERAL_DIRECTORY_NAME + File.separator + this.username;
        File userStorageDir = context.getExternalFilesDir(userdirectoy);
        // Create storage directory if it does not exist
        if (!userStorageDir.exists()) {
            if (!userStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + userdirectoy + " directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        video_name = "VID_" + timeStamp + ".mp4";

        // External sdcard file location --> MICROMOBILITY/username/video_title
        VIDEO_DIRECTORY_NAME = GENERAL_DIRECTORY_NAME + File.separator + this.username + File.separator + video_name;
        File mediaStorageDir = context.getExternalFilesDir(VIDEO_DIRECTORY_NAME);
        // Create storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + VIDEO_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        File destFile;
        // Video saved on MICROMOBILITY/username/video_title/video_title.mp4
        destFile = new File(mediaStorageDir.getPath() + File.separator + video_name);

        try{
            byte[] buffer = new byte[sourceVideo.available()];
            sourceVideo.read(buffer);
            OutputStream outStream = new FileOutputStream(destFile);
            outStream.write(buffer);
        } catch (FileNotFoundException e) {
            // handle exception here
        } catch (IOException e) {
            // handle exception here
        }

        realVideo = destFile;
        return destFile;
    }

    public boolean writeSummary(){

        File dir = context.getExternalFilesDir(VIDEO_DIRECTORY_NAME);
        File file = new File(dir, "Summary.json");
        System.out.println("VIDEO DIRECTORY NAME: "+ VIDEO_DIRECTORY_NAME);
        System.out.println("DIRECTORY: "+ dir.getAbsolutePath());


        if(!file.exists()){
            try{
                System.out.println("FILE SUMMARY: "+ file.getAbsolutePath());
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                JSONObject jsonObject = getJSONSummary(type);
                bufferedWriter.write(jsonObject.toString(5));
                bufferedWriter.close();
            }catch (IOException | JSONException ex){
                ex.printStackTrace();
                return false;
            }
        }
        else{
            file.delete();
            writeSummary();
        }
        return true;
    }

    private JSONObject getJSONSummary(String type) {
        JSONObject summary = new JSONObject();
        SimpleDateFormat formatter_date= new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatter_time= new SimpleDateFormat("HH:mm:ss");
        Date date_time = new Date(System.currentTimeMillis());
        String date = formatter_date.format(date_time);
        String time = formatter_time.format(date_time);
        String duration = "0";

        try{
            File dir = context.getExternalFilesDir(VIDEO_DIRECTORY_NAME);
            System.out.println("Directory :"+ dir.getAbsolutePath());
            System.out.println("Video name is: "+video_name);
            File file = new File(dir, video_name);
            IsoFile isoFile = new IsoFile(file.getAbsolutePath());
            double lengthInSeconds = (double)
                    isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                    isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            duration = secToFormat(lengthInSeconds);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            summary.put("title", video_name);
            summary.put("detected", false);
            summary.put("uploaded", false);
            summary.put("geojson", type);
            if(type.equals("None")){
                summary.put("fromAddress", "None");
                summary.put("toAddress", "None");
                summary.put("fps", "None");
            }else {
                summary.put("fromAddress", getLocation(pointList.get(0)));
                summary.put("toAddress", getLocation(pointList.get(pointList.size()-1)));
                summary.put("fps", locationExtensor.getFps());
            }
            summary.put("image", "first_frame.png");
            summary.put("detected_path", "None");
            summary.put("username", this.username);
            summary.put("date", date);
            summary.put("time", time);
            summary.put("duration", duration);
            summary.put("directory_path", VIDEO_DIRECTORY_NAME);


        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }

        return summary;
    }

    private String secToFormat(Double total)
    {
        int hours = (int) (total / 3600);
        int remainder = (int) (total - hours * 3600);
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        return hours +":"+ mins +":"+ secs;
    }

    public JSONArray getLocation(Point location){
        JSONArray locations = new JSONArray();
        JSONObject locations2 = new JSONObject();
        try {
            locations2.put("Longitude", location.longitude());
            locations2.put("Latitude", location.latitude());
            locations.put(locations2);

        }catch (JSONException ex){
            ex.printStackTrace();
        }

        return locations;
    }


}
