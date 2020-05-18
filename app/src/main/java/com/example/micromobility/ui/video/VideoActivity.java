package com.example.micromobility.ui.video;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.Language.SetLanguage;
import com.example.micromobility.MainActivity;
import com.example.micromobility.R;
import com.example.micromobility.Storage.InternalStorage;

import com.example.micromobility.ui.video.dashboard.DashboardFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


public class VideoActivity extends AppCompatActivity implements MyMap.OnFragmentInteractionListener {
    private String username;
    private static final String TAG = "VideoActivity";
    private Bitmap avatar;

    private String path, relativePath;
    // Properties of the video
    String date ,time, image_path, directory_path, title, duration, geojson, fps;
    String [] toAddress, toRealAddress, fromAddress, fromRealAddress = new String[2];
    Bitmap firstFrame;
    boolean uploaded, detected;

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 5;

    private int[] tabIcons = {
            R.drawable.ic_map_black_24dp,
            R.drawable.ic_myvideos,
            R.drawable.ic_detect,
            R.drawable.ic_dashboard_black_24dp,
            R.drawable.ic_objects
    };

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    TabLayout tabLayout;
    private ViewPager2 viewPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private ScreenSlidePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize varibles of User:
        InternalStorage in = new InternalStorage(VideoActivity.this);
        username = in.getUsername();
        avatar = in.getPhoto(username);

        // Handle argument title
        Bundle b = getIntent().getExtras();
        if(b != null)
            path = b.getString("Item");

        String[] names = path.split("/");

        traverseVideo(names[names.length-1]);
        setContentView(R.layout.video_activity);

        // Set the toolbar Options
        Toolbar toolbar = findViewById(R.id.toolbar_video);

        setSupportActionBar(toolbar);
        toolbar.setTitle(title);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        startActivity(new Intent(VideoActivity.this, MainActivity.class));
                        return false;
                    case R.id.action_undo:
                        VideoActivity.super.onBackPressed();

