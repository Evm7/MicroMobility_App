package com.example.micromobility.ui.video;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.R;
import com.example.micromobility.ui.video.dashboard.FileHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class VideoDescriptor extends Fragment {
    private View mcontainer;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String title;
    private String path;
    private File file;
    private FileHelper fileHelper;



    public static VideoDescriptor newInstance(String param1, String param2) {
        VideoDescriptor fragment = new VideoDescriptor();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(ARG_PARAM1);
            title = getArguments().getString(ARG_PARAM2);

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mcontainer = inflater.inflate(R.layout.video_descriptor_fragment, container, false);
        fileHelper = new FileHelper(mcontainer.getContext(), path);
        JSONObject summary = fileHelper.readManualFile("Summary.json");
        JSONObject detections = fileHelper.readManualFile("None");
        TextView summary_text = mcontainer.findViewById(R.id.summary);
        TextView detections_text = mcontainer.findViewById(R.id.detections_manuel);

        try {
            summary_text.setText(summary.toString(5));
            detections_text.setText(detections.toString(5));
        } catch (JSONException e) {
            Toast.makeText(mcontainer.getContext(), "Manual detections does not exist yet", Toast.LENGTH_SHORT ).show();
        }
        return mcontainer;
    }
}

