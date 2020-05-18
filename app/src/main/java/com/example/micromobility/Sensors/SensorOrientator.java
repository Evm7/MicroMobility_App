package com.example.micromobility.Sensors;

import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;



public class SensorOrientator implements SensorEventListener {

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    public final int ORIENTATION_PORTRAIT = ExifInterface.ORIENTATION_ROTATE_90; // 6
    public final int ORIENTATION_LANDSCAPE_REVERSE = ExifInterface.ORIENTATION_ROTATE_180; // 3
    public final int ORIENTATION_LANDSCAPE = ExifInterface.ORIENTATION_NORMAL; // 1
    public final int ORIENTATION_PORTRAIT_REVERSE = ExifInterface.ORIENTATION_ROTATE_270; // 8

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    public float azimuth=0, pitch =0, roll=0;
    private int orientation = ORIENTATION_PORTRAIT;

    private TextView sensortext;
    ImageView viewleftArrow, viewrightArrow, lineToRotate;
    Button btn_callibrate;
    private Boolean changeImages = Boolean.FALSE;


    public void initialize(Context context, Boolean changing, TextView textToEdit, ImageView leftArrow, ImageView rightArrow, Button callibrate) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        changeImages = changing;
        sensortext=textToEdit;
        viewleftArrow = leftArrow;
        viewrightArrow = rightArrow;
        btn_callibrate = callibrate;
        onResume();
    }

    public void setToRotate(Context context, Boolean changing, ImageView imageview){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lineToRotate = imageview;
        changeImages = changing;
        onResume();


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    protected void onResume() {
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    protected void onPause() {
        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        if (magnetometerReading != null && accelerometerReading != null){
                updateOrientationAngles();
                if (changeImages == Boolean.TRUE){
                    sensortext.setText(getOrientationString());
                }
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        azimuth = orientationAngles[0]; // orientation contains: azimuth, pitch and roll
        pitch = orientationAngles[1];
        roll = orientationAngles[2];

        if (changeImages == Boolean.FALSE && lineToRotate.getDrawable() != null){
            Float angle;
            // In the little angles, roll has huge variation. From  0 to -1 there are 15 degrees
            if (roll >= -1 && roll <= 1){
                angle =roll * 15 *-1;
            }
            // In the angles where roll is over 1 --> 1.5, the angle goes from 15 to 90 degrees
            else{
                angle=  Math.signum(roll)* (15 + Math.abs(roll)-1 * 75);
            }
            rotateImage(angle, lineToRotate);
        }
        orientation = calculateOrientation();
    }

    public boolean getOrientation() {
        if ((orientation == ORIENTATION_PORTRAIT_REVERSE) || (orientation== ORIENTATION_PORTRAIT)){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public String getOrientationString(){
        String resume = "LANDSCAPE";
        if (orientation == ORIENTATION_PORTRAIT || orientation == ORIENTATION_PORTRAIT_REVERSE){
            resume = "PORTRAIT";
        }
        return resume + " | "+ "Azimuth : " + Integer.toString((int) (azimuth*90))+ " | " +"Roll : " + Integer.toString((int) (roll*90))+ " | " + "Pitch : " + Integer.toString((int) (pitch*90));
    }

    private int calculateOrientation() {
        // finding local orientation dip
        if (roll > -1 && roll < 1) {
            if (changeImages == Boolean.TRUE) {
                viewleftArrow.setVisibility(View.INVISIBLE);
                viewrightArrow.setVisibility(View.INVISIBLE);
                btn_callibrate.setVisibility(View.VISIBLE);
                btn_callibrate.setClickable(Boolean.TRUE);
            }
            if (pitch > 0)
                return ORIENTATION_PORTRAIT_REVERSE;
            else
                return ORIENTATION_PORTRAIT;
        } else {
            // divides between all orientations
            if (changeImages == Boolean.TRUE) {
                btn_callibrate.setVisibility(View.INVISIBLE);
                btn_callibrate.setClickable(Boolean.FALSE);
            }
            if (roll > 0) {
                if (changeImages == Boolean.TRUE) {
                    viewleftArrow.setVisibility(View.INVISIBLE);
                    viewrightArrow.setVisibility(View.VISIBLE);
                }
                return ORIENTATION_LANDSCAPE_REVERSE;
            } else {
                if (changeImages == Boolean.TRUE) {
                    viewleftArrow.setVisibility(View.VISIBLE);
                    viewrightArrow.setVisibility(View.INVISIBLE);
                }
                return ORIENTATION_LANDSCAPE;
            }
        }
    }


    // For the callibration of the video
    public void rotateImage(Float angle, ImageView imageView){
        Matrix matrix = new Matrix();
        imageView.setScaleType(ImageView.ScaleType.MATRIX); //required
        matrix.postRotate((float) angle, imageView.getDrawable().getBounds().width()/2, imageView.getDrawable().getBounds().height()/2);
        imageView.setImageMatrix(matrix);
    }

}