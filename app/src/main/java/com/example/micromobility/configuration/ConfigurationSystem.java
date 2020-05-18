package com.example.micromobility.configuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.micromobility.BuildConfig;
import com.example.micromobility.LoginActivity;
import com.example.micromobility.ui.home.HomeFragment;
import com.example.micromobility.MainActivity;
import com.example.micromobility.R;
import com.example.micromobility.Storage.InternalStorage;

import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class ConfigurationSystem extends Fragment {

    AlertDialog alertDialog;
    AlertDialog alertDialog1;
    View mcontainer;
    String username, email;
    Bitmap profile_picture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mcontainer = inflater.inflate(R.layout.configuration, container, false);

        LinearLayout help = mcontainer.findViewById(R.id.help_btn);
        LinearLayout account = mcontainer.findViewById(R.id.account_btn);
        LinearLayout notifications = mcontainer.findViewById(R.id.notifications_btn);

        LinearLayout video_micromobility = mcontainer.findViewById(R.id.video_micromobility);
        LinearLayout invite_friends = mcontainer.findViewById(R.id.invite_friends);
        LinearLayout app_information = mcontainer.findViewById(R.id.app_information);

        Button button_to_home = mcontainer.findViewById(R.id.button_to_home);

        LinearLayout close_session = mcontainer.findViewById(R.id.close_session);

        TextView username_txt = mcontainer.findViewById(R.id.Name_user);
        TextView email_address = mcontainer.findViewById(R.id.email_address);

        ImageView profile_picture_img = mcontainer.findViewById(R.id.profile_picture);


        // Take username and profile picture of our user and set it
        //get USERNAME AND PHOTO
        InternalStorage in = new InternalStorage(mcontainer.getContext());
        username = in.getUsername();
        profile_picture = in.getPhoto(username);

        username_txt.setText(username);
        profile_picture_img.setImageBitmap(profile_picture);
        email = in.getValue(username, "email_user");
        email_address.setText(email);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getString(R.string.help) + " \uD83C\uDFE0");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Help()).commit();

            }
        });
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getString(R.string.account) + " \uD83C\uDFE0");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Account()).commit();


            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getString(R.string.notification) + " \uD83C\uDFE0");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new Notifications()).commit();

            }
        });
        button_to_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home \uD83C\uDFE0");
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();

            }
        });
        profile_picture_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        video_micromobility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video_micromobility();

            }
        });
        invite_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invite_friends();

            }
        });
        app_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app_information();

            }
        });
        close_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mcontainer.getContext());
                builder.setView(getLayoutInflater().inflate(R.layout.log_out, null));

                final AlertDialog alertDialog = builder.create();
                // To prevent a dialog from closing when the positive button clicked, set onShowListener to
                // the AlertDialog
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialogInterface) {

                        final Button YesDelete = alertDialog.findViewById(R.id.YesDelete);
                        final Button NotDelete = alertDialog.findViewById(R.id.NotDelete);

                        YesDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //DELETE ACCOUNT
                                InternalStorage in = new InternalStorage(mcontainer.getContext());
                                in.removeUsername();
                                startActivity(new Intent(mcontainer.getContext(), LoginActivity.class));
                                alertDialog.cancel();
                            }
                        });
                        NotDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.cancel();
                            }
                        });

                    }
                });
                alertDialog.show();
            }
        });
        return mcontainer;
    }

    private void video_micromobility() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mcontainer.getContext());

        builder.setView(getLayoutInflater().inflate(R.layout.show_video, null));

        alertDialog1 = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog1.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button closingDialog = alertDialog1.findViewById(R.id.closingDialog);
                VideoView videoView = (VideoView) alertDialog1.findViewById(R.id.VideoView);
                //Creating MediaController
                MediaController mediaController = new MediaController(mcontainer.getContext());
                mediaController.setAnchorView(videoView);
        /*
        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.pae_video;
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(path));
        videoView.requestFocus();
        videoView.start();

         */

                closingDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog1.cancel();
                    }
                });
            }
        });

        alertDialog1.show();
    }


    private void invite_friends() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "MicroMobility AI App");
            String shareMessage = "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            Toast.makeText(mcontainer.getContext(), "Error while trying to share Micromobility!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void app_information() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mcontainer.getContext());

        builder.setView(getLayoutInflater().inflate(R.layout.app_information, null));

        alertDialog = builder.create();
        // To prevent a dialog from closing when the positive button clicked, set onShowListener to
        // the AlertDialog
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                final Button closeDialog = alertDialog.findViewById(R.id.CloseDialog);


                closeDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
            }
        });

        alertDialog.show();
    }

    private void selectImage() {
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
                        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);
                    }

                });

                choose_from_gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
                        Bitmap roundView = getCroppedBitmap(imageView);
                        ImageView add_photo = mcontainer.findViewById(R.id.profile_picture);
                        add_photo.setImageBitmap(roundView);
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
                            Bitmap roundView = getCroppedBitmap(imageView);
                            ImageView add_photo = mcontainer.findViewById(R.id.profile_picture);
                            add_photo.setImageBitmap(roundView);
                            alertDialog.cancel();
                        }

                    }
                    break;
            }
        }

    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

}
