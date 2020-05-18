package com.example.micromobility.ui.profile.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.R;
import com.example.micromobility.ui.video.VideoActivity;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.ramotion.foldingcell.FoldingCell;


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FoldingCellListAdapter extends ArrayAdapter<Item> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;



    public FoldingCellListAdapter(Context context, List<Item> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // get item for selected view
        Item item = getItem(position);
        // if cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.cell, parent, false);
            // binding view parts to view holder
            // Folded cell
            viewHolder.image = cell.findViewById(R.id.title_photo_route);
            viewHolder.time = cell.findViewById(R.id.title_time_label);
            viewHolder.date = cell.findViewById(R.id.title_date_label);
            viewHolder.fromAddress = cell.findViewById(R.id.title_from_address);
            viewHolder.toAddress = cell.findViewById(R.id.title_to_address);
            viewHolder.duration = cell.findViewById(R.id.title_duration_label);
            viewHolder.title = cell.findViewById(R.id.title_video_name);
            viewHolder.detected = cell.findViewById(R.id.button_detection);
            viewHolder.uploaded = cell.findViewById(R.id.button_uploaded);

            // Unfolded cell
            viewHolder.detected_extend = cell.findViewById(R.id.head_image_right_text);
            viewHolder.uploaded_extend = cell.findViewById(R.id.head_image_center_text);
            viewHolder.duration_extend = cell.findViewById(R.id.extend_duration_label);
            viewHolder.username = cell.findViewById(R.id.content_name_view);
            viewHolder.image_extend = cell.findViewById(R.id.head_photo);
            viewHolder.time_extend = cell.findViewById(R.id.content_delivery_time);
            viewHolder.date_extend = cell.findViewById(R.id.content_delivery_date);
            viewHolder.fromAddress_extend_1 = cell.findViewById(R.id.content_from_address_1);
            viewHolder.toAddress_extend_1 = cell.findViewById(R.id.content_to_address_1);
            viewHolder.fromAddress_extend_2 = cell.findViewById(R.id.content_from_address_2);
            viewHolder.toAddress_extend_2 = cell.findViewById(R.id.content_to_address_2);
            viewHolder.contentRequestBtn = cell.findViewById(R.id.content_request_btn);
            viewHolder.content_avatar = cell.findViewById(R.id.content_avatar);
            viewHolder.information = cell.findViewById(R.id.info_button_card);
            viewHolder.map_arrow = cell.findViewById(R.id.goToMapArrow);
            viewHolder.addMap = cell.findViewById(R.id.add_geojson);
            viewHolder.nogeojson = cell.findViewById(R.id.adding_address);
            viewHolder.clicking_map_address = cell.findViewById(R.id.clicking_map_address);



            cell.setTag(viewHolder);
        } else {
            // for existing cell set valid valid state(without animation)
            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            viewHolder = (ViewHolder) cell.getTag();
        }

        if (null == item)
            return cell;

        if (!item.getGeojson().equals("None")) {

            // bind data from selected element to view through view holder
            String[] from = item.getFromAddress();
            String[] to = item.getToAddress();
            String[] fromC = getAddress(Double.parseDouble(from[1]), Double.parseDouble(from[0]));
            String[] toC = getAddress(Double.parseDouble(to[1]), Double.parseDouble(to[0]));

            if (fromC == null) {
                fromC = new String[2];
                fromC[0] = "Not Known";
                fromC[1] = from[0] + " and " + from[1];
            }
            if (toC == null) {
                toC = new String[2];
                toC[0] = "Not Known";
                toC[1] = to[0] + " and " + to[1];
            }

            viewHolder.fromAddress.setText(fromC[1]);
            viewHolder.toAddress.setText(toC[1]);
            viewHolder.fromAddress_extend_1.setText(fromC[1]);
            viewHolder.toAddress_extend_1.setText(toC[1]);
            viewHolder.fromAddress_extend_2.setText(fromC[0]);
            viewHolder.toAddress_extend_2.setText(toC[0]);
        }
        viewHolder.image.setImageBitmap(item.getImage());
        viewHolder.time.setText(item.getTime());
        viewHolder.date.setText(item.getDate());
        viewHolder.duration.setText(item.getDuration());
        viewHolder.username.setText(item.getUsername());
        viewHolder.content_avatar.setImageBitmap(item.getContentAvatar());
        viewHolder.title.setText(item.getTitle());
        viewHolder.duration_extend.setText(item.getDuration());
        viewHolder.image_extend.setImageBitmap(item.getImage());
        viewHolder.time_extend.setText(item.getTime());
        viewHolder.date_extend.setText(item.getDate());


        if (!item.getGeojson().equals("map_information.geojson")){

            cell.findViewById(R.id.file_uploaded_content).setVisibility(View.VISIBLE);
            if (item.getGeojson().equals("None")) {
                viewHolder.clicking_map_address.setVisibility(View.GONE);
                viewHolder.nogeojson.setVisibility(View.VISIBLE);
                viewHolder.fromAddress.setText("Not location information");
                viewHolder.toAddress.setText("Extend to add location");
            }
        }


        viewHolder.detected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                item.setDetected(true);
                viewHolder.detected.setBackgroundColor(R.drawable.gradient);
                viewHolder.detected.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.detected_extend.setBackgroundColor(R.drawable.gradient);
                viewHolder.detected_extend.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.detected.setClickable(false);
                viewHolder.detected_extend.setClickable(false);
                Toast.makeText(getContext(), "Detecting video "+ item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.detected_extend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                item.setDetected(true);
                viewHolder.detected.setBackgroundColor(R.drawable.gradient);
                viewHolder.detected.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.detected_extend.setBackgroundColor(R.drawable.gradient);
                viewHolder.detected_extend.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.detected.setClickable(false);
                viewHolder.detected_extend.setClickable(false);
                Toast.makeText(getContext(), "Detecting video "+ item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.information.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                showInformation(v, viewHolder.title.getText().toString());
            }
        });
        viewHolder.uploaded.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                item.setUploaded(true);
                viewHolder.uploaded.setBackgroundColor(R.drawable.gradient);
                viewHolder.uploaded.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.uploaded_extend.setBackgroundColor(R.drawable.gradient);
                viewHolder.uploaded_extend.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.uploaded.setClickable(false);
                viewHolder.uploaded_extend.setClickable(false);
                Toast.makeText(getContext(), "Uploading video "+ item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.uploaded_extend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                item.setUploaded(true);
                viewHolder.uploaded.setBackgroundColor(R.drawable.gradient);
                viewHolder.uploaded.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.uploaded_extend.setBackgroundColor(R.drawable.gradient);
                viewHolder.uploaded_extend.setText(viewHolder.detected.getContext().getString(R.string.detection_button_YES));
                viewHolder.uploaded.setClickable(false);
                viewHolder.uploaded_extend.setClickable(false);
                Toast.makeText(getContext(), "Uploading video "+ item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.map_arrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Toast.makeText(getContext(), "Visiting map for video "+ item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        viewHolder.addMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                Toast.makeText(getContext(), "Adding location to "+ item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // set custom btn handler for list item from that item
        if (item.getRequestBtnClickListener() != null) {
            viewHolder.contentRequestBtn.setOnClickListener(item.getRequestBtnClickListener());
        }
        if (item.getDetectBtnClickListener() != null) {
            viewHolder.detected.setOnClickListener(item.getDetectBtnClickListener());
            viewHolder.detected.setOnClickListener(item.getDetectBtnClickListener());
        }
        if (item.getUploadBtnClickListener() != null) {
            viewHolder.uploaded.setOnClickListener(item.getUploadBtnClickListener());
            viewHolder.uploaded_extend.setOnClickListener(item.getUploadBtnClickListener());
        }
        if (item.getInformBtnClickListener() != null) {
            viewHolder.information.setOnClickListener(item.getInformBtnClickListener());
        }
        if (item.getMapBtnClickListener() != null) {
            viewHolder.map_arrow.setOnClickListener(item.getMapBtnClickListener());
        }
        if (item.getAddMapBtnClickListener() != null) {
            viewHolder.addMap.setOnClickListener(item.getAddMapBtnClickListener());
        }

        return cell;
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    public void showInformation(View v, String title) {
        Toast.makeText(getContext(), "Inforamtion for the video "+ title, Toast.LENGTH_SHORT).show();

    }

    public String[] getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
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

    // View lookup cache
    private static class ViewHolder {
        ImageView image;
        ImageView image_extend;
        TextView contentRequestBtn;
        TextView duration;
        TextView duration_extend;
        TextView fromAddress;
        TextView toAddress;
        TextView detected;
        TextView uploaded;
        TextView fromAddress_extend_1;
        TextView toAddress_extend_1;
        TextView fromAddress_extend_2;
        TextView toAddress_extend_2;
        TextView detected_extend;
        TextView uploaded_extend;
        TextView information;
        TextView title;
        TextView date;
        TextView time;
        TextView date_extend;
        TextView time_extend;
        ImageView content_avatar;
        TextView username;
        TextView map_arrow;
        TextView addMap;
        LinearLayout nogeojson;
        LinearLayout clicking_map_address;

    }





}