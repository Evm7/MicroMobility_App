package com.example.micromobility.ui.profile;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.example.micromobility.R;
import com.example.micromobility.Storage.InternalStorage;
import com.example.micromobility.ui.profile.Adapters.Item;
import com.example.micromobility.ui.video.MyMap;
import com.example.micromobility.ui.video.VideoActivity;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.os.AsyncTask;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import androidx.fragment.app.Fragment;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


public class MyMapFragment extends Fragment {

    private MapView mapView;
    private MapboxMap mapboxMap;

    private static final String TAG = "MarkerFollowingRoute";
    private static final String DOT_SOURCE_ID = "dot-source-id";
    private static final String LINE_SOURCE_ID = "line-source-id";
    private static final String  MARKER_LAYER_ID = "marker-source-id";
    private int count = 0;
    private Handler handler;
    private Runnable runnable;
    private GeoJsonSource dotGeoJsonSource;
    private ValueAnimator markerIconAnimator;
    private LatLng markerIconCurrentLocation;
    private List<Point> routeCoordinateList;
    private SymbolManager symbolManager;

    private String username;

    private OnFragmentInteractionListener mListener;

    private double center_lat = 0, center_long = 0;
    private int zoom = 0;
    private InternalStorage in;

    private View mcontainer;

    private EnableScroll callback;
    private boolean blocked=true;



    public MyMapFragment() {
        // Required empty public constructor
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(requireActivity(), getString(R.string.acces_token));
        this.callback = (MyMapFragment.EnableScroll) getParentFragment();
        mcontainer = inflater.inflate(R.layout.mymap_fragment, container, false);

        in = new InternalStorage(mcontainer.getContext());
        username = in.getUsername();


        return mcontainer;
    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mapView = mcontainer.findViewById(R.id.mapView);
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

                MyMapFragment.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments.

                        // create symbol manager object
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(false);
                        symbolManager.setTextAllowOverlap(false);

                        // add click listeners if desired
                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public void onAnnotationClick(Symbol symbol) {
                                String imagename =symbol.getIconImage();
                                if (imagename.endsWith("avatar")){
                                    symbol.setIconImage(imagename.replace("avatar",""));

                                }else {
                                    symbol.setIconImage(imagename+"avatar");
                                    Toast.makeText(mcontainer.getContext(),"Maintain to visit video",Toast.LENGTH_SHORT).show();
                                }
                                symbolManager.update(symbol);

                            }
                        });

                        symbolManager.addLongClickListener(symbol -> {
                            String imagename =symbol.getIconImage();
                            if (imagename.endsWith("avatar")) {
                                imagename = imagename.replace("avatar", "");
                            }
                            Intent intent = new Intent(getContext(), VideoActivity.class);
                            Bundle b = new Bundle();
                            b.putString("Item", imagename); //Your id
                            intent.putExtras(b); //Put your id to your next Intent
                            startActivity(intent);

                        });

                        // set non-data-driven properties, such as:
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setIconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_VIEWPORT);
                        addAllMarkers();

                        setUIoptions();
                    }
                });
            }
        });
    }

    private void setUIoptions(){
        RelativeLayout block = mcontainer.findViewById(R.id.stop_btn);
        ImageView imageView = block.findViewById(R.id.locker);
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

    private void addMarker(MarkerSimple markerSimple){
        mapboxMap.getStyle().addImage(markerSimple.getPath(), BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.ic_place_)));
        mapboxMap.getStyle().addImage(markerSimple.getPath()+"avatar",markerSimple.getAvatar());
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(markerSimple.getLatitude(), markerSimple.getLongitude()))
                .withIconImage(markerSimple.getPath())
                .withIconSize(1f));
    }

    private void addAllMarkers(){
        ArrayList<MarkerSimple> markers = navegateVideos();
        getCenter(markers);
        for(MarkerSimple markerSimple: markers){
            addMarker(markerSimple);
        }


    }

    private void getCenter(ArrayList<MarkerSimple> markers){
        // We define the values border of the Latitude and Longitude, so always our location will be in the limits.
        double minLat=90, maxLat=0, minLon=180, maxLon=-180;

        // We are computing a bounding box between all the markers, reducing the box to the maximum viewable
        // Then, we will compute its center and the zoom.
        for (MarkerSimple marker : markers)
        {

            double lat = marker.getLatitude();
            double lon = marker.getLongitude();

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

        System.out.println("Zoom choosen is " + zoom);
        System.out.println("Center Lat is " + center_lat);
        System.out.println("Center Long is " + center_long);


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
    private int computeRightZoom(Double maxLon, Double minLon) {
        double longitude_wide = maxLon - minLon;
        Float maxZoom = log(360 / longitude_wide, 2) - 2;
        Float minZoom = log(360 / longitude_wide, 2) - 1;
        int maxZoom_i = Math.round(maxZoom);
        int minZoom_i = Math.round(minZoom);

        // We have to create some rules such as the maximum Zoom is 18 --> for our purpose because more
        // than that is too much zoomed; and the minimum is 0;
        if ((Math.abs(minZoom_i - minZoom)) >= (Math.abs(maxZoom_i - maxZoom))) {
            if ((maxZoom_i <= 18) && (maxZoom_i >= 0)) {
                return maxZoom_i;
            } else {
                return 18;
            }
        } else {
            if (minZoom_i >= 0) {
                return minZoom_i;
            }
        }
        return 0;
    }

    private Float log(Double x, int base)
    {
        return (float) (Math.log(x) / Math.log(base));
    }


    private ArrayList<MarkerSimple> navegateVideos() {
        File dir = mcontainer.getContext().getExternalFilesDir("Micromobility"+"/"+username);
        ArrayList<MarkerSimple> markers = traverseAll(dir);
        return markers;
    }

    // Iterate through all the videos directories
    public ArrayList<MarkerSimple> traverseAll (File dir) {
        ArrayList<MarkerSimple> markers = new ArrayList<>();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    MarkerSimple markerSimple =traverseVideo(file);
                    if (markerSimple !=null){
                        markers.add(markerSimple);
                    }
                } else {
                    // do something here with the file
                    System.out.println("ERROR: There should not be any FILE in here");
                }
            }
        }
        return markers;
    }

    // Iterate through all the files in each video
    private MarkerSimple traverseVideo (File dir) {
        // Create a path where we will place our private file on external
        // storage.
        File summary = new File( dir, "Summary.json");
        if (!summary.exists()){
            Toast.makeText(mcontainer.getContext(), "Error while retrieving information on file "+ dir.getName(), Toast.LENGTH_SHORT).show();
        }else{
            JSONObject jsonObject = parseFile(summary);

            try{
                String title = jsonObject.get("title").toString();
                String[] fromAddress = getStringLocation(jsonObject.get("fromAddress").toString());
                if (fromAddress==null){
                    return null;
                }
                String image_path = jsonObject.get("image").toString();
                //String directory_path = jsonObject.get("directory_path").toString();
                Bitmap bitmap = BitmapFactory.decodeFile(dir.getAbsolutePath()+"/"+image_path);
                String username = jsonObject.get("username").toString();
                if (!username.equals(this.username)){
                    System.out.println("Error in usernames. Getting file from "+ username + " as user "+ this.username);
                    return null;
                }
                MarkerSimple marker = new MarkerSimple(title, Double.parseDouble(fromAddress[0]), Double.parseDouble(fromAddress[1]), bitmap, dir.getAbsolutePath());
                return marker;
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

    private String[] getStringLocation(String jsonObject){
        try {
            JSONArray jsonArray = new JSONArray(jsonObject);
            JSONObject loc = jsonArray.getJSONObject(0);
            String[] loc_string = new String[2];
            loc_string[1] = loc.getString("Longitude");
            loc_string[0] = loc.getString("Latitude");
            if(loc_string[0].equals("None")){
                return null;
            }
            return loc_string;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    class MarkerSimple {
        private double latitude, longitude;
        private String title, path;
        private Bitmap avatar;

        public MarkerSimple(String title, double lat, double lon, Bitmap avatar, String path) {
            latitude = lat;
            longitude = lon;
            this.title=title;
            this.path = path;
            this.avatar=avatar;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getTitle() {
            return title;
        }

        public Bitmap getAvatar() {
            return avatar;
        }

        public String getPath() {
            return path;
        }
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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

    public interface EnableScroll{
        void enableScrolling(Boolean enable);
    }

}

