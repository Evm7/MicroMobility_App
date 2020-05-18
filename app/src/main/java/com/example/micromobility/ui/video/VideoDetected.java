package com.example.micromobility.ui.video;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.micromobility.R;

import org.w3c.dom.Text;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class VideoDetected extends Fragment {

    private View mcontainer;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private String title;
    private String path;
    private File file;
    private VideoView videoView;
    private boolean detected;
    private LinearLayout none_detected_layout;
    private ImageButton led;



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
        videoView =(VideoView) mcontainer.findViewById(R.id.vdVw);
        none_detected_layout= (LinearLayout) mcontainer.findViewById(R.id.linear_detected);
        led = mcontainer.findViewById(R.id.led_detected_video);
        if(detected){
            file = new File(mcontainer.getContext().getExternalFilesDir(path), title);
            if(file.exists()) {
                none_detected_layout.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                initializeVideo();
            }else {
                Toast.makeText(mcontainer.getContext(), "Error while retrieving the detected video", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            led.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(detected==false) {
                        detected = true;
                        led.setImageResource(R.drawable.led_on);
                        TextView textView = mcontainer.findViewById(R.id.below_detection_text);
                        TextView textView1 = mcontainer.findViewById(R.id.above_detection_text);
                        textView1.setText("Wait! Video is being detected right now");
                        textView.setVisibility(View.GONE);
                        Toast.makeText(mcontainer.getContext(), "Video is being detected", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mcontainer.getContext(), "Working on this", Toast.LENGTH_SHORT).show();

                    }

                }
            });


        }
        return mcontainer;

    }

    private void initializeVideo(){
        //Set MediaController  to enable play, pause, forward, etc options.
        MediaController mediaController= new MediaController(mcontainer.getContext());
        mediaController.setAnchorView(videoView);
        //Location of Media File
        Uri uri = Uri.fromFile(file);
        //Starting VideView By Setting MediaController and URI
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();

    }

    private void startVideo(){
        videoView.start();
    }
}

