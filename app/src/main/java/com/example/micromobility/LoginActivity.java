package com.example.micromobility;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.micromobility.Language.SetLanguage;
import com.example.micromobility.Storage.InternalStorage;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity"; //for logger
    private static final int REQUEST_SIGNUP = 0; // in order to check number max of errors
    private String username;
    private InternalStorage in;

    /*      PATTERN MATCHING --> used to check password complexity
             ^                 # start-of-string
            (?=.*[0-9])       # a digit must occur at least once
            (?=.*[a-z])       # a lower case letter must occur at least once
            (?=.*[A-Z])       # an upper case letter must occur at least once
            (?=.*[@#$%^&+=])  # a special character must occur at least once
            (?=\S+$)          # no whitespace allowed in the entire string
            .{8,}             # anything, at least eight places though
            $                 # end-of-string
    */
    private static final String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(SetLanguage.onAttach(base));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "You have entered to the Login");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //CHECK IF USER DID NOT LOGGED OUT SO CACHÉ ENTERS DIRECTLY:
        in = new InternalStorage(LoginActivity.this);
        username = in.getUsername();
        if(!username.isEmpty()){
            Log.d(TAG, "Due to Caché memory, directly entered to Main");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            this.finish();
        }

        //In order to set up new Registration as user clicks on "Don't Have an Account?  Sign Up"
        TextView signUp_text = findViewById(R.id.signUp_text);
        signUp_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        //If user does have an account, we will proceed to get its inputs to validate user authentication
        Button signIN_btn = findViewById(R.id.signIN_button);
        signIN_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }



    //Handles actual login
    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        Button signIN_btn = findViewById(R.id.signIN_button);
        signIN_btn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.NewDialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();


        EditText username_txt = findViewById(R.id.username);
        EditText password_txt = findViewById(R.id.password);

        String email = username_txt.getText().toString();
        String password = password_txt.getText().toString();

        // TODO: Implement your own authentication logic here.Call to presenter methods.
        if (true) {
            //onLoginSuccess();  //theorically only this line
            Handler handler = new Handler();

            //in order to check correct behaviour of Progress dialog
            handler.postDelayed(new Runnable() {
                public void run() {
                    onLoginSuccess();
                    progressDialog.dismiss();
                }
            }, 3000);

            return;
        }

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public boolean validate() {
        boolean valid = true;

        EditText username_txt = findViewById(R.id.username);
        EditText password_txt = findViewById(R.id.password);
        username = username_txt.getText().toString();
        String password = password_txt.getText().toString();

        if (username.isEmpty()) {
            username_txt.setError("Enter a valid Username");
            valid = false;
        } else {
            //used to clear the error
            username_txt.setError(null);
        }

        if (!matches(password)) {
            password_txt.setError("Not a Valid Password. Enter Password with at least 8 characters and  at least one uppercase, lowercase, number and special character");
            valid = false;
        } else {
            password_txt.setError(null);
        }

        return valid;
    }

    public boolean matches(String pwd) {
        //returns:
        //      - true - if password is valid.
        //      - false - if password not valid.
        return (pwd.matches(pattern));
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        Button signIN_btn = findViewById(R.id.signIN_button);
        ;
        signIN_btn.setEnabled(true);
    }

    public void onLoginSuccess() {
        Button signIN_btn = findViewById(R.id.signIN_button);
        InternalStorage in = new InternalStorage(LoginActivity.this);
        in.setUsername(username);
        signIN_btn.setEnabled(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}