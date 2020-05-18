package com.example.micromobility.Camera;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.example.micromobility.Camera.Location.LocationAdapter;
import com.example.micromobility.MainActivity;
import com.example.micromobility.R;
import com.example.micromobility.Sensors.SensorOrientator;
import com.example.micromobility.Storage.InternalStorage;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Use the {@link CameraRecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraRecordFragment extends CameraVideoFragment {

    // For the camera when recording
    private static final String TAG = "CameraFragment";
    private static final String TAG_2 = "Dragging";

    private String VIDEO_DIRECTORY_NAME;
    @BindView(R.id.mTextureView)
    AutoFitTextureView mTextureView;
    @BindView(R.id.mRecordVideo)
    ImageView mRecordVideo;
    @BindView(R.id.mVideoView)
    VideoView mVideoView;
    @BindView(R.id.mPlayVideo)
    ImageView mPlayVideo;
    Unbinder unbinder;
    private String mOutputFilePath;

    private File mediaFile;

    private LocationAdapter locationAdapter;

    // Sensor Variables
    SensorOrientator sensor;
    ImageView lineToRotate;
    Integer angle = 0;

    ImageView firstFrame ;
    CardView firstFrame_card, trash_card, save_card, video_name_card;
    ConstraintLayout.LayoutParams layoutParams;
    EditText nameforVideo;

    View mcontainer;

    InternalStorage in;

    String username;
    String videoname;

    Boolean detection = false;

    private static final String ARG_PARAM1 = "param1";
    public static final String [] properties = new String[]{"Traffic Light", "Car", "MotorBike", "Pedestrian", "Bench"};
    public static final String [] typesOfRoads = new String[]{"Bike Bidirectional", "Bike Unidirectional", "Road", "Sidewalk", "Unknown", "Crosswalk"};

    private TextView typeOfRoad, propertiesDet;
    private String typeOfRoad_str, propertiesDetStr;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public CameraRecordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */


    public static CameraRecordFragment newInstance(Boolean param1) {
        CameraRecordFragment fragment = new CameraRecordFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            detection = getArguments().getBoolean(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (detection){
            mcontainer = inflater.inflate(R.layout.fragment_camera2, container, false);
            typeOfRoad = mcontainer.findViewById(R.id.type_of_road);
            propertiesDet = mcontainer.findViewById(R.id.propertiesDet);
        }else {
            mcontainer = inflater.inflate(R.layout.fragment_camera, container, false);
        }

        in = new InternalStorage(mcontainer.getContext());
        username = in.getUsername();

        unbinder = ButterKnife.bind(this, mcontainer);

        lineToRotate = mcontainer.findViewById(R.id.callibration_line);
        firstFrame = mcontainer.findViewById(R.id.firstFrame);
        firstFrame_card = mcontainer.findViewById(R.id.firsFrame_cardView);
        trash_card = mcontainer.findViewById(R.id.trash_cardView);
        save_card = mcontainer.findViewById(R.id.save_cardView);
        video_name_card = mcontainer.findViewById(R.id.video_name_card);
        nameforVideo = mcontainer.findViewById(R.id.video_name);

        save_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When saving video, also save location file
                locationAdapter.writeLocations(mediaFile, VIDEO_DIRECTORY_NAME);
                // When saving video, rename if new name has been introduced
                videoname = nameforVideo.getText().toString();
                Boolean bool = Boolean.FALSE;
                if (!videoname.matches("")) {
                    bool = renameFile(videoname);
                    if (!bool){
                        Toast.makeText(mcontainer.getContext(), "Video saved, but error when changing name", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mcontainer.getContext(), "VIDEO SAVED: "+ videoname, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(mcontainer.getContext(), "VIDEO SAVED", Toast.LENGTH_SHORT).show();

                }
                writeSummary(bool);

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        trash_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mcontainer.getContext(), "VIDEO DELETED", Toast.LENGTH_SHORT).show();
                deleteVideo();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });


        // Initialize the sensor
        sensor = new SensorOrientator();
        sensor.setToRotate(mcontainer.getContext(), Boolean.FALSE, lineToRotate);
        Log.v(TAG, "UPDATED SENSORS: ");

        // Initialize the location
        locationAdapter = new LocationAdapter();
        locationAdapter.initializeLocation(mcontainer.getContext());

        if (detection){
            // Initialize the detector

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while(!isInterrupted()) {
                            Thread.sleep(1000);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    propertiesDetStr = getRandom(properties);
                                    typeOfRoad_str = getRandom(typesOfRoads);
                                    typeOfRoad.setText(typeOfRoad_str);
                                    propertiesDet.setText(propertiesDetStr);
                                }
                            });
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
        }
        return mcontainer;
    }

    public static String getRandom(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    @Override
    public int getTextureResource() {
        return R.id.mTextureView;
    }

    @Override
    protected void setUp(View view) {

    }

    @OnClick({R.id.mRecordVideo, R.id.mPlayVideo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mRecordVideo:
                /**
                 * If media is not recoding then start recording else stop recording
                 */
                if (mIsRecordingVideo) {
                    try {
                        stopRecordingVideo();
                        locationAdapter.stopLocation();
                        prepareViews();
                        VIDEO_DIRECTORY_NAME = getCurrentPath();
                        writeFirstFrame();
                        writeSummary(Boolean.FALSE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    startRecordingVideo(mcontainer.getContext(), this.username);
                    locationAdapter.startLocations();
                    mRecordVideo.setImageResource(R.drawable.ic_stop);
                    //Receive out put file here
                    mOutputFilePath = getCurrentFile().getAbsolutePath();
                }
                break;
            case R.id.mPlayVideo:
                mVideoView.start();
                mPlayVideo.setVisibility(View.GONE);
                trash_card.setVisibility(View.GONE);
                save_card.setVisibility(View.GONE);
                video_name_card.setVisibility(View.GONE);
                firstFrame_card.setVisibility(View.GONE);

                break;
        }
    }

    private void prepareViews() {
        if (mVideoView.getVisibility() == View.GONE) {
            mVideoView.setVisibility(View.VISIBLE);
            mPlayVideo.setVisibility(View.VISIBLE);
            mTextureView.setVisibility(View.GONE);
            try {
                setMediaForRecordVideo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setMediaForRecordVideo() throws IOException {
        mOutputFilePath = parseVideo(mOutputFilePath);
        // Set media controller
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.requestFocus();
        mVideoView.setVideoPath(mOutputFilePath);
        mVideoView.seekTo(100);

        trash_card.setVisibility(View.VISIBLE);
        save_card.setVisibility(View.VISIBLE);
        video_name_card.setVisibility(View.VISIBLE);

        /*
        Bitmap bmp = getFirstFrame();
        firstFrame.setImageBitmap(bmp);
        firstFrame_card.setVisibility(View.VISIBLE);
        scrollateImageView(firstFrame_card);
        */
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                trash_card.setVisibility(View.VISIBLE);
                save_card.setVisibility(View.VISIBLE);
                video_name_card.setVisibility(View.VISIBLE);
                mPlayVideo.setVisibility(View.VISIBLE);

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private String parseVideo(String mFilePath) throws IOException {
        DataSource channel = new FileDataSourceImpl(mFilePath);
        IsoFile isoFile = new IsoFile(channel);
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        boolean isError = false;
        for (TrackBox trackBox : trackBoxes) {
            TimeToSampleBox.Entry firstEntry = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox().getTimeToSampleBox().getEntries().get(0);
            // Detect if first sample is a problem and fix it in isoFile
            // This is a hack. The audio deltas are 1024 for my files, and video deltas about 3000
            // 10000 seems sufficient since for 30 fps the normal delta is about 3000
            if (firstEntry.getDelta() > 10000) {
                isError = true;
                firstEntry.setDelta(3000);
            }
        }
        File file = getOutputMediaFile();
        String filePath = file.getAbsolutePath();
        if (isError) {
            Movie movie = new Movie();
            for (TrackBox trackBox : trackBoxes) {
                movie.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
            }
            movie.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());
            Container out = new DefaultMp4Builder().build(movie);

            //delete file first!
            FileChannel fc = new RandomAccessFile(filePath, "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
            Log.d(TAG, "Finished correcting raw video");
            return filePath;
        }
        return mFilePath;
    }

    /**
     * Create directory and return file
     * returning video file
     */
    private File getOutputMediaFile() {
        mediaFile = getCurrentFile();
        return mediaFile;

    }

    /**
     * Used to Delete the video if delete button pressed
     */
    private void deleteVideo(){
        File fileOrDirectory = mcontainer.getContext().getExternalFilesDir(VIDEO_DIRECTORY_NAME);
        deleteRecursievly(fileOrDirectory);
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

    private Boolean renameFile(String nameTo){
        System.out.println("VIDEO DIRECTORY NAME IS: "+ VIDEO_DIRECTORY_NAME);
        String name;
        if (nameTo.contains(".mp4")){
            name=nameTo;
        }else{
            name=nameTo+".mp4";
        }
        videoname = name;
        File to = new File(this.VIDEO_DIRECTORY_NAME+ "/"+name);
        if(mediaFile.exists()){
            mediaFile.renameTo(to);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    private void writeFirstFrame(){
        Bitmap bmp = getFirstFrame();
        try (FileOutputStream out = new FileOutputStream(VIDEO_DIRECTORY_NAME + "/first_frame.png")) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap getFirstFrame(){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

        mediaMetadataRetriever.setDataSource(mediaFile.getAbsolutePath());
        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(0);
        return bmFrame;
    }

    private boolean writeSummary(Boolean bool){
        File file = new File(VIDEO_DIRECTORY_NAME, "Summary.json");

        if(!file.exists()){
            try{
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                JSONObject jsonObject = getJSONSummary(bool);
                bufferedWriter.write(jsonObject.toString(5));
                bufferedWriter.close();
            }catch (IOException | JSONException ex){
                ex.printStackTrace();
                return false;
            }
        }
        else{
            return false;
        }
        return true;
    }

    private JSONObject getJSONSummary(Boolean bool) {
        JSONObject summary = new JSONObject();
        SimpleDateFormat formatter_date= new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatter_time= new SimpleDateFormat("HH:mm:ss");
        Date date_time = new Date(System.currentTimeMillis());
        String date = formatter_date.format(date_time);
        String time = formatter_time.format(date_time);

        IsoFile isoFile = null;
        try {
            if(!bool) {
                videoname = mediaFile.getName();
                isoFile = new IsoFile(mediaFile.getAbsolutePath());
            }
            else{
                isoFile = new IsoFile(VIDEO_DIRECTORY_NAME +"/"+videoname);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
        String duration = secToFormat(lengthInSeconds);


        try {
            summary.put("title", videoname);
            summary.put("detected", false);
            summary.put("uploaded", false);
            summary.put("geojson", "map_information.geojson");
            summary.put("fromAddress", locationAdapter.getFirstLocation());
            summary.put("toAddress", locationAdapter.getLastLocation());
            summary.put("image", "first_frame.png");
            summary.put("detected_path", "None");
            summary.put("username", this.username);
            summary.put("date", date);
            summary.put("time", time);
            summary.put("duration", duration);
            summary.put("fps", locationAdapter.getFps());
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

}