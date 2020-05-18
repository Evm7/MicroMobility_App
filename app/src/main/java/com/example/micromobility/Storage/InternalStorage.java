package com.example.micromobility.Storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;


public class InternalStorage {

    Context mcontainer;
/*
        FILENAME is username
        mapUser: {
            username:
            password:
            language:
            email_user:
            gender:
            birth_day:
            postal_code:
            profile_picture:
            email_tutor: "None"
            user_id_telegram: "None"
            }
       Also it default system settings:
       System: {
            "tone": "ON"
            "vibration": "Predetermined"
            "light": "Cyan"
            "priority": " DEFAULT"
            "email_notification" : "OFF"
           }
           Final Map is: { "user" : {}, "system" : {} }        */


    public InternalStorage(Context context) {
        this.mcontainer = context;
    }

    public String getUsername(){
        ContextWrapper cw = new ContextWrapper(mcontainer);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("temporary", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"users.txt");
        int c;
        String temp = "";
        try {
            FileInputStream fis = new FileInputStream(mypath);
            while ((c = fis.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return temp;
    }

    public void setUsername(String username){
        ContextWrapper cw = new ContextWrapper(mcontainer);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("temporary", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"users.txt");
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            fos.write(username.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeUsername(){
        ContextWrapper cw = new ContextWrapper(mcontainer);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("temporary", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"users.txt");
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            fos.write("".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void removeAccount(){
        String username = getUsername();
        ContextWrapper cw = new ContextWrapper(mcontainer);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("temporary", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"users.txt");
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            fos.write("".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        directory = cw.getDir(username, Context.MODE_PRIVATE);
        // Create imageDir
        mypath=new File(directory,"info.txt");
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            fos.write("".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private HashMap<String, String>  defaultSettings(){
        HashMap<String, String> defaultSettings;
        defaultSettings = new HashMap<>();
        defaultSettings.put("tone","ON");
        defaultSettings.put("vibration","Predetermined");
        defaultSettings.put("light","Cyan");
        defaultSettings.put("priority","DEFAULT");
        defaultSettings.put("email_notification","OFF");
        return (defaultSettings);
    }

    //CREATES NEW USER
    public void createUser(HashMap<String, String> mapUser) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("user", getStringfromMap(mapUser));
        map.put("system", getStringfromMap(defaultSettings()));

        String username=mapUser.get("username");
        ContextWrapper cw = new ContextWrapper(mcontainer);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(username, Context.MODE_PRIVATE);
        saveLocal(getStringfromMap(map), username);

        System.out.println(readLocal(username)); //PROVE

    }

    //SET VALUE
    public void setValue(String username, String key, String value) {
        //Read whole file and convert to Map
        String read = readLocal(username);
        System.out.println(read); //PROVE

        HashMap<String, String> myMap = getMapfromString(read);

        //Get exact map we wanto change value to:
        String name =defaultSettings().containsKey(key) ? "system" :"user";
        HashMap<String, String> realMap = getMapfromString(myMap.get(name));

        //Insert new value
        realMap.put(key, value);

        // Convert whole Map to String
        myMap.put(name, getStringfromMap(realMap));
        read = getStringfromMap(myMap);
        System.out.println(read); //PROVE

        saveLocal(read, username);
    }

    //SET VALUE
    public String getValue(String username, String key) {
        //Read whole file and convert to Map
        String read = readLocal(username);
        System.out.println(read); //PROVE

        HashMap<String, String> myMap = getMapfromString(read);

        //Get exact map we wanto change value to:
        String name =defaultSettings().containsKey(key) ? "system" :"user";
        HashMap<String, String> realMap = getMapfromString(myMap.get(name));

        //Insert new value
        return realMap.get(key);
    }

    private String getStringfromMap(HashMap<String, String> mymap) {
        Gson gson = new Gson();
        HashMap<String, String> map = new HashMap<String, String>();
        return gson.toJson(mymap, map.getClass());

    }

    private HashMap getMapfromString(String str) {
        Gson gson = new Gson();
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> myMap = gson.fromJson(str, map.getClass());
        return myMap;
    }

    private void saveLocal(String data, String username) {
        // MODE_APPEND -->used in order to append data to not existing information
        // MODE_PRIVATE --> used in order to overwrite data (removes previous content)
        ContextWrapper cw = new ContextWrapper(mcontainer);
        File directory = cw.getDir(username, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"info.txt");
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            fos.write(data.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String readLocal(String username) {
        ContextWrapper cw = new ContextWrapper(mcontainer);
        File directory = cw.getDir(username, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"info.txt");
        int c;
        String temp = "";
        try {
            FileInputStream fis = new FileInputStream(mypath);
            while ((c = fis.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return temp;
    }

    //HANDLERS PROFILE PHOTO
    public void savePhoto(Bitmap profile_picture, String username){
        ContextWrapper cw = new ContextWrapper(mcontainer);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,username+ ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            profile_picture.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getPhoto(String username){
        try {
            ContextWrapper cw = new ContextWrapper(mcontainer);
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f=new File(directory, username+".jpg");
            return (BitmapFactory.decodeStream(new FileInputStream(f)));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String,String> getInformation(String username){
        return getMapfromString(readLocal(username));
    }

}