package com.example.micromobility.ui.video.dashboard;


import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import toan.android.floatingactionmenu.FloatingActionButton;
import toan.android.floatingactionmenu.FloatingActionsMenu;


public class DetectorHelper implements  PlayerHelper.EndOption{

    private View mcontainer;

    PlayerHelper mPlayerHelper;

    private String relative_path;
    private String title;
    private String type;
    private String category;
    private File mediafile;
    private Context context;
    private View buttons;
    FloatingActionsMenu menu_road;
    private TextView classifier, resolution;
    int minteger = 0;
    TextView displayInteger;
    private FileHelper fileHelper;
    private JSONArray jsonArray;
    RelativeLayout save_results;



    public DetectorHelper(View view, String relative_path, String title, String type) {
        this.context = view.getContext();
        this.mPlayerHelper = new PlayerHelper(relative_path, title, type, this);
        mPlayerHelper.initializePlayer(view);
        this.relative_path = relative_path;
        this.title = title;
        this.type = type;
        this.fileHelper = new FileHelper(context, relative_path);
        this.jsonArray = new JSONArray();
    }

    public void controlExoPlayer(){
        this.mPlayerHelper.setControls();
        this.mPlayerHelper.setCallback(this);
    }


    public void addViews(ViewGroup container) {
        if (type.equals("Type of Road")) {
            set_type_of_road(container);
            classifier = container.findViewById(R.id.classifier);
            resolution = container.findViewById(R.id.resolution);
            classifier.setText(type);
            resolution.setText("");
        }
        else if (type.equals("Pedestrians"))  {
            set_type_of_pedestrians(container);
        }
        else if (type.equals("Bicycles"))  {
            set_type_of_bike(container);
        }
        else if (type.equals("Small vehicles"))  {
            set_type_of_small_vehicles(container);
        }
        else if (type.equals("Big vehicles"))  {
            set_type_of_big_vehicles(container);
        }

        else if (type.equals("Other objects"))  {
            set_type_of_others(container);
        }
        else if (type.equals("New categories"))  {
            set_type_of_news(container);
        }
        else if (type.equals("Traffic Signs"))  {
            set_type_of_signs(container);
        }
    }


    private void set_type_of_road(ViewGroup view) {
        menu_road = view.findViewById(R.id.menu_fab);
        FloatingActionButton sidewalk_btn = view.findViewById(R.id.sidewalk_btn);
        sidewalk_btn.setOnClickListener(v -> {
            menu_road.collapse();
            menu_road.setIcon(context.getDrawable(R.mipmap.sidewalk));
            markFrame("SideWalk");
        });
        FloatingActionButton bidirect_btn = view.findViewById(R.id.bidirect_btn);
        bidirect_btn.setOnClickListener(v -> {
            menu_road.collapse();
            menu_road.setIcon(context.getDrawable(R.mipmap.bidirectional));
            markFrame("Bidirectional");
        });
        FloatingActionButton unidirect_btn = view.findViewById(R.id.unidirect_btn);
        unidirect_btn.setOnClickListener(v -> {
            menu_road.collapse();
            menu_road.setIcon(context.getDrawable(R.drawable.unidirectional));
            markFrame("Unidirectional");
        });
        FloatingActionButton crosswalk_btn = view.findViewById(R.id.crosswalk_btn);
        crosswalk_btn.setOnClickListener(v -> {
            menu_road.collapse();
            menu_road.setIcon(context.getDrawable(R.mipmap.crosswalk));
            markFrame("CrossWalk");
        });
        FloatingActionButton road_btn = view.findViewById(R.id.road_btn);
        road_btn.setOnClickListener(v -> {
            menu_road.collapse();
            menu_road.setIcon(context.getDrawable(R.mipmap.road));
            markFrame("Road");
        });
        FloatingActionButton unknown_btn = view.findViewById(R.id.unknown_btn);
        unknown_btn.setOnClickListener(v -> {
            menu_road.collapse();
            menu_road.setIcon(context.getDrawable(R.mipmap.unknown));
            markFrame("Unknown");
        });
        save_results = view.findViewById(R.id.save_results);

    }

