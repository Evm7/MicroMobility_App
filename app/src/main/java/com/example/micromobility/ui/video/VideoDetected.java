package com.example.micromobility.ui.video;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.micromobility.R;
import com.example.micromobility.ui.video.dashboard.FileHelper;
import com.example.micromobility.ui.video.dashboard.PlayerHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class VideoDetected extends Fragment implements PlayerHelper.EndOption {

    private View mcontainer;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private String title;
    private static final String Real_TITLE = "detected.mp4";
    private String path;
    private File file;
    private boolean detected;
    private LinearLayout none_detected_layout;
    private ImageButton led;
    private PlayerHelper playerHelper;
    private RelativeLayout manual_video, detections;


    public static VideoDetected newInstance(String param1, String param2, Boolean param3) {
        VideoDetected fragment = new VideoDetected();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putBoolean(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(ARG_PARAM1);
            title = getArguments().getString(ARG_PARAM2);
            detected = getArguments().getBoolean(ARG_PARAM3);

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mcontainer = inflater.inflate(R.layout.video_player_detected, container, false);
        none_detected_layout= (LinearLayout) mcontainer.findViewById(R.id.linear_detected);
        led = mcontainer.findViewById(R.id.led_detected_video);
        manual_video = mcontainer.findViewById(R.id.manual_detections_btn);
        detections = mcontainer.findViewById(R.id.card_detections_layout);
        showVisibility();
        return mcontainer;

    }

    private void showVisibility() {
        File manual_detections = new File(mcontainer.getContext().getExternalFilesDir(path), "Manual_detections.json");
        if (manual_detections.exists()) {
            manual_video.setVisibility(View.VISIBLE);
        }

        if (detected) {
            file = new File(mcontainer.getContext().getExternalFilesDir(path), Real_TITLE);
            if (file.exists()) {
                none_detected_layout.setVisibility(View.GONE);
                playerHelper = new PlayerHelper(path, Real_TITLE, "Detection", this);
                playerHelper.initializePlayer(mcontainer);
                playerHelper.setControls();
                detections.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(mcontainer.getContext(), "Error while retrieving the detected video", Toast.LENGTH_SHORT).show();
            }
        } else {
            led.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (detected == false) {
                        manual_video.setVisibility(View.GONE);
                        detected = true;
                        led.setImageResource(R.drawable.led_on);
                        TextView textView = mcontainer.findViewById(R.id.below_detection_text);
                        TextView textView1 = mcontainer.findViewById(R.id.above_detection_text);
                        textView1.setText("Wait! Video is being detected right now");
                        textView.setVisibility(View.GONE);
                        Toast.makeText(mcontainer.getContext(), "Video is being detected", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mcontainer.getContext(), "Working on this", Toast.LENGTH_SHORT).show();
                        detections.setVisibility(View.VISIBLE);
                    }

                }
            });

            manual_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mcontainer.getContext(), "Showing manual detections", Toast.LENGTH_SHORT).show();
                    manual_video.setVisibility(View.GONE);
                    none_detected_layout.setVisibility(View.GONE);
                    playerHelper = new PlayerHelper(path, title, "Manual_detections", VideoDetected.this);
                    playerHelper.initializePlayer(mcontainer);
                    playerHelper.setControls();
                    detections.setVisibility(View.VISIBLE);
                    setDetections("None");
                }
            });
        }
    }

    private void setDetections(String file){
        FileHelper fileHelper = new FileHelper(mcontainer.getContext(), path);
        JSONObject detections = fileHelper.readManualFile("None");
        TreeMap<Integer, String> road_detections = getRoadClassifer(detections);
        Spinner spinner_items = mcontainer.findViewById(R.id.spinner_items);
        String[] items = getResources().getStringArray(R.array.items_detect_array);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mcontainer.getContext(), R.layout.spinner_list, items);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_list);
        spinner_items.setAdapter(arrayAdapter);
        TextView propertiesDet = mcontainer.findViewById(R.id.propertiesDet);
        spinner_items.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,View selectedItemView, int position, long id) {
                // Object item = parentView.getItemAtPosition(position);
                String selected = parentView.getItemAtPosition(position).toString();
                TreeMap treeMap = getCounter(detections, selected);
                if(treeMap==null){
                     propertiesDet.setText("None");
                     Toast.makeText(mcontainer.getContext(), "This category has not been detected", Toast.LENGTH_SHORT).show();
                }else{
                    playerHelper.writeFramesText2(propertiesDet, treeMap);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        TextView type_of_road = mcontainer.findViewById(R.id.type_of_road);
        if(road_detections==null){
            type_of_road.setText("Unknown");
            Toast.makeText(mcontainer.getContext(), "Type of Road has not been classified", Toast.LENGTH_SHORT).show();

        }else {
            playerHelper.writeFramesText(type_of_road, road_detections);
        }
    }

    private TreeMap getRoadClassifer(JSONObject jsonObject){
        try {
            JSONArray road_classifier = jsonObject.getJSONArray("Type of Road");
            TreeMap<Integer, String> road_detections  =new TreeMap<>();

            for(int i =0 ; i<road_classifier.length();i++){
                JSONObject key_pairs = road_classifier.getJSONObject(i);
                int time = (int) (key_pairs.getLong("Time"));
                road_detections.put( time, key_pairs.getString("Value"));
            }
            return road_detections;
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(mcontainer.getContext(), "Video has not been detected yet", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private TreeMap getCounter(JSONObject jsonObject, String category){
        try {
            JSONArray counter = jsonObject.getJSONArray(category);
            TreeMap<Integer, String> detections  =new TreeMap<>();

            for(int i =0 ; i<counter.length();i++){
                JSONObject key_pairs = counter.getJSONObject(i);
                int time = (int) (key_pairs.getLong("Time"));
                detections.put( time, key_pairs.getString("Value"));
            }
            return detections;
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(mcontainer.getContext(), "Video has not been detected yet", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    public void onEndOption(String type) {

    }

    @Override
    public void onResume() {
        super.onResume();
        showVisibility();
    }
}