                    case R.id.action_delete:
                        showDeletePanel();
                }
                return false;
            }
        });

        viewPager = findViewById(R.id.view_pager);

        tabLayout = findViewById(R.id.tabs);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        pagerAdapter.addFragment(MyMap.newInstance(fromAddress, relativePath, geojson, fps), "Map", tabIcons[0]);
        pagerAdapter.addFragment(VideoReal.newInstance(relativePath, title), "Video Real", tabIcons[1]);
        pagerAdapter.addFragment(VideoDetected.newInstance(relativePath,"detected.mp4", detected), "Video Detected", tabIcons[2]);
        pagerAdapter.addFragment(DashboardFragment.newInstance(relativePath, detected, title), "Manual Detection", tabIcons[3]);
        pagerAdapter.addFragment(VideoDescriptor.newInstance(relativePath, "Summary.txt"), "Objects", tabIcons[4]);

        viewPager.setAdapter(pagerAdapter);


        viewPager.setPageTransformer(new CubeOutDepthTransformation());
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position) {
                            case 0:
                                tab.setText("MAP");
                                break;
                            case 1:
                                tab.setText("REAL VIDEO");
                                break;
                            case 2:
                                tab.setText("DETECTED VIDEO");
                                break;
                            case 3:
                                tab.setText("MANUAL DETECTION");
                                break;
                            case 4:
                                tab.setText("OBJECTS");
                                break;
                            default:
                                tab.setText("MAP");
                                break;
                        }
                    }
                }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                highLightCurrentTab(position);
            }
        });

    }

    private void showDeletePanel(){
        // Create an AlertDialog.Builder and set the message.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.delete_video, null));

        // Create the AlertDialog
        final AlertDialog alertDialog = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog.setOnShowListener(dialog -> {

                    final Button buttonclose = alertDialog.findViewById(R.id.NotDelete);
                    Button buttonDelete = alertDialog.findViewById(R.id.YesDelete);

                    buttonclose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });
                    buttonDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteVideo();
                            startActivity(new Intent(VideoActivity.this, MainActivity.class));

                        }

                    });
                });

        // Show the AlertDialog
        alertDialog.show();
    }

    /**
     * Used to Delete the video if delete button pressed
     */
    private void deleteVideo(){
        File fileOrDirectory = this.getExternalFilesDir(relativePath);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_menu, menu);
        return true;
    }


    private void loadFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }
     private void setToolbar(int title, String emoji){
         getSupportActionBar().setTitle(getString(title)+ " "+ emoji);
         getSupportActionBar().setSubtitle("");
     }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(SetLanguage.onAttach(base, "en"));
    }

    // Iterate through all the files in each video
    private void traverseVideo (String name) {
        relativePath = "MicroMobility"+"/"+username+"/"+name;

        File summary = new File(this.getExternalFilesDir(relativePath),"Summary.json");
        if (!summary.exists()){
            Toast.makeText(this, "Error while retrieving information on file "+ name, Toast.LENGTH_SHORT).show();
        }else{
            JSONObject jsonObject = parseFile(summary);
            try{
                title = jsonObject.get("title").toString();
                duration = jsonObject.get("duration").toString();
                geojson = jsonObject.get("geojson").toString();
                if(!geojson.equals("None")){
                    fromAddress = getStringLocation(jsonObject.get("fromAddress").toString());
                    toAddress = getStringLocation(jsonObject.get("toAddress").toString());
                    if (fromAddress == null){
                        toRealAddress=null;
                        fromRealAddress=null;
                    }else {
                        toRealAddress = getAddress(Double.parseDouble(toAddress[1]), Double.parseDouble(toAddress[0]));
                        fromRealAddress = getAddress(Double.parseDouble(fromAddress[1]), Double.parseDouble(fromAddress[0]));
                    }
                }

                if (jsonObject.get("detected").toString().equals("false")){
                    detected = false;
                }
                else{
                    detected = true;
                }
                if ( jsonObject.get("uploaded").toString().equals("false")){
                    uploaded = false;
                }
                else{
                    uploaded = true;
                }
                fps = jsonObject.get("fps").toString();
                date = jsonObject.get("date").toString();
                time = jsonObject.get("time").toString();
                image_path = jsonObject.get("image").toString();
                directory_path = jsonObject.get("directory_path").toString();
                firstFrame= BitmapFactory.decodeFile(directory_path+"/"+image_path);
                String username = jsonObject.get("username").toString();
                if (!username.equals(this.username)){
                    System.out.println("Error in usernames. Getting file from "+ username + " as user "+ this.username);
                }
            }catch (JSONException ex){
                ex.printStackTrace();
            }
        }
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
        if (jsonObject == null){
            return null;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonObject);
            JSONObject loc = jsonArray.getJSONObject(0);
            String[] loc_s = new String[2];
            loc_s[0] = loc.getString("Longitude");
            loc_s[1] =  loc.getString("Latitude");
            return loc_s;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            if (obj == null){
                return null;
            }
            String add[] = new String[2];
            add[0] = obj.getAddressLine(0);
            if (obj.getLocality()== null){
                add[1] = obj.getSubAdminArea();
            }else{
                add[1] = obj.getLocality();
            }
            return add;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void highLightCurrentTab(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            assert tab != null;
            tab.setCustomView(null);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        assert tab != null;
        tab.setCustomView(null);
        tab.setCustomView(pagerAdapter.getSelectedTabView(position));

    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final List<Integer> mFragmentIconList = new ArrayList<>();
        private Context context;


        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
            this.context = fa;
        }

        @Override
        public Fragment createFragment(int position) {
            return mFragmentList.get(position);
        }

        public void addFragment(Fragment fragment, String title, int tabIcon) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
            mFragmentIconList.add(tabIcon);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }


        public View getTabView(int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
            TextView tabTextView = view.findViewById(R.id.tabTextView);
            tabTextView.setText(mFragmentTitleList.get(position));
            ImageView tabImageView = view.findViewById(R.id.tabImageView);
            tabImageView.setImageResource(mFragmentIconList.get(position));
            return view;
        }
        public View getSelectedTabView(int position) {
            View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
            TextView tabTextView = view.findViewById(R.id.tabTextView);
            tabTextView.setText(mFragmentTitleList.get(position));
            tabTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
            ImageView tabImageView = view.findViewById(R.id.tabImageView);
            tabImageView.setImageResource(mFragmentIconList.get(position));
            tabImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryLight), PorterDuff.Mode.SRC_ATOP);
            return view;
        }

    }

    // Animation for the transition method
    public class CubeOutDepthTransformation implements ViewPager2.PageTransformer {
        @Override
        public void transformPage(View page, float position) {

            if (position < -1) {    // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(0);

            } else if (position <= 0) {    // [-1,0]
                page.setAlpha(1);
                page.setPivotX(page.getWidth());
                page.setRotationY(-90 * Math.abs(position));

            } else if (position <= 1) {    // (0,1]
                page.setAlpha(1);
                page.setPivotX(0);
                page.setRotationY(90 * Math.abs(position));

            } else {    // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(0);

            }


            if (Math.abs(position) <= 0.5) {
                page.setScaleY(Math.max(0.4f, 1 - Math.abs(position)));
            } else if (Math.abs(position) <= 1) {
                page.setScaleY(Math.max(0.4f, 1 - Math.abs(position)));
            }


        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public interface IOnBackPressed {
        /**
         * If you return true the back press will not be taken into account, otherwipòse the activity will act naturally
         * @return true if your processing has priority if not false
         */
        boolean onBackPressed();
    }
}

