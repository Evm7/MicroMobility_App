package com.example.micromobility.ui.video.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.R;
import com.example.micromobility.ui.video.VideoDetected;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class DashboardFragment extends Fragment {
    View mcontainer;
    private RelativeLayout type_of_road_icon, pedestrians_icon, bycicle_icon, vehicle_icon, public_vehicle_icon, others_icon, new_icon, signs_icon;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String path, title;
    private boolean detected;

    public static DashboardFragment newInstance(String param1, Boolean param2, String param3) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putBoolean(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(ARG_PARAM1);
            detected = getArguments().getBoolean(ARG_PARAM2);
            title = getArguments().getString(ARG_PARAM3);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mcontainer = inflater.inflate(R.layout.fragment_dashboard, container, false);
        getAllLayouts();
        return mcontainer;


    }

    private void getAllLayouts(){
        type_of_road_icon = mcontainer.findViewById(R.id.type_of_road_icon);
        specifyIcon(type_of_road_icon, R.mipmap.road, "Type of Road", R.color.choose_traffic_road);

        pedestrians_icon = mcontainer.findViewById(R.id.pedestrians_icon);
        specifyIcon(pedestrians_icon, R.mipmap.pedestrian, "Pedestrians", R.color.choose_pedestrian);

        bycicle_icon = mcontainer.findViewById(R.id.bycicle_icon);
        specifyIcon(bycicle_icon, R.mipmap.bike, "Bicycles", R.color.choose_bikes);

        vehicle_icon = mcontainer.findViewById(R.id.vehicle_icon);
        specifyIcon(vehicle_icon, R.mipmap.vehicle, "Small vehicles", R.color.choose_vehicle);

        public_vehicle_icon = mcontainer.findViewById(R.id.public_vehicle_icon);
        specifyIcon(public_vehicle_icon, R.mipmap.public_vehicle, "Big vehicles", R.color.choose_public_transport);

        others_icon = mcontainer.findViewById(R.id.others_icon);
        specifyIcon(others_icon, R.mipmap.others, "Other objects", R.color.choose_others);

        new_icon = mcontainer.findViewById(R.id.new_icon);
        specifyIcon(new_icon, R.mipmap.news, "New categories", R.color.choose_news);

        signs_icon = mcontainer.findViewById(R.id.signs_icon);
        specifyIcon(signs_icon, R.mipmap.traffic_light, "Traffic Signs", R.color.choose_sings);


    }
    private void specifyIcon(RelativeLayout icon, int image_minimap, String text, int color){
        ImageView imageView = icon.findViewById(R.id.image_icon);
        imageView.setImageDrawable(mcontainer.getContext().getDrawable(image_minimap));
        TextView textView = icon.findViewById(R.id.icon_descriptor);
        textView.setText(text);
        CardView cardView = icon.findViewById(R.id.card_view_inner);
        cardView.setCardBackgroundColor(mcontainer.getResources().getColor(color));
        icon.setOnClickListener(v -> {
            // Create new fragment and transaction
            prepareVideo(icon, text);

        });
    }

    private void prepareVideo(RelativeLayout icon, String text) {
        Toast.makeText(mcontainer.getContext(), "Detecting category: "+ text, Toast.LENGTH_SHORT).show();
        ViewGroup container = ((ViewGroup)getView().getParent());
        container.removeAllViews();
        if (text.equals("Type of Road")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_road, container, false);

        }else if(text.equals("Pedestrians")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_pedestrians, container, false);
        }
        else if(text.equals("Bicycles")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_bikes, container, false);

        }
        else if(text.equals("Small vehicles")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_vehicle, container, false);

        }else if(text.equals("Big vehicles")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_public, container, false);

        }else if(text.equals("Other objects")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_others, container, false);

        }else if(text.equals("New categories")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_news, container, false);

        }else if(text.equals("Traffic Signs")){
            mcontainer = getLayoutInflater().inflate(R.layout.dashboard_signs, container, false);

        }
        container.addView(mcontainer);

        DetectorHelper detectorHelper = new DetectorHelper(mcontainer, path, title, text);
        detectorHelper.addViews(((ViewGroup)mcontainer.getParent()));
        detectorHelper.controlExoPlayer();
        RelativeLayout relativeLayout = ((ViewGroup)mcontainer.getParent()).findViewById(R.id.action_undo_detect);
        relativeLayout.setOnClickListener(v -> {
                    ViewGroup layouts = ((ViewGroup) container.getParent());
                    detectorHelper.finish();
                    layouts.removeAllViews();
                    mcontainer = getLayoutInflater().inflate(R.layout.fragment_dashboard, layouts, false);
                    getAllLayouts();

                }
        );
    }


}
       