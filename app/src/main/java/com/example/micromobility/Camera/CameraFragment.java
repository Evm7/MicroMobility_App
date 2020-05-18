package com.example.micromobility.Camera;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.micromobility.R;
import com.example.micromobility.Sensors.SensorOrientator;


import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends CameraVideoFragment {

    // For the camera when recording
    private static final String TAG = "CameraFragment";
    @BindView(R.id.mTextureView)
    AutoFitTextureView mTextureView;
    Unbinder unbinder;


    // Sensor Variables
    SensorOrientator sensor;
    TextView sensorcoordinates;
    ImageView leftArrow, rightArrow;
    Button callibration_button;
    ToggleButton detection_button;

    View mcontainer;


    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */


    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mcontainer = inflater.inflate(R.layout.video_callibration, container, false);
        callibration_button = mcontainer.findViewById(R.id.start_callibration_button);
        callibration_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Video is set to be callibrated, move to recording optional
                Fragment fragment = CameraRecordFragment.newInstance(detection_button.isChecked());
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment).commit();
            }
        });

        detection_button = mcontainer.findViewById(R.id.preprocessing_btn);
        detection_button.setOnClickListener(v -> {
            Boolean on = ((ToggleButton) v).isChecked();
        });


        sensorcoordinates = mcontainer.findViewById(R.id.realSensor);
        leftArrow = mcontainer.findViewById(R.id.leftToRight);
        rightArrow = mcontainer.findViewById(R.id.rightToLeft);



        // Initialize the sensor
        sensor = new SensorOrientator();
        sensor.initialize(mcontainer.getContext(), Boolean.TRUE, sensorcoordinates, leftArrow, rightArrow, callibration_button);
        Log.v(TAG, "UPDATED SENSORS: ");

       // updateRotate();

        unbinder = ButterKnife.bind(this, mcontainer);
        return mcontainer;
    }


    // FOR CAMERA HANDLING
    @Override
    public int getTextureResource() {
        return R.id.mTextureView;
    }

    @Override
    protected void setUp(View view) {

    }

}