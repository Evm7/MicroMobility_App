package com.example.micromobility;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.micromobility.Camera.CameraActivity;
import com.example.micromobility.Language.SetLanguage;
import com.example.micromobility.Storage.InternalStorage;
import com.example.micromobility.Upload.UploadHelper;
import com.example.micromobility.configuration.ConfigurationSystem;
import com.example.micromobility.ui.home.HomeFragment;
import com.example.micromobility.ui.profile.MyMapFragment;
import com.example.micromobility.ui.profile.ProfileFragment;
import com.example.micromobility.ui.video.MyMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.BoundingBox;
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
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements MyMapFragment.OnFragmentInteractionListener {
    private static final int SELECT_VIDEO_REQUEST = 103;
    private static final int GEOJSON_REQUEST_CODE = 77;
    private String username;
    private BottomNavigationView bottomNavigationView;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "MainActivity";
    AlertDialog alertDialog, alertDialog2;
    private static final String GENERAL_DIRECTORY_NAME = "MicroMobility";
    private static String VIDEO_DIRECTORY_NAME;
    private String video_name;

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.micromobility);
        if (getIntent().getBooleanExtra("LOGOUT", false)) {
            finish();
        }

        //get USERNAME AND PHOTO --> configurate Language
        InternalStorage in = new InternalStorage(MainActivity.this);
        username = in.getUsername();
        String language = in.getValue(username, "language");
        System.out.println(language);
        if (language.startsWith("Spa") || language.startsWith("Es")) {
            SetLanguage.setLocale(MainActivity.this, "es");
        } else {
            SetLanguage.setLocale(MainActivity.this, "en");
        }

        // Initialize helper
        uploadHelper = new UploadHelper(username, MainActivity.this, TAG);

        // Set the toolbar Options
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        Fragment fragment = new ConfigurationSystem();
                        loadFragment(fragment, "settings");
                        return false;
                    case R.id.action_upload:
                        selectVideoFromGallery();
                }
                return false;
            }
        });


        // Set the Botthom Navigation View
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        getSupportActionBar().show();
                        setToolbar(R.string.title_home, "\uD83C\uDFE0");
                        fragment = new HomeFragment();
                        loadFragment(fragment, "Home");
                        break;
                    case R.id.navigation_profile:
                        setToolbar(R.string.title_profile, "\uD83D\uDC64");
                        fragment = new ProfileFragment();
                        /*
                        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
                            MainActivity.super.onBackPressed();
                        }

                         */
                        loadFragment(fragment, "Profile");


                        break;
                    case R.id.navigation_camera:
                        getSupportActionBar().show();
                        if (checkLocationPermission()) {
                            Log.d(TAG, "Due to Caché memory, directly entered to Main");
                            startActivity(new Intent(MainActivity.this, CameraActivity.class));
                        }
                        break;
                }
                checkLastTask();
                return true;
            }
        });

        // Default fragment
        if (savedInstanceState == null) {
            Fragment fragment = new HomeFragment();
            loadFragment(fragment, "Home");
            setToolbar(R.string.title_home, "\uD83C\uDFE0");
            bottomNavigationView.setSelectedItemId(R.id.home);
        }
    }

    public void selectVideoFromGallery() {
        Intent intent;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, SELECT_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_VIDEO_REQUEST) {
                    Uri selectedvideo = data.getData();
                    String videopath = selectedvideo.getPath();
                    InputStream inputStream = getContentResolver().openInputStream(selectedvideo);

                    Log.e("Video Path", videopath);
                    uploadHelper.setSourceVideo(inputStream);
                    getGeoJson();
                }
                if (requestCode == GEOJSON_REQUEST_CODE) {
                    Uri geojsonuri = data.getData();
                    String geojsonpath = geojsonuri.getPath();
                    InputStream inputStream = getContentResolver().openInputStream(geojsonuri);
                    Log.e("GeoJson path", geojsonpath);
                    boolean bool = uploadHelper.setGeoJsonFile(inputStream);
                    if(bool==false){
                        Toast.makeText(this, "Format of file incorrect. Try again", Toast.LENGTH_SHORT).show();
                        uploadHelper.clearCacheGeoJson();
                    }else{
                        Toast.makeText(this, "Uploading file", Toast.LENGTH_SHORT).show();
                        uploadHelper.writeAll();
                        alertDialog.cancel();
                    }
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Failed to load the video. Try again", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean getGeoJson() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.select_video, null));

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
                    Toast.makeText(MainActivity.this, "Video upload has been cancelled", Toast.LENGTH_SHORT).show();
                    alertDialog.cancel();

                });
                information.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "Click on the images below to proceed", Toast.LENGTH_SHORT).show();

                });
                map_geojson.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "Select the locations from our map", Toast.LENGTH_SHORT).show();
                    uploadHelper.setType("Map");
                    alertDialog.cancel();
                    getGeoJsonFromMap();

                });
                upload_geojson.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "Upload the geojson file with the locations", Toast.LENGTH_SHORT).show();
                    uploadHelper.setType("Upload");
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    //intent.setType("application/geo+json|application/json|text/plain");
                    startActivityForResult(intent, GEOJSON_REQUEST_CODE);


                });
                no_geojson.setOnClickListener(v -> {
                    uploadHelper.setType("None");
                    uploadHelper.writeAll();
                    Toast.makeText(MainActivity.this, "Video saved without GeoJSON", Toast.LENGTH_SHORT).show();
                    alertDialog.cancel();
                    uploadHelper.clearCache();
                });

            }
        });
        alertDialog.show();
        return false;
    }


    private void getGeoJsonFromMap(){
        Mapbox.getInstance(MainActivity.this, getString(R.string.acces_token));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.alert_map, null));

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
                MapboxMapOptions options = MapboxMapOptions.createFromAttributes(MainActivity.this, null)
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

                        MainActivity.this.mapboxMap = mapboxMap;
                        mapboxMap.setStyle(Style.SATELLITE, new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                // Map is set up and the style has loaded. Now you can add data or make other map adjustments.
                                // Adding route of JSON

                                // Add the  layer for the dashed directions route line
                                initDottedLineSourceAndLayer(style);
                                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                                UiSettings uiSettings = MainActivity.this.mapboxMap.getUiSettings();
                                uiSettings.setCompassGravity(Gravity.BOTTOM|Gravity.RIGHT);
                                MainActivity.this.mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
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
                    Toast.makeText(MainActivity.this, "GeoJson file created. Video correctly uploaded", Toast.LENGTH_SHORT).show();
                    uploadHelper.setFeatureCollection(dashedLineDirectionsFeatureCollection);
                    uploadHelper.setPointList(MainActivity.this.coordinates);
                    uploadHelper.writeAll();
                    alertDialog2.cancel();
                    mapView.onStop();

                });
                go_home_btn.setOnClickListener(v -> {
                    Toast.makeText(MainActivity.this, "Select GeoJson Format", Toast.LENGTH_SHORT).show();
                    alertDialog2.cancel();
                    getGeoJson();
                    mapView.onStop();

                });
            }
        });
        alertDialog2.show();
    }

    private Symbol addMarker(Double lat, double lon, Integer drawable, String name){
        mapboxMap.getStyle().addImage(name, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(drawable)));
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
                .origin(Point.fromLngLat(MainActivity.this.sourcePoint.getLongitude(), MainActivity.this.sourcePoint.getLatitude()))
                .destination(Point.fromLngLat(MainActivity.this.destinationPoint.getLongitude(), MainActivity.this.destinationPoint.getLatitude()))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_CYCLING)
                .accessToken(MainActivity.this.getString(R.string.acces_token))
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
                    directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    private String checkLastTask() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm == null) {
            System.out.println("Fragment Manager is Null --> Finishing app");
            return "None";
        }
        String a = "";
        String tag;
        FragmentManager.BackStackEntry backEntry;
        for (int entry = 0; entry < fm.getBackStackEntryCount(); entry++) {
            backEntry = fm.getBackStackEntryAt(entry);
            tag = backEntry.getName();
            a = a + "\n" + tag;
            Log.i(TAG, "Found fragment: " + fm.getBackStackEntryAt(entry).getId() + " --> " + tag);
        }
        try {
            return fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Nothing on stack --> Finishhing app");
            if (getSupportActionBar().getTitle().toString().contains("Profile")) {
                getSupportActionBar().show();
                setToolbar(R.string.title_home, "\uD83C\uDFE0");
                Fragment fragment = new HomeFragment();
                loadFragment(fragment, "Home");
                return "Home";
            }
            return "None";
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    private void setToolbar(int title, String emoji) {
        getSupportActionBar().setTitle(getString(title) + " " + emoji);
        getSupportActionBar().setSubtitle("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int item;
        String currentFragment = checkLastTask();
        switch (currentFragment) {
            case "Home":
                item = R.id.navigation_home;
                break;
            case "Profile":
                item = R.id.navigation_profile;
                break;
            case "None":
                this.finish();
                System.exit(0);
            default:
                item = R.id.navigation_home;
                break;
        }
        System.out.println("Back pressed handled in Main Activity. Selected item " + currentFragment);
        bottomNavigationView.setSelectedItemId(item);
        getSupportFragmentManager().popBackStack();

    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Resuming handled in Main Activity");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Paused handled in Main Activity");

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(SetLanguage.onAttach(base, "en"));
    }

    // Method used for checking LocationAdapter permission. If Permission is Granted, enable Camera Recording. If not, maintain state.
    private boolean checkLocationPermission() {
        // Check if the application has the permission to acces to your LocationAdapter
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                // Supply index input as an argument.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // PERMISSION WAS GRANTED
                    // Do the location-related TASK.
                    // Changing to camera Activity
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        setToolbar(R.string.title_camera, "\uD83D\uDCF8");
                        /*
                        // If we treat it as Fragments
                        Fragment fragment = new CameraFragment();
                        loadFragment(fragment);
                        */
                        // IF we create a new activity
                        Log.d(TAG, "Due to Caché memory, directly entered to Main");
                        startActivity(new Intent(MainActivity.this, CameraActivity.class));
                    }

                } else {
                    // PERMISSION DENIED
                    // Disable the functionality that depends on this permission.
                    // Not connecting to the camera
                    Toast.makeText(getBaseContext(), "GPS is not turned on. Grand permission for Recording!", Toast.LENGTH_LONG).show();

                }
                return;
            }

        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public interface IOnBackPressed {
        /**
         * If you return true the back press will not be taken into account, otherwise the activity will act naturally
         *
         * @return true if your processing has priority if not false
         */
        boolean onBackPressed();
    }
}

