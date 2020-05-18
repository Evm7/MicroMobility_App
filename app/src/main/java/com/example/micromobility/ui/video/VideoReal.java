package com.example.micromobility.ui.video;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.micromobility.R;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class VideoReal extends Fragment {

    private View mcontainer;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String title;

    private String path;
    private File file;
    private VideoView videoView;




    public static VideoReal newInstance(String param1, String param2) {
        VideoReal fragment = new VideoReal();
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

        mcontainer = inflater.inflate(R.layout.video_player_real, container, false);
        file = new File(mcontainer.getContext().getExternalFilesDir(path), title);
        if(file.exists()) {
            initializeVideo();
        }else {
            Toast.makeText(mcontainer.getContext(), "Video has not been detected yet", Toast.LENGTH_SHORT).show();
        }
        return mcontainer;
    }

       private void initializeVideo(){
            videoView =(VideoView) mcontainer.findViewById(R.id.vdVw);
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

