package com.example.micromobility.ui.profile;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.micromobility.MainActivity;
import com.example.micromobility.R;
import com.example.micromobility.Storage.InternalStorage;
import com.example.micromobility.Upload.UploadHelper;
import com.example.micromobility.ui.profile.Adapters.AddLocation;
import com.example.micromobility.ui.profile.Adapters.FoldingCellListAdapter;
import com.example.micromobility.ui.profile.Adapters.Item;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.micromobility.ui.video.VideoActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ramotion.foldingcell.FoldingCell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MyVideosFragment extends Fragment implements AddLocation.ProcessCallback {

    private static final int GEOJSON_REQUEST_CODE = 107 ;
    private View mcontainer;
    private InternalStorage in;
    private String username;
    private Bitmap avatar;
    private UploadHelper uploadHelper;
    private AddLocation adapterLocation;



    public ArrayList<Item> items = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mcontainer = inflater.inflate(R.layout.fragment_myvideos, container, false);

        in = new InternalStorage(mcontainer.getContext());
        username =in.getUsername();
        avatar = in.getPhoto(username);

        // prepare elements to display
        final ArrayList<Item> items = getVideos();

        // get our list view
        ListView theListView = mcontainer.findViewById(R.id.mainListView);
        // add custom btn handler to first list item
        try {
            for(Item i: items){
                i.setRequestBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mcontainer.getContext(), "Sharing video " + i.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });

                i.setDetecttBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mcontainer.getContext(), "Detect video " + i.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
                i.setUploadBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mcontainer.getContext(), "Upload video " + i.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });

                i.setInformBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mcontainer.getContext(), "Information of the video " + i.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
                i.setMapBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mcontainer.getContext(), "Mapping for the video " + i.getTitle(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), VideoActivity.class);
                        Bundle b = new Bundle();
                        b.putString("Item", i.getDirectory_path()); //Your id
                        intent.putExtras(b); //Put your id to your next Intent
                        startActivity(intent);
                    }
                });
                i.setAddMapBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mcontainer.getContext(), "Adding location to " + i.getTitle(), Toast.LENGTH_SHORT).show();
                        System.out.println("VIDEO NAME IS "+ i.getTitle());
                        adapterLocation.addLocation(i.getDirectory_path(), i.getTitle());

                    }
                });

            }
        }catch (IndexOutOfBoundsException ex){
            Toast.makeText(mcontainer.getContext(), "No videos stored currently", Toast.LENGTH_SHORT).show();
        }

        // create custom adapter that holds elements and their state (we need hold a id's of unfolded elements for reusable elements)
        final FoldingCellListAdapter adapter = new FoldingCellListAdapter(mcontainer.getContext(), items);
        this.adapterLocation = new AddLocation(mcontainer.getContext(),  getActivity(), username, this);


        // set elements to adapter
        theListView.setAdapter(adapter);

        // set on click event listener to list view
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                // toggle clicked cell state
                ((FoldingCell) view).toggle(false);
                // register in adapter that state for selected cell is toggled
                adapter.registerToggle(pos);
            }
        });

        return mcontainer;
    }


    private ArrayList<Item> getVideos() {
        File dir = mcontainer.getContext().getExternalFilesDir("Micromobility"+"/"+username);
        ArrayList<Item> items = traverseAll(dir);
        return items;
    }

    // Iterate through all the videos directories
    public ArrayList<Item> traverseAll (File dir) {
        ArrayList<Item> items = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    Item item =traverseVideo(file);
                    if (item !=null){
                        items.add(item);
                    }
                    else{
                        deleteDirectory(file);
                    }
                } else {
                    // do something here with the file
                    System.out.println("ERROR: There should not be any FILE in here");
                }
            }
        }
        return items;
    }


    private void deleteDirectory(File dir){
        File dir_to_remove = mcontainer.getContext().getExternalFilesDir("Micromobility"+"/"+username+"/"+dir.getName());
        System.out.println("DELETEING WRONG SAVED DIRECTORY: "+ dir_to_remove.getAbsolutePath());
        deleteRecursievly(dir_to_remove);
    }
    private void deleteRecursievly(File fileOrDirectory){
        if (fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursievly(child);
                }
            }
            fileOrDirectory.delete();
        }
    }

    // Iterate through all the files in each video
    private Item traverseVideo (File dir) {
        File summary = new File( mcontainer.getContext().getExternalFilesDir("Micromobility"+"/"+username+"/"+dir.getName()), "Summary.json");
        if (!summary.exists()){
            Toast.makeText(mcontainer.getContext(), "Error while retrieving information on file "+ dir.getName(), Toast.LENGTH_SHORT).show();
        }else{
            JSONObject jsonObject = parseFile(summary);
            if (jsonObject==null){
                return null;
            }
            try{
                String geojson = jsonObject.get("geojson").toString();
                String title = jsonObject.get("title").toString();
                String duration = jsonObject.get("duration").toString();

                String fromAddress = getStringLocation(jsonObject.get("fromAddress").toString());
                String toAddress = getStringLocation(jsonObject.get("toAddress").toString());
                boolean detected;
                if ( jsonObject.get("detected").toString().equals("false")){
                    detected = false;
                }
                else{
                    detected = true;
                }
                boolean uploaded;
                if ( jsonObject.get("uploaded").toString().equals("false")){
                    uploaded = false;
                }
                else{
                    uploaded = true;
                }

                String date = jsonObject.get("date").toString();
                String time = jsonObject.get("time").toString();
                String image_path = jsonObject.get("image").toString();
                String directory_path = jsonObject.get("directory_path").toString();
                if (!geojson.equals("map_information.geojson")){
                    directory_path = getContext().getExternalFilesDir(directory_path).getAbsolutePath();
                }
                Bitmap bitmap = BitmapFactory.decodeFile(directory_path+"/"+image_path);
                String username = jsonObject.get("username").toString();
                if (!username.equals(this.username)){
                    System.out.println("Error in usernames. Getting file from "+ username + " as user "+ this.username);
                    return null;
                }
                Item item = new Item(title, bitmap,duration, fromAddress, toAddress,detected,uploaded,date,time, directory_path,username, this.avatar, geojson);
                return item;
            }catch (JSONException ex){
                ex.printStackTrace();
            }
        }
        return null;
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

    private String getStringLocation(String jsonObject){
        try {
            if (jsonObject.equals("None")){
                return "None";
            }
            JSONArray jsonArray = new JSONArray(jsonObject);
            JSONObject loc = jsonArray.getJSONObject(0);
            String lon_s = loc.getString("Longitude");
            String lat_s = loc.getString("Latitude");
            if (lon_s.equals("None")){
                return "None";
            }
            return (lon_s +" "+ lat_s);

        } catch (JSONException e) {
            System.out.println("There string should be :"+jsonObject);
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onProcessCallback(String arg, UploadHelper video) {
        uploadHelper = video;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, GEOJSON_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GEOJSON_REQUEST_CODE) {
                    Uri geojsonuri = data.getData();
                    String geojsonpath = geojsonuri.getPath();
                    InputStream inputStream = getContext().getContentResolver().openInputStream(geojsonuri);
                    Log.e("GeoJson path", geojsonpath);
                    uploadHelper.setType("Upload");
                    boolean bool = uploadHelper.setGeoJsonFile(inputStream);
                    if(bool==false){
                        Toast.makeText(mcontainer.getContext(), "Format of file incorrect. Try again", Toast.LENGTH_SHORT).show();
                        uploadHelper.clearCacheGeoJson();
                    }else{
                        Toast.makeText(mcontainer.getContext(), "Uploading file", Toast.LENGTH_SHORT).show();
                        uploadHelper.writeGeoJsonFile();
                        uploadHelper.writeSummary();
                    }
                }
            }

        } catch (Exception e) {
            Toast.makeText(mcontainer.getContext(), "Failed to load the video. Try again", Toast.LENGTH_SHORT).show();
        }
    }
}