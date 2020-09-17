package com.dissertation.cmpmcamp.myapplication;
//todo: Ensure that the app runs on 8.1.0 with no errors

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final AppCompatActivity activity = MainActivity.this;

    public static final String TAG = "MainActivity";
    public static final String SN_TAG = "SafetyNetAPI";
    public static final String F_TAG = "Fingerprint";
    public static final String R_TAG = "RegisterActivity";
    public String PublicEmail = RegisterActivity.PublicEmail;
    public String PublicPassword = RegisterActivity.PublicPassword;

    //==============================================================================================
    //  Defines all views & input methods for the user
    //==============================================================================================
    private NestedScrollView nestedScrollView;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;
    private AppCompatButton appCompatButtonLogin;
    private AppCompatTextView textViewLinkRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo: Check if adding a id statement to capture the login_activity would be required
        setContentView(R.layout.login_activity);
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        PublicEmail = userDetails.getString("Email", "");
        PublicPassword = userDetails.getString("Password", "");

        //==============================================================================================
        //  Checks what version of Android is present.
        //  SDK 21 (version no. 5.0) is checked here
        //==============================================================================================
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            //==============================================================================================
            //  Starts both the SafetyNet and Fingerprint solutions in the background
            //==============================================================================================
            if (savedInstanceState == null) {
                android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
                SafetyNetAPI fragmentSN = new SafetyNetAPI();
                Fingerprint fragmentF = new Fingerprint();
                transaction.add(fragmentSN, SN_TAG);
                transaction.add(fragmentF, F_TAG);
                transaction.commit();
                Log.d(TAG, "Starting Fingerprinting");
                Log.d(TAG, "Calling SafetyNet");
            }
        }
        //==============================================================================================
        //  The application will close & will not stay active
        //==============================================================================================
        else {
            Toast.makeText(getApplicationContext(), "Your version of Android: " + Build.VERSION.RELEASE + " is NOT compatible.\nClosing application!!!!", Toast.LENGTH_LONG).show();
            this.finishAffinity();
        }

        //==============================================================================================
        //  This initialises all images, so they can be clicked on
        //==============================================================================================
        nestedScrollView = findViewById(R.id.nestedScrollView);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        appCompatButtonLogin = findViewById(R.id.appCompatButtonLogin);
        textViewLinkRegister = findViewById(R.id.textViewLinkRegister);

        //==============================================================================================
        //  The onClickListener will allow each image to become 'clickable'
        //==============================================================================================
        appCompatButtonLogin.setOnClickListener(this);
        textViewLinkRegister.setOnClickListener(this);
    }

    protected void onResume() {
        super.onResume();
        //==============================================================================================
        //  Checks to see if the user is logged on on or not. Stops null (unregistered users).
        // ==============================================================================================
        if (PublicEmail.isEmpty() && PublicPassword.isEmpty()) {
            this.accountError();
        } else if (PublicEmail.equals(PublicEmail) && PublicPassword.equals(PublicPassword)) {
            Intent SuccessPage = new Intent(this.activity, SuccessPage.class);
            startActivity(SuccessPage);
            finish();
        } else {
            checkAccount();
        }
    }

    //==============================================================================================
    //  onClick will enable the login & register register
    //==============================================================================================
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appCompatButtonLogin:
                checkAccount();
                break;
            case R.id.textViewLinkRegister:
                Intent RegisterUser = new Intent(this.activity, RegisterActivity.class);
                startActivity(RegisterUser);
                finish();
                break;
        }
    }
    //==============================================================================================
    //  checkAccount will check all values inputted into the DB.
    //  When called, will automatically log the user in upon resume
    //==============================================================================================
    public void checkAccount() {
        String Email = (textInputEditTextEmail.getText().toString());
        String Password = (textInputEditTextPassword.getText().toString());

        try {
            if (Email.isEmpty() || Password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "You must input details!!", Toast.LENGTH_LONG).show();
                textInputEditTextEmail.setText(null);
                textInputEditTextPassword.setText(null);
            } else if (PublicEmail.contains(Email) && PublicPassword.contains(Password)) {
                textInputEditTextEmail.setText(null);
                textInputEditTextPassword.setText(null);
                Intent SuccessPage = new Intent(this.activity, SuccessPage.class);
                startActivity(SuccessPage);
            } else {
                Toast.makeText(getApplicationContext(), "Could not find those details, please check if the details are correct!!", Toast.LENGTH_LONG).show();
                textInputEditTextEmail.setText(null);
                textInputEditTextPassword.setText(null);
            }
        } catch (java.lang.NullPointerException exception) {
            Log.d(TAG, "Exception\n" + exception);
            textInputEditTextEmail.setText(null);
            textInputEditTextPassword.setText(null);
            accountError();
            Toast.makeText(getApplicationContext(), "Error thrown!!", Toast.LENGTH_LONG).show();
        }
    }
    //==============================================================================================
    //  Checks DB for empty values, will let the user know that there is no accounts set up
    //==============================================================================================
    public void accountError() {
        if (PublicEmail.isEmpty() || PublicPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "The array is empty, please register!!", Toast.LENGTH_LONG).show();
            textInputEditTextEmail.setText(null);
            textInputEditTextPassword.setText(null);
        } else {
            checkAccount();
        }
    }
}