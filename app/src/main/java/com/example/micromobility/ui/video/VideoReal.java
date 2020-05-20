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
import com.example.micromobility.ui.video.dashboard.PlayerHelper;
import com.google.android.exoplayer2.source.dash.PlayerEmsgHandler;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class VideoReal extends Fragment implements PlayerHelper.EndOption {

    private View mcontainer;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String title;

    private String path;
    private File file;
    private PlayerHelper playerHelper;




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
           playerHelper = new PlayerHelper(path, title, "Real Video", this);
           playerHelper.initializePlayer(mcontainer);
           playerHelper.setControls();
        }

    @Override
    public void onEndOption(String type) {

    }
}