    private void markFrame(String text) {
        resolution.setText(text);
        JSONObject information = new JSONObject();
        try {
            information.put("Time", getFrame());
            information.put("Value", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.jsonArray.put(information);
    }

    private void markNumber(String text) {
        Toast.makeText(context, "There is " + minteger +" of "+ text, Toast.LENGTH_SHORT).show();
        fileHelper.writeManualFile(this.category, this.jsonArray);
        restart();
    }

    private void set_type_of_pedestrians(ViewGroup view) {
        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        category = "Pedestrians";
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            markNumber(category);
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }

    public void increaseInteger() {
        minteger = minteger + 1;
        display(minteger);

    }public void decreaseInteger() {
        if (minteger == 0){
            Toast.makeText(context, "Value can not be negative", Toast.LENGTH_SHORT).show();
        }else{
            minteger = minteger - 1;
            display(minteger);
        }
    }

    private void display(int number) {
        displayInteger.setText("" + number);
        JSONObject information = new JSONObject();
        try {
            information.put("Time", getFrame());
            information.put("Value", number);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonArray.put(information);
    }

    private void set_type_of_bike(ViewGroup view) {
        menu_road = view.findViewById(R.id.menu_fab);
        category = "Bikes";
        FloatingActionButton bikes_btn = view.findViewById(R.id.bikes_btn);
        bikes_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Bikes";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.bike));
        });
        FloatingActionButton skateboard_btn = view.findViewById(R.id.skateboard_btn);
        skateboard_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Skateboard";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.skateboard));
        });

        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            markNumber(category);
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }

    private void set_type_of_small_vehicles(ViewGroup view) {
        menu_road = view.findViewById(R.id.menu_fab);
        category = "Cars";
        FloatingActionButton cars_btn = view.findViewById(R.id.car_btn);
        cars_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Cars";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.vehicle));

        });
        FloatingActionButton motorbike_btn = view.findViewById(R.id.motorbike_btn);
        motorbike_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Motorbikes";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.motorbike));
        });

        FloatingActionButton bus_btn = view.findViewById(R.id.bus_btn);
        bus_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Bus";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.bus));
        });

        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            markNumber(category);
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }

    private void set_type_of_big_vehicles(ViewGroup view) {
        menu_road = view.findViewById(R.id.menu_fab);
        category = "Truck";
        FloatingActionButton truck_btn = view.findViewById(R.id.truck_btn);
        truck_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Truck";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.truck));
        });
        FloatingActionButton boat_btn = view.findViewById(R.id.boat_btn);
        boat_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Boat";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.boat));
        });
        FloatingActionButton plane_btn = view.findViewById(R.id.plane_btn);
        plane_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Plane";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.plane));
        });
        FloatingActionButton train_btn = view.findViewById(R.id.train_btn);
        train_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Train";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.train));
        });

        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            markNumber(category);
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }

    private void set_type_of_others(ViewGroup view) {
        menu_road = view.findViewById(R.id.menu_fab);
        category = "Bench";
        Button parking_meter_btn = view.findViewById(R.id.parking_meter_btn);
        parking_meter_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Parking Meter";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.parking_meter));
        });
        FloatingActionButton fire_hydrant_btn = view.findViewById(R.id.fire_hydrant_btn);
        fire_hydrant_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Fire Hydrant";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.fire_hydrant));
        });

        FloatingActionButton bench_btn = view.findViewById(R.id.bench_btn);
        bench_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Bench";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.others));
        });

        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            markNumber(category);
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }
    private void set_type_of_signs(ViewGroup view) {
        menu_road = view.findViewById(R.id.menu_fab);
        category = "Bikes";
        category = "Traffic Light";
        FloatingActionButton traffic_light_btn = view.findViewById(R.id.traffic_light_btn);
        traffic_light_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Traffic Light";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.traffic_light));
        });
        FloatingActionButton stop_sign_btn = view.findViewById(R.id.stop_sign_btn);
        stop_sign_btn.setOnClickListener(v -> {
            menu_road.collapse();
            category = "Stop Sign";
            Toast.makeText(context, "Counting "+ category, Toast.LENGTH_SHORT).show();
            menu_road.setIcon(context.getDrawable(R.mipmap.stop_sign));
        });

        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            markNumber(category);
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }

    private void set_type_of_news(ViewGroup view) {
        EditText new_category = view.findViewById(R.id.new_category_text);
        displayInteger = (TextView) view.findViewById(R.id.integer_number);
        RelativeLayout save_btn = view.findViewById(R.id.save_results);
        save_btn.setOnClickListener(v -> {
            category = new_category.getText().toString();
            if(category.equals("")) {
                Toast.makeText(context, "New category not introduced", Toast.LENGTH_SHORT).show();
            }else {
                markNumber(category);
            }
        });
        Button increase_btn = view.findViewById(R.id.increase);
        increase_btn.setOnClickListener(v -> increaseInteger());
        Button decrease_btn = view.findViewById(R.id.decrease);
        decrease_btn.setOnClickListener(v -> decreaseInteger());
    }

    public void finish(){
        Toast.makeText(context, "Select new category", Toast.LENGTH_SHORT).show();
        mPlayerHelper.finish();
    }

    private Long getFrame(){
        return mPlayerHelper.getTimePosition();
    }

    private void restart(){
        minteger = 0 ;
        display(minteger);
        this.jsonArray = new JSONArray();
        mPlayerHelper.restart();
    }

    private void restartRoad(){
        resolution.setText("");
        this.jsonArray = new JSONArray();
        mPlayerHelper.restart();
    }


    @Override
    public void onEndOption(String category) {
        System.out.println("Solution for "+category+" is " + this.jsonArray);
        if(category.equals("Type of Road")){
                menu_road.setVisible(false);
                System.out.println("ON END OPTION");
                save_results.setVisibility(View.VISIBLE);

            save_results.setOnClickListener(v -> {
                fileHelper.writeManualFile(category, this.jsonArray);
                save_results.setVisibility(View.GONE);
                if(category.equals("Type of Road")){
                    menu_road.setVisible(true);
                }
                restartRoad();
            });
        }
    }
}

