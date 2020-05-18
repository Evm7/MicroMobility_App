package com.example.micromobility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.Language.SetLanguage;
import com.example.micromobility.Storage.InternalStorage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;


public class RegistrationActivityOptional extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    String birth_date, postal_code, gender, age, username;
    Bitmap roundView;
    AlertDialog alertDialog;
    Context mcontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_optional);
        mcontainer = getApplicationContext();

        //get USERNAME AND PHOTO
        InternalStorage in = new InternalStorage(RegistrationActivityOptional.this);
        username = in.getUsername();
        String language = in.getValue(username, "language");
        System.out.println(language);
        if (language.startsWith("Spa") || language.startsWith("Es")){
            SetLanguage.setLocale(RegistrationActivityOptional.this, "es");
        }
        else{
            SetLanguage.setLocale(RegistrationActivityOptional.this, "en");
        }

        //In case user does have an account, return to SIGN IN display
        TextView signIn_text = findViewById(R.id.new_signIn_text_optional);
        signIn_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivityOptional.this, LoginActivity.class));
                finish();
            }
        });

        //In case user press SIGN UP button, create new account
        Button signUp_btn = findViewById(R.id.signUp_button);
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        ImageView add_photo = findViewById(R.id.add_photo_here);
        Button calendar_btn = findViewById(R.id.calendar_image_button);
        add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        calendar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dateDialog = new DatePickerDialog(view.getContext(), datePickerListener, mYear, mMonth, mDay);
                dateDialog.getDatePicker().setMaxDate(new Date().getTime());
                dateDialog.show();
            }

            ;
        });
    }

    //Handles actual login
    public void createAccount() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignUPFailed();
            return;
        }

        Button signUp_btn = findViewById(R.id.signUp_button);
        signUp_btn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivityOptional.this,
                R.style.NewDialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        EditText postal_code_txt = findViewById(R.id.add_postalcode);
        EditText birth_date_txt = findViewById(R.id.add_birthdate);
        Spinner gender_txt = findViewById(R.id.add_gender);

        String postal_code = postal_code_txt.getText().toString();
        birth_date = birth_date_txt.getText().toString();
        String gender = gender_txt.getSelectedItem().toString();


        // TODO: Implement your own signup logic here. Call to presenter methods.
        if (true) {
            //onLoginSuccess();  //theorically only this line
            Handler handler = new Handler();

            //in order to check correct behaviour of Progress dialog
            handler.postDelayed(new Runnable() {
                public void run() {
                    onSignUPSuccess();
                    progressDialog.dismiss();
                }
            }, 3000);

            return;
        }


        //If happens 3 seconds without verification, we suppose Failure in SignUp.
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignUPFailed();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public boolean validate() {
        boolean valid = true;

        EditText postal_code_txt = findViewById(R.id.add_postalcode);
        EditText birth_date_txt = findViewById(R.id.add_birthdate);
        Spinner gender_txt = findViewById(R.id.add_gender);

        postal_code = postal_code_txt.getText().toString();
        birth_date = birth_date_txt.getText().toString();
        gender = gender_txt.getSelectedItem().toString();

        if (postal_code.isEmpty() || postal_code.length() < 3) {
            postal_code_txt.setError("Enter a valid PostCode");
            valid = false;
        } else {
            //used to clear the error
            postal_code_txt.setError(null);
        }
        if (birth_date.isEmpty()) {
            birth_date_txt.setError("Enter a valid date");
            valid = false;
        } else {
            birth_date_txt.setError(null);
        }


        TextView errorTextview = (TextView) gender_txt.getSelectedView();
        if (gender.isEmpty()) {
            errorTextview.setError("Choose your gender");
            valid = false;
        } else {
            //used to clear the error
            errorTextview.setError(null);
        }
        return valid;
    }


    private void onSignUPFailed() {
        Toast.makeText(getBaseContext(), "Unable to create account. Try again!", Toast.LENGTH_LONG).show();
        Button signUp_btn = findViewById(R.id.signUp_button);
        signUp_btn.setEnabled(true);
    }

    private void onSignUPSuccess() {
        Button signUp_btn = findViewById(R.id.signUp_button);
        addToUser();
        signUp_btn.setEnabled(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void addToUser(){
        InternalStorage in = new InternalStorage(RegistrationActivityOptional.this);
        String username = in.getUsername();
        HashMap<String, String> new_user = in.getInformation(username);
        new_user.put("gender", gender);
        new_user.put("birth_day", birth_date);
        new_user.put("postal_code", postal_code);
        in.createUser(new_user);
        in.savePhoto(roundView, username);
    }


    //Handlers Calendar uniq day selection
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            String format = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
            EditText birth_date_txt = findViewById(R.id.add_birthdate);
            birth_date_txt.setText(format);
            age = Integer.toString(calculateAge(c.getTimeInMillis()));
        }
    };

    int calculateAge(long date) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(date);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }
        return age;
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivityOptional.this);

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
                        roundView = getCroppedBitmap (imageView);
                        ImageView add_photo = findViewById(R.id.add_photo_here);
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
                                imageStream = mcontainer.getContentResolver().openInputStream(selectedImageUri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            Bitmap imageView = BitmapFactory.decodeStream(imageStream);
                            roundView = getCroppedBitmap (imageView);
                            ImageView add_photo = findViewById(R.id.add_photo_here);
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(SetLanguage.onAttach(base, "en"));
    }

}