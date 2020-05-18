package com.example.micromobility.ui.profile.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.micromobility.MainActivity;
import com.example.micromobility.R;
import com.example.micromobility.Upload.UploadHelper;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLocation {

    public static final int GEOJSON_REQUEST_CODE = 107;
    private static final String TAG = "AddLocation";
    android.app.AlertDialog alertDialog;
    AlertDialog alertDialog2;
    private static final String GENERAL_DIRECTORY_NAME = "MicroMobility";
    private static String VIDEO_DIRECTORY_NAME;
    private String video_name;

    private ProcessCallback mCallback;


    private UploadHelper uploadHelper;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private SymbolManager symbolManager;
    private Symbol sourceMarker, destinationMarker;


    private LatLng sourcePoint = null, destinationPoint = null;
    private FeatureCollection dashedLineDirectionsFeatureCollection;
    private List<Point> coordinates;
    TextView origin_text;
    TextView destination_text ;
    ToggleButton origin_btn ;
    ToggleButton destination_btn ;
    Button set_location ;
    ImageButton go_home_btn;
    private Context context;
    private Activity activity;
    private String username;


    public AddLocation(Context context, Activity activity, String username, ProcessCallback mCallback) {
        this.context=context;
        this.activity=activity;
        this.username = username;
        uploadHelper = new UploadHelper(username, context, TAG);
        this.mCallback=mCallback;
    }

    public void setVideoDirectoryName(String videoDirectoryName) {
        String video_path = videoDirectoryName.split("/")[videoDirectoryName.split("/").length-1];
        VIDEO_DIRECTORY_NAME = GENERAL_DIRECTORY_NAME + File.separator + this.username + File.separator + video_path;
        this.uploadHelper.setVideoDirectoryName(VIDEO_DIRECTORY_NAME);
        this.uploadHelper.setVideo_name(video_name);
    }

    public void setVideo_name(String video_name){
        this.video_name=video_name;
    }

    public boolean addLocation(String directory_path, String video_name){
        setVideoDirectoryName(directory_path);
        this.video_name=video_name;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setView(this.activity.getLayoutInflater().inflate(R.layout.select_video, null));

        alertDialog = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {

                LinearLayout upload_geojson = alertDialog.findViewById(R.id.upload_geojson);
                LinearLayout no_geojson = alertDialog.findViewById(R.id.no_geojson);
                LinearLayout map_geojson = alertDialog.findViewById(R.id.map_geojson);
                ImageView information = alertDialog.findViewById(R.id.information_geojson);
                Button cancel_progress = alertDialog.findViewById(R.id.cancel_upload);


                cancel_progress.setOnClickListener(v -> {
                    Toast.makeText(context, "Video upload has been cancelled", Toast.LENGTH_SHORT).show();
                    alertDialog.cancel();

                });
                information.setOnClickListener(v -> {
                    Toast.makeText(context, "Click on the images below to proceed", Toast.LENGTH_SHORT).show();

                });
                map_geojson.setOnClickListener(v -> {
                    Toast.makeText(context, "Select the locations from our map", Toast.LENGTH_SHORT).show();
                    uploadHelper.setType("Map");
                    alertDialog.cancel();
                    getGeoJsonFromMap();

                });
                upload_geojson.setOnClickListener(v -> {
                    Toast.makeText(context, "Upload the geojson file with the locations", Toast.LENGTH_SHORT).show();
                    uploadHelper.setVideo_name(video_name);
                    mCallback.onProcessCallback("Upload", uploadHelper);
                    alertDialog.cancel();
                });
                no_geojson.setOnClickListener(v -> {
                    Toast.makeText(context, "Video saved without GeoJSON", Toast.LENGTH_SHORT).show();
                    alertDialog.cancel();
                    uploadHelper.clearCache();
                });

            }
        });
        alertDialog.show();
        return false;
    }

    private void getGeoJsonFromMap(){
        Mapbox.getInstance(context, activity.getString(R.string.acces_token));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(activity.getLayoutInflater().inflate(R.layout.alert_map, null));

        alertDialog2 = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog2.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {

                origin_text = alertDialog2.findViewById(R.id.origin_location);
                destination_text = alertDialog2.findViewById(R.id.destiation_location);
                origin_btn = alertDialog2.findViewById(R.id.origin_btn);
                destination_btn = alertDialog2.findViewById(R.id.destination_btn);
                set_location = alertDialog2.findViewById(R.id.setRoute);
                go_home_btn = alertDialog2.findViewById(R.id.go_home);

                origin_btn.setOnClickListener(v -> {
                    origin_btn.setChecked(true);
                    origin_btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    destination_btn.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    destination_btn.setChecked(false);
                });

                destination_btn.setOnClickListener(v -> {
                    destination_btn.setChecked(true);
                    destination_btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    origin_btn.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    origin_btn.setChecked(false);
                });


                mapView = alertDialog2.findViewById(R.id.alert_mapView);
                MapboxMapOptions options = MapboxMapOptions.createFromAttributes(context, null)
                        .camera(new CameraPosition.Builder()
                                .target(new LatLng(0, 0))
                                .zoom(0)
                                .build());
                mapView.onCreate(null);

                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull MapboxMap mapboxMap) {
                        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(0, 0))
                                .zoom(0)
                                .build());

                        AddLocation.this.mapboxMap = mapboxMap;
                        mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                                // Adding route of JSON

                                // Add the  layer for the dashed directions route line
                                initDottedLineSourceAndLayer(style);
                                symbolManager = new SymbolManager(mapView, mapboxMap, style);

                                AddLocation.this.mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                                    @Override
                                    public boolean onMapClick(@NonNull LatLng point) {
                                        String lon = ""+ point.getLongitude();
                                        String lat = ""+ point.getLatitude();

                                        if(origin_btn.isChecked()){
                                            origin_text.setText(lat +" , "+ lon);
                                            if (sourceMarker!=null){
                                                symbolManager.delete(sourceMarker);
                                            }
                                            sourceMarker = addMarker(point.getLatitude(), point.getLongitude(), R.drawable.ic_place_origin, "Origin");
                                            sourcePoint = point;

                                        }
                                        else if(destination_btn.isChecked()){
                                            destination_text.setText(lat +" , "+ lon);
                                            if (destinationMarker!=null){
                                                symbolManager.delete(destinationMarker);
                                            }
                                            destinationMarker = addMarker(point.getLatitude(), point.getLongitude(), R.drawable.ic_place_destination, "Destination");
                                            destinationPoint = point;
                                        }

                                        if (destinationPoint != null && sourcePoint != null){
                                            getRoute();
                                            set_location.setClickable(true);
                                        }
                                        else{
                                            set_location.setClickable(false);

                                        }
                                        return true;
                                    }
                                });
                            }
                        });
                    }
                });

                set_location.setOnClickListener(v -> {
                    Toast.makeText(context, "GeoJson file created. Video correctly uploaded", Toast.LENGTH_SHORT).show();
                    uploadHelper.setFeatureCollection(dashedLineDirectionsFeatureCollection);
                    uploadHelper.setPointList(AddLocation.this.coordinates);
                    uploadHelper.setType("Map");
                    uploadHelper.setVideo_name(video_name);
                    uploadHelper.writeFeatureCollectionFile();
                    uploadHelper.writeSummary();
                    alertDialog2.cancel();

                });
                go_home_btn.setOnClickListener(v -> {
                    Toast.makeText(context, "Select GeoJson Format", Toast.LENGTH_SHORT).show();
                    alertDialog2.cancel();
                });
            }
        });
        alertDialog2.show();
    }

    private Symbol addMarker(Double lat, double lon, Integer drawable, String name){
        mapboxMap.getStyle().addImage(name, BitmapUtils.getBitmapFromDrawable(
                activity.getResources().getDrawable(drawable)));
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(lat, lon))
                .withIconImage(name)
                .withIconSize(1f));
        return symbol;
    }

    /**
     * Set up a GeoJsonSource and LineLayer in order to show the directions route from the device location
     * to the place picker location
     */
    private void initDottedLineSourceAndLayer(@NonNull Style loadedMapStyle) {
        dashedLineDirectionsFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {});
        loadedMapStyle.addSource(new GeoJsonSource("SOURCE_ID", dashedLineDirectionsFeatureCollection));
        loadedMapStyle.addLayer(
                new LineLayer(
                        "DIRECTIONS_LAYER_ID", "SOURCE_ID").withProperties(
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Color.WHITE),
                        PropertyFactory.lineTranslate(new Float[] {0f, 4f}),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                ));
    }

    private void getRoute(){
        System.out.println("Setting route");
        MapboxDirections client = MapboxDirections.builder()
                .origin(Point.fromLngLat(AddLocation.this.sourcePoint.getLongitude(), AddLocation.this.sourcePoint.getLatitude()))
                .destination(Point.fromLngLat(AddLocation.this.destinationPoint.getLongitude(), AddLocation.this.destinationPoint.getLatitude()))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_CYCLING)
                .accessToken(activity.getString(R.string.acces_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                System.out.println("Computing route");
                // Retrieve the directions route from the API response
                drawNavigationPolylineRoute(response.body().routes().get(0));
            }

            @Override public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {

                Log.e(TAG, "Error: " + throwable.getMessage());

            }
        });
    }

    /**
     * Update the GeoJson data that's part of the LineLayer.
     *
     * @param route The route to be drawn in the map's LineLayer that was set up above.
     */
    private void drawNavigationPolylineRoute(final DirectionsRoute route) {
        if (mapboxMap != null) {
            System.out.println("Drawing navigation Poly Line Route");
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    List<Feature> directionsRouteFeatureList = new ArrayList<>();
                    LineString lineString = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6);
                    coordinates = lineString.coordinates();
                    for (int i = 0; i < coordinates.size(); i++) {
                        directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
                    }
                    dashedLineDirectionsFeatureCollection = FeatureCollection.fromFeatures(directionsRouteFeatureList);
                    GeoJsonSource source = style.getSourceAs("SOURCE_ID");
                    if (source != null) {
                        System.out.println("Source is not null yes sir");
                        source.setGeoJson(lineString);
                    }
                }
            });
        }
    }


    public interface ProcessCallback {
        void onProcessCallback(String argument, UploadHelper video);
    }
}
