package com.dissertation.cmpmcamp.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private final AppCompatActivity activity = RegisterActivity.this;

    public static String PublicEmail;
    public static String PublicPassword;
    public static String PublicFingerprint;

    private TextInputEditText textInputEditTextName;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;
    private TextInputEditText textInputEditTextConfirmPassword;

    private AppCompatButton appCompatButtonRegister;
    private AppCompatTextView appCompatTextViewLoginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        getDetails();

        //todo: try to add this to a separate public void.... (ignore if breaks)
        SharedPreferences userDetails = getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String StoredEmail = userDetails.getString("Email", "");
        String StoredPassword = userDetails.getString("Password", "");
        String StoredFingerprint = userDetails.getString("Fingerprint", "");
        PublicEmail = StoredEmail;
        PublicPassword = StoredPassword;
        PublicFingerprint = StoredFingerprint;

        //==============================================================================================
        //  This initialises all images, so they can be clicked on
        //==============================================================================================
        textInputEditTextName = findViewById(R.id.textInputEditTextName);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        textInputEditTextConfirmPassword = findViewById(R.id.textInputEditTextConfirmPassword);
        appCompatButtonRegister = findViewById(R.id.appCompatButtonRegister);
        appCompatTextViewLoginLink = findViewById(R.id.appCompatTextViewLoginLink);

        //==============================================================================================
        //  The onClickListener will allow each image to become 'clickable'
        //==============================================================================================
        appCompatButtonRegister.setOnClickListener(this);
        appCompatTextViewLoginLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent MainActivity = new Intent(activity, MainActivity.class);
        switch (v.getId()) {
            case R.id.appCompatButtonRegister:
                //todo: add DB checks
                writeToArray();
                break;
            case R.id.appCompatTextViewLoginLink:
                startActivity(MainActivity);
                break;
        }
    }

    public void writeToArray() {
        //==============================================================================================
        //  This creates the array so that the user name and password can be added to the array
        //==============================================================================================
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);

        String Email = (textInputEditTextEmail.getText().toString().trim());
        String Name = (textInputEditTextName.getText().toString().trim());
        String Password1 = (textInputEditTextPassword.getText().toString().trim());
        String Password2 = (textInputEditTextConfirmPassword.getText().toString().trim());
        String HashedArray = Fingerprint.getHashArray();

        SharedPreferences.Editor UserDB = userDetails.edit();
        UserDB.clear();

        Intent MainActivity = new Intent(activity, MainActivity.class);

        if (Email.isEmpty() || Name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter some details", Toast.LENGTH_LONG).show();
        } else {
            UserDB.putString("Email", Email);
            UserDB.putString("Password", Password1);
            UserDB.putString("Fingerprint", HashedArray);
            UserDB.commit();

            if (Password1.isEmpty() || Password1.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Passwords cannot be null!!\nPlease retry", Toast.LENGTH_LONG).show();
                textInputEditTextPassword.setText(null);
                textInputEditTextConfirmPassword.setText(null);
                UserDB.putString("Password", "");
                UserDB.commit();
            } else if (Password1.equals(Password2)) {
                UserDB.putString("Password", Password1);
                UserDB.commit();
                Toast.makeText(getApplicationContext(), "Success passwords match, account created", Toast.LENGTH_LONG).show();
                textInputEditTextName.setText(null);
                textInputEditTextEmail.setText(null);
                textInputEditTextPassword.setText(null);
                textInputEditTextConfirmPassword.setText(null);
                startActivity(MainActivity);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Passwords do not match!!\nPlease retry", Toast.LENGTH_LONG).show();
                textInputEditTextPassword.setText(null);
                textInputEditTextConfirmPassword.setText(null);
                UserDB.clear();
                UserDB.commit();
            }
        }
    }

    public void getDetails() {
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        String StoredEmail = userDetails.getString("Email", "");
        String StoredPassword = userDetails.getString("Password", "");
        PublicEmail = StoredEmail;
        PublicPassword = StoredPassword;
    }
}
