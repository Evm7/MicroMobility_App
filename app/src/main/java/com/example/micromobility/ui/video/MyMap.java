package com.example.micromobility.ui.video;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.micromobility.R;
import com.example.micromobility.ui.video.dashboard.FileHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


public class MyMap extends Fragment {

    private MapView mapView;
    private MapboxMap mapboxMap;

    private static final String TAG = "MarkerFollowingRoute";
    private static final String DOT_SOURCE_ID = "dot-source-id";
    private static final String LINE_SOURCE_ID = "line-source-id";
    private int count = 0;
    private Handler handler;
    private Runnable runnable;
    private GeoJsonSource dotGeoJsonSource;
    private ValueAnimator markerIconAnimator;
    private LatLng markerIconCurrentLocation;
    private List<Point> routeCoordinateList;
    private SymbolManager symbolManager;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";

    private EnableScroll callback;


    private boolean has_locations=false, blocked=true;

    private String path;
    private File file;
    private Float fps;
    private OnFragmentInteractionListener mListener;
    private JSONObject detections;

    private double center_lat, center_long;
    private int zoom = 10;

    private View mcontainer;


    // In order to mark the Road detection in each color
    private TreeMap<Integer, String> road_detections_map;


    public MyMap(){
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyMap newInstance(String[] param1, String param2, String param3, String param4) {
        MyMap fragment = new MyMap();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM4, param3);

        if (param3.equals("None")){
            fragment.setArguments(args);
            return fragment;
        }
        args.putString(ARG_PARAM1, param1[1]);
        args.putString(ARG_PARAM2, param1[0]);
        args.putString(ARG_PARAM3, param2);
        args.putString(ARG_PARAM5, param4);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            has_locations=!getArguments().getString(ARG_PARAM4).equals("None");
            if (has_locations) {
                center_lat = Double.parseDouble(getArguments().getString(ARG_PARAM1));
                center_long = Double.parseDouble(getArguments().getString(ARG_PARAM2));
                path = getArguments().getString(ARG_PARAM3);
                fps = Float.parseFloat(getArguments().getString(ARG_PARAM5));
            }

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(requireActivity(), getString(R.string.acces_token));
        this.callback = (EnableScroll) getActivity();
        mcontainer = inflater.inflate(R.layout.videomap_fragment, container, false);
        file = new File(mcontainer.getContext().getExternalFilesDir(path),"map_information.geojson");
        if(!file.exists()){
            Toast.makeText(getContext(), "Error while uploading map : map file not found", Toast.LENGTH_SHORT).show();
        }

        return mcontainer;
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

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mapView = mcontainer.findViewById(R.id.mapViewVideo);
        if (!has_locations){
            zoom=0;
            center_lat=0;
            center_long=0;
        }
        MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null)
                .camera(new CameraPosition.Builder()
                        .target(new LatLng(center_lat, center_long))
                        .zoom(zoom)
                        .build());
        mapView.onCreate(null);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(center_lat, center_long))
                        .zoom(zoom)
                        .build());

                MyMap.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                        // Adding route of JSON

                        setSettings();

                        if(has_locations) {
                            new LoadGeoJson(MyMap.this, file).execute();
                            // create symbol manager object
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);

                            // add click listeners if desired
                            symbolManager.addClickListener(symbol -> {

                            });

                            symbolManager.addLongClickListener(symbol -> {

                            });

                            // set non-data-driven properties, such as:
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.setIconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_VIEWPORT);
                            addMarker(center_lat, center_long, R.drawable.ic_place_, "Starter");
                            getDetections();
                        }
                        else{
                            Toast.makeText(mcontainer.getContext(), "Add its geojson map", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void setSettings(){
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setLogoEnabled(true);
        RelativeLayout information = mcontainer.findViewById(R.id.information_btn);
        RelativeLayout legend = mcontainer.findViewById(R.id.legend);
        RelativeLayout block = mcontainer.findViewById(R.id.stop_btn);
        ImageView imageView = block.findViewById(R.id.locker);

        information.setOnClickListener(v -> {
            information.setVisibility(View.GONE);
            legend.setVisibility(View.VISIBLE);
        });
        legend.setOnClickListener(v -> {
            information.setVisibility(View.VISIBLE);
            legend.setVisibility(View.GONE);
        });
        block.setOnClickListener(v -> {
            blocked = !blocked;
            this.callback.enableScrolling(blocked);
            if (blocked){
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_black_24dp));
                Toast.makeText(mcontainer.getContext(), "Scrolled unlocked", Toast.LENGTH_SHORT).show();
            }else{
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_black_24dp));
                Toast.makeText(mcontainer.getContext(), "Scrolled locked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getDetections(){
        System.out.println("The path for the video directory is: "+ path);
        FileHelper fileHelper = new FileHelper(mcontainer.getContext(), path);
        detections = fileHelper.readManualFile("None");
    }

    public void getRoadClassifer(){
        try {
            JSONArray road_classifier = detections.getJSONArray("Type of Road");
            road_detections_map =new TreeMap<>();

            for(int i =0 ; i<road_classifier.length();i++){
                JSONObject jsonObject = road_classifier.getJSONObject(i);
                int time = (int) ((int) (jsonObject.getLong("Time")) /this.fps);
                road_detections_map.put( time, jsonObject.getString("Value"));
            }
        } catch (JSONException | NullPointerException e) {
            Toast.makeText(mcontainer.getContext(), "Video has not been detected yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void computeCenter(){
        // We define the values border of the Latitude and Longitude, so always our location will be in the limits.
        double minLat=90, maxLat=0, minLon=180, maxLon=-180;

        // We are computing a bounding box between all the markers, reducing the box to the maximum viewable
        // Then, we will compute its center and the zoom.
        if (routeCoordinateList == null){
            return;
        }
        for (Point point : routeCoordinateList)
        {

            double lat = point.latitude();
            double lon = point.longitude();

            maxLat = Math.max(lat, maxLat);
            minLat = Math.min(lat, minLat);
            maxLon = Math.max(lon, maxLon);
            minLon = Math.min(lon, minLon);
        }

        center_lat = (maxLat + minLat)/2;
        center_long = (maxLon + minLon)/2;

        /*
         To compute the zoom we are taking into account the differnece in Longitude degress.
            - Zoom in MapBox range from 0 to 22, being 0 all earth and 15 building sites.
            - As we have seen, we need to be moving from 0 to 15 in order to be able to compute right zooms.
            - It is important to note that we are just taking into account the first position as it was it displays in this map.
         */
        zoom = computeRightZoom(maxLon, minLon);


        CameraPosition position =new CameraPosition.Builder()
                .target(new LatLng(center_lat, center_long))
                .zoom(zoom)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000);
        /*
        mapController.zoomToSpan(Math.abs(maxLat - minLat), Math.abs(maxLon - minLon));
        mapController.animateTo(new GeoPoint( (maxLat + minLat)/2,
                 ));

         */
    }
    private int computeRightZoom(Double maxLon, Double minLon){
        double longitude_wide = maxLon - minLon;
        Float maxZoom = log(360/longitude_wide, 2) - 2;
        Float minZoom = log(360/longitude_wide, 2) -1 ;
        int maxZoom_i = Math.round(maxZoom);
        int minZoom_i = Math.round(minZoom);

        // We have to create some rules such as the maximum Zoom is 18 --> for our purpose because more
        // than that is too much zoomed; and the minimum is 0;
        if ((Math.abs(minZoom_i - minZoom))>= (Math.abs(maxZoom_i -maxZoom))){
            if ((maxZoom_i<=18) && (maxZoom_i>=0)) {
                return maxZoom_i;
            }
            else{
                return 18;
            }
        }
        else{
            if (minZoom_i>=0) {
                return minZoom_i;
            }
        }
        return 0;
    }

    private Float log(Double x, int base)
    {
        return (float) (Math.log(x) / Math.log(base));
    }


    private void addMarker(Double lat, double lon, Integer drawable, String name){
        mapboxMap.getStyle().addImage(name, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(drawable)));
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(lat, lon))
                .withIconImage(name)
                .withIconSize(1f));
    }



    private void addLines(){
        getRoadClassifer();
        if(road_detections_map== null){
            return;
        }
        Iterator iterator = road_detections_map.entrySet().iterator();
        int last_frame= 0;
        int current_frame=0;
        // Iterate through the hashmap
        while (iterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry) iterator.next();
            String title = (String) mapElement.getValue();
            current_frame = (int) mapElement.getKey();
            addLine(getLineMarker(title, last_frame, current_frame));
            last_frame = current_frame;
        }
    }

    private void addLine(LineMarker lineMarker){
        System.out.println("Adding line for :"+ lineMarker.getIdentifier());
        System.out.println("LineMarker points are :"+ lineMarker.getPoints());

        mapboxMap.getStyle().addSource(new GeoJsonSource(lineMarker.getIdentifier(), lineMarker.getPoints()));

        mapboxMap.getStyle().addLayer(new LineLayer(lineMarker.getIdentifier(), lineMarker.getIdentifier()).withProperties(
                lineColor(lineMarker.getColor()),
                lineWidth(4f)
        ));
    }

    private LineMarker getLineMarker(String title, int current_frame, int last_frame){
        List<Point> listpoint = new ArrayList<>();

        for(int i = current_frame; (i<routeCoordinateList.size()) && (i<last_frame); i++){
            listpoint.add(routeCoordinateList.get(i));
        }
        List<Feature> directionsRouteFeatureList = new ArrayList<>();
        directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(listpoint)));
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(directionsRouteFeatureList);
        return new LineMarker(title,current_frame, last_frame, featureCollection);
    }


    /**
     * Add data to the map once the GeoJSON has been loaded
     *
     * @param featureCollection returned GeoJSON FeatureCollection from the async task
     */
    private void initData(@NonNull FeatureCollection featureCollection) {
        System.out.println("Initializating data "+ featureCollection.features().toString());
        if (featureCollection.features() != null) {
            LineString lineString = (LineString) featureCollection.features().get(0).geometry();
            System.out.println("LineString is "+ lineString.toJson());
            if (lineString != null) {
                routeCoordinateList = lineString.coordinates();
                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        initSources(style, featureCollection);
                        initSymbolLayer(style);
                        initDotLinePath(style);
                        addLines();
                        initRunnable();
                    });
                }
                computeCenter();

            }
        }
    }

    /**
     * Set up the repeat logic for moving the icon along the route.
     */
    private void initRunnable() {
        // Animating the marker requires the use of both the ValueAnimator and a handler.
        // The ValueAnimator is used to move the marker between the GeoJSON points, this is
        // done linearly. The handler is used to move the marker along the GeoJSON points.
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Check if we are at the end of the points list, if so we want to stop using
                // the handler.
                if ((routeCoordinateList.size() - 1 > count)) {

                    Point nextLocation = routeCoordinateList.get(count + 1);

                    if (markerIconAnimator != null && markerIconAnimator.isStarted()) {
                        markerIconCurrentLocation = (LatLng) markerIconAnimator.getAnimatedValue();
                        markerIconAnimator.cancel();
                    }
                    markerIconAnimator = ObjectAnimator
                            .ofObject(latLngEvaluator, count == 0 || markerIconCurrentLocation == null
                                            ? new LatLng(center_lat, center_long)
                                            : markerIconCurrentLocation,
                                    new LatLng(nextLocation.latitude(), nextLocation.longitude()))
                            .setDuration((long) (1000/fps));
                            //.setDuration((long) (300));
                    markerIconAnimator.setInterpolator(new LinearInterpolator());

                    markerIconAnimator.addUpdateListener(animatorUpdateListener);
                    markerIconAnimator.start();

                    // Keeping the current point count we are on.
                    count++;

                    // Once we finish we need to repeat the entire process by executing the
                    // handler again once the ValueAnimator is finished.
                    handler.postDelayed(this, (long) (1000/fps));
                    //handler.postDelayed(this, 300));
                }
            }
        };
        handler.post(runnable);
    }

    /**
     * Listener interface for when the ValueAnimator provides an updated value
     */
    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    if (dotGeoJsonSource != null) {
                        dotGeoJsonSource.setGeoJson(Point.fromLngLat(
                                animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                    }
                }
            };

    /**
     * Add various sources to the map.
     */
    private void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
        dotGeoJsonSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection);
        loadedMapStyle.addSource(dotGeoJsonSource);
        loadedMapStyle.addSource(new GeoJsonSource(LINE_SOURCE_ID, featureCollection));
    }


    /**
     * Add the marker icon SymbolLayer.
     */
    private void initSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("moving-pink-dot", BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.pink_dot)));

        loadedMapStyle.addLayer(new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID).withProperties(
                iconImage("moving-pink-dot"),
                iconSize(0.3f),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }


    /**
     * Add the LineLayer for the marker icon's travel route.
     */
    private void initDotLinePath(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new LineLayer("line-layer-id", LINE_SOURCE_ID).withProperties(
                lineColor(Color.parseColor("#C233FF")),
                lineWidth(4f)
        ));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }





    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // When the activity is resumed we restart the marker animating.
        if (handler != null && runnable != null) {
            handler.post(runnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        // Check if the marker is currently animating and if so, we pause the animation so we aren't
        // using resources when the activities not in view.
        if (handler != null && runnable != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (markerIconAnimator != null) {
            markerIconAnimator.cancel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }


    /**
     * We want to load in the GeoJSON file asynchronous so the UI thread isn't handling the file
     * loading. The GeoJSON file we are using is stored in the assets folder, you could also get
     * this information from the Mapbox mapboxMap matching API during runtime.
     */
    private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

        private WeakReference<MyMap> weakReference;
        private File file;

        LoadGeoJson(MyMap activity, File file) {
            this.weakReference = new WeakReference<>(activity);
            this.file = file;
        }

        @Override
        protected FeatureCollection doInBackground(Void... voids) {
            try {
                MyMap fragment = weakReference.get();
                if (fragment != null) {
                    InputStream inputStream = new FileInputStream(file);
                    /*
                    System.out.println("JSON FILE:" + convertStreamToString(inputStream2));
                    InputStream inputStream = fragment.getActivity().getAssets().open("matched_route.geojson");
                    System.out.println("JSON :" + convertStreamToString(inputStream));
                    FeatureCollection featureCollection= FeatureCollection.fromJson(convertStreamToString(inputStream));
                    */
                    return  FeatureCollection.fromJson(convertStreamToString(inputStream));
                }
            } catch (Exception exception) {
                Timber.e(exception.toString());
            }
            return null;
        }

        static String convertStreamToString(InputStream is) {
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        @Override
        protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            MyMap activity = weakReference.get();
            if (activity != null && featureCollection != null) {
                activity.initData(featureCollection);
            }
        }
    }

    /**
     * Method is used to interpolate the SymbolLayer icon animation.
     */
    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    class LineMarker {
        private String title, identifier;
        private int frame_start, frame_end;
        private FeatureCollection points;
        private int color;

        public LineMarker(String title, int frame_start, int frame_end, FeatureCollection points) {
            this.frame_start = frame_start;
            this.frame_end = frame_end;
            this.title=title;
            this.points = points;
            this.identifier = this.title +":"+frame_start+"-"+ frame_end;
            setColor();
        }

        public int getColor() {
            return color;
        }

        public String getIdentifier() {
            return identifier;
        }

        public FeatureCollection getPoints() {
            return points;
        }

        public int getFrame_end() {
            return frame_end;
        }

        public int getFrame_start() {
            return frame_start;
        }

        public String getTitle() {
            return title;
        }

        private void setColor(){
            if(title.equals("SideWalk")){
                this.color = Color.parseColor("#335DFF");
            }else if (title.equals("Bidirectional")){
                this.color = Color.parseColor("#6CFF33");
            }else if (title.equals("Unidirectional")){
                this.color = Color.parseColor("#EDFF33");
            }else if (title.equals("CrossWalk")){
                this.color = Color.parseColor("#33FFF4");
            }else if (title.equals("Unknown")){
                this.color = Color.parseColor("#C233FF");
            }else if (title.equals("Road")){
                this.color = Color.parseColor("#FF335B");
            }else{
                this.color = Color.parseColor("#C233FF");
            }
        }
    }

    public interface EnableScroll{
        void enableScrolling(Boolean enable);
    }
}

