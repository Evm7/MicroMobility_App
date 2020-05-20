package com.example.micromobility.ui.video.dashboard;

import android.content.Context;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {
    private String directory_path, username;
    private Context context;
    private File directory, manual_detected_json;
    private final static String FILE_MANUAL_NAME = "Manual_detections.json", FILE_AUTOMATIC_NAME = "Micro_detections.json";

    public FileHelper(Context context, String directory_path){
        this.context=context;
        this.directory_path=directory_path;
        this.directory = context.getExternalFilesDir(directory_path);
    }

    public void writeManualFile(String category, JSONArray jsonArray){
        File file = new File(this.directory, FILE_MANUAL_NAME);
        try{
        if (!file.exists()){
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            JSONObject created = createFileObject();
            JSONObject updated = setManualValue(category, jsonArray,  created);
            bufferedWriter.write(updated.toString(5));
            bufferedWriter.close();

        }else{
            JSONObject read_Object = readManualFile("None");
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            JSONObject updated;
            if(read_Object==null){
                JSONObject created = createFileObject();
                updated = setManualValue(category, jsonArray,  created);
            }else{
                updated = setManualValue(category, jsonArray,  read_Object);
            }
            bufferedWriter.write(updated.toString(5));
            bufferedWriter.close();
        }
        }catch (IOException | JSONException ex){
            ex.printStackTrace();
        }
    }


    public JSONObject readManualFile(String file_str){
        File file;
        if (file_str.equals("None")){
            file = new File(this.directory, FILE_MANUAL_NAME);
        }
        else{
            file = new File(this.directory, file_str);
        }
        if (!file.exists()) {
            return null;
        }else{
            return parseFile(file);
        }
    }

    private JSONObject parseFile(File file){
        //JSON parser object to parse read file
        try{
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null){
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            // This response will have Json Format String
            String response = stringBuilder.toString();
            JSONObject jsonObject  = new JSONObject(response);
            return jsonObject;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject setManualValue(String category, JSONArray value, JSONObject read){
        try {
            JSONArray jsonArray = read.getJSONArray(category);
            read.put(category,value);
            return read;
        } catch (JSONException e) {
            try {
                System.out.println("Manual file does not contain this category: "+ read.toString(5));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return null;
    }

    private JSONArray getManualValue(String category, JSONObject read){
        try {
            return  read.getJSONArray(category);
        } catch (JSONException e) {
            try {
                System.out.println("Manual file does not contain this category: "+ read.toString(5));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject createFileObject(){
        JSONObject all = new JSONObject();
        JSONArray type_of_road = new JSONArray();
        JSONArray pedestrians = new JSONArray();
        JSONArray cars = new JSONArray();
        JSONArray motorbikes = new JSONArray();
        JSONArray bus = new JSONArray();
        JSONArray truck = new JSONArray();
        JSONArray plane = new JSONArray();
        JSONArray boat = new JSONArray();
        JSONArray train = new JSONArray();
        JSONArray bicycle = new JSONArray();
        JSONArray skateboard = new JSONArray();
        JSONArray parking_meter = new JSONArray();
        JSONArray traffic_light = new JSONArray();
        JSONArray stop_sign = new JSONArray();
        JSONArray fire_hydrant = new JSONArray();
        JSONArray bench = new JSONArray();
        JSONArray new_categories = new JSONArray();
        try {
            all.put("Type of Road", type_of_road);
            all.put("Pedestrians", pedestrians);
            all.put("Cars", cars);
            all.put("Motorbike", motorbikes);
            all.put("Bus", bus);
            all.put("Truck", truck);
            all.put("Plane", plane);
            all.put("Boat", boat);
            all.put("Train", train);
            all.put("Bicycle", bicycle);
            all.put("Skateboard", skateboard);
            all.put("Traffic Light", traffic_light);
            all.put("Stop Sign", stop_sign);
            all.put("Fire Hydrant", fire_hydrant);
            all.put("Bench", bench);
            all.put("Parking Meter", parking_meter);
            all.put("New categories", new_categories);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return all;
    }
}
