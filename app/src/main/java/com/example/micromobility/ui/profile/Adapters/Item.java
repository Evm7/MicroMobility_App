package com.example.micromobility.ui.profile.Adapters;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import java.util.ArrayList;

/**
 * Simple POJO model for example
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Item {
    private String title;
    private Bitmap image;
    private String duration;
    private String fromAddress;
    private String toAddress;
    private boolean detected;
    private boolean uploaded;
    private String date;
    private String time;
    private String directory_path;
    private String username;
    private String geojson;
    private Bitmap contentAvatar;


    private View.OnClickListener requestBtnClickListener;
    private View.OnClickListener uploadBtnClickListener;
    private View.OnClickListener detecttBtnClickListener;
    private View.OnClickListener informBtnClickListener;
    private View.OnClickListener mapBtnClickListener;
    private View.OnClickListener addMapBtnClickListener;





    public Item() {
    }

    public Item(String title, Bitmap image, String duration, String fromAddress, String toAddress, boolean detected, boolean uploaded, String date, String time, String directory_path, String username, Bitmap contentAvatar, String geojson) {
        this.title = title;
        this.image = image;
        this.duration = duration;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.detected = detected;
        this.uploaded = uploaded;
        this.date = date;
        this.time = time;
        this.directory_path = directory_path;
        this.username = username;
        this.contentAvatar = contentAvatar;
        this.geojson= geojson;
    }

    public String getGeojson() {
        return geojson;
    }

    public void setGeojson(String geojson) {
        this.geojson = geojson;
    }

    public String getDirectory_path() {
        return directory_path;
    }

    public void setDirectory_path(String directory_path) {
        this.directory_path = directory_path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Bitmap getContentAvatar() {
        return contentAvatar;
    }

    public void setContentAvatar(Bitmap avatar) {
        this.contentAvatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String pledgePrice) {
        this.duration = pledgePrice;
    }

    public String[] getFromAddress() {
        String[] loc;
        loc = fromAddress.split(" ");
        return loc;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String[] getToAddress() {
        String[] loc;
        loc = toAddress.split(" ");
        return loc;    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public boolean getDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public View.OnClickListener getRequestBtnClickListener() {
        return requestBtnClickListener;
    }

    public void setRequestBtnClickListener(View.OnClickListener requestBtnClickListener) {
        this.requestBtnClickListener = requestBtnClickListener;
    }

    public View.OnClickListener getUploadBtnClickListener() {
        return uploadBtnClickListener;
    }

    public void setUploadBtnClickListener(View.OnClickListener uploadBtnClickListener) {
        this.uploadBtnClickListener = uploadBtnClickListener;
    }

    public View.OnClickListener getDetectBtnClickListener() {
        return detecttBtnClickListener;
    }

    public void setDetecttBtnClickListener(View.OnClickListener detecttBtnClickListener) {
        this.detecttBtnClickListener = detecttBtnClickListener;
    }

    public View.OnClickListener getMapBtnClickListener() {
        return mapBtnClickListener;
    }

    public void setMapBtnClickListener(View.OnClickListener mapBtnClickListener) {
        this.mapBtnClickListener = mapBtnClickListener;
    }

    public View.OnClickListener getAddMapBtnClickListener() {
        return addMapBtnClickListener;
    }

    public void setAddMapBtnClickListener(View.OnClickListener addMapBtnClickListener) {
        this.addMapBtnClickListener = addMapBtnClickListener;
    }

    public View.OnClickListener getInformBtnClickListener() {
        return informBtnClickListener;
    }

    public void setInformBtnClickListener(View.OnClickListener informBtnClickListener) {
        this.informBtnClickListener = informBtnClickListener;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (duration != item.duration) return false;
        if (image != null ? !image.equals(item.image) : item.image != null) return false;
        if (detected != item.detected)
            return false;
        if (uploaded != item.uploaded)
            return false;
        if (fromAddress != null ? !fromAddress.equals(item.fromAddress) : item.fromAddress != null)
            return false;
        if (toAddress != null ? !toAddress.equals(item.toAddress) : item.toAddress != null)
            return false;
        if (date != null ? !date.equals(item.date) : item.date != null) return false;
        return !(time != null ? !time.equals(item.time) : item.time != null);

    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (fromAddress != null ? fromAddress.hashCode() : 0);
        result = 31 * result + (toAddress != null ? toAddress.hashCode() : 0);
        result = 31 * result + (detected == true ? 1 : 0);
        result = 31 * result + (uploaded == true ? 1 : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }



}