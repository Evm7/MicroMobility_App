package com.example.micromobility.configuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class Help extends Fragment {
    AlertDialog alertDialog;
    int number = 1;
    View mcontainer;
    Boolean[] bools = {Boolean.FALSE,Boolean.FALSE, Boolean.FALSE};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mcontainer = inflater.inflate(R.layout.help, container, false);

        EditText input_problem = mcontainer.findViewById(R.id.input_problem);
        TextView frequent_questions_btn = mcontainer.findViewById(R.id.frequent_questions_btn);

        final LinearLayout photo1_btn = mcontainer.findViewById(R.id.photo1_btn);
        final LinearLayout photo2_btn = mcontainer.findViewById(R.id.photo2_btn);
        final LinearLayout photo3_btn = mcontainer.findViewById(R.id.photo3_btn);

        Button button_to_home = mcontainer.findViewById(R.id.button_to_home);
        final Button button_to_send = mcontainer.findViewById(R.id.button_to_send);
        button_to_send.setEnabled(false);


        photo1_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = 1;
                take_photo();

            }
        });
        photo2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = 2;
                take_photo();
            }
        });
        photo3_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = 3;
                take_photo();
            }
        });

        button_to_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                Fragment fragment =  new ConfigurationSystem();
                loadFragment(fragment, "Help-ConfigurationSystem");


            }
        });
        button_to_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_help();

            }
        });


        frequent_questions_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_freq_questions();

            }
        });

        input_problem.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() <= 3) {
                    button_to_send.setEnabled(false);
                    button_to_send.setBackgroundColor(Color.parseColor("#fcfdfb"));
                } else {
                    button_to_send.setEnabled(true);
                    button_to_send.setBackgroundColor(Color.parseColor("#15e719"));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
        return mcontainer;
    }
    private void loadFragment(Fragment fragment, String tag){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }
    private void take_photo() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mcontainer.getContext());

        builder.setView(getLayoutInflater().inflate(R.layout.picture_dialog, null));

        alertDialog = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button take_pic = alertDialog.findViewById(R.id.take_pic);
                Button choose_from_gallery = alertDialog.findViewById(R.id.choose_from_gallery);
                final Button close_dialog = alertDialog.findViewById(R.id.close_dialog);


                close_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
                take_pic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);
                    }

                });

                choose_from_gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 1);
                    }

                });
            }
        });

        alertDialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        Bitmap imageView = selectedImage;
                        ImageView add_photo;
                        switch (number) {
                            case 3:
                                add_photo = mcontainer.findViewById(R.id.photo3_img);
                                bools[2]=Boolean.TRUE;
                                break;
                            case 2:
                                add_photo = mcontainer.findViewById(R.id.photo2_img);
                                bools[1]=Boolean.TRUE;
                                break;
                            default:
                                add_photo = mcontainer.findViewById(R.id.photo1_img);
                                bools[0]=Boolean.TRUE;
                                break;
                        }
                        add_photo.setImageBitmap(imageView);
                        alertDialog.cancel();
                    }

                    break;
                case 1:
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Uri selectedImageUri = data.getData();
                            InputStream imageStream = null;
                            try {
                                imageStream = mcontainer.getContext().getContentResolver().openInputStream(selectedImageUri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            Bitmap imageView = BitmapFactory.decodeStream(imageStream);
                            ImageView add_photo;
                            switch (number) {
                                case 3:
                                    add_photo = mcontainer.findViewById(R.id.photo3_img);
                                    break;
                                case 2:
                                    add_photo = mcontainer.findViewById(R.id.photo2_img);
                                    break;
                                default:
                                    add_photo = mcontainer.findViewById(R.id.photo1_img);
                                    break;
                            }
                            add_photo.setImageBitmap(imageView);
                            alertDialog.cancel();
                        }

                    }
                    break;
            }
        }

    }

    private void show_freq_questions() {
        //URL will be changed after having webpage. In meantime one which was already created as forum.
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://soft0.upc.edu/~ldatusr14/practica3/forum.cgi"));
        startActivity(browserIntent);
    }

    private void send_help() {
        ImageView photo1 = mcontainer.findViewById(R.id.photo1_img);
        ImageView photo2 = mcontainer.findViewById(R.id.photo2_img);
        ImageView photo3 = mcontainer.findViewById(R.id.photo3_img);

        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        if (bools[0]){
            bitmaps.add(((BitmapDrawable) photo1.getDrawable()).getBitmap());
        }
        if (bools[1]){
            bitmaps.add(((BitmapDrawable) photo2.getDrawable()).getBitmap());
        }
        if (bools[2]){
            bitmaps.add(((BitmapDrawable) photo3.getDrawable()).getBitmap());
        }


        EditText description_ed = mcontainer.findViewById(R.id.input_problem);
        String description = description_ed.getText().toString();

        sendEmail(mcontainer.getContext(), "micromobility_tfg@gmail.com", "Help with user", description, bitmaps);
    }

    public void sendEmail(Context context, String emailTo, String subject, String body, List<Bitmap> bitmaps) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (Bitmap file : bitmaps) {
            Uri uri = saveTempBitmap(file);
            if (uri == null) {
                System.out.println("Error while saving pictures");
            } else {
                uris.add(uri);
            }
        }
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public Uri saveTempBitmap(Bitmap bitmap) {
        if (isExternalStorageWritable()) {
            return saveImage(bitmap);
        } else {
            Toast.makeText(mcontainer.getContext(), "Error while saving images to gallery!",
                    Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private Uri saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "AideBot_question_" + timeStamp + ".jpg";

        File file = new File(myDir, fname);

        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return (Uri.fromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}