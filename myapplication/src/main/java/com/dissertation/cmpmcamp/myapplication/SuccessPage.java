package com.dissertation.cmpmcamp.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView;

import com.nimbusds.jose.JWSObject;

import java.text.ParseException;

public class SuccessPage extends AppCompatActivity implements View.OnClickListener{

    private final AppCompatActivity activity = SuccessPage.this;

    ListView d;


    public static final String F_TAG = "Fingerprint";
    public static final String SN_TAG = "SafetyNetAPI";

    //==============================================================================================
    //  This is the string array for each value displayed in the report
    //==============================================================================================
    public String[] descriptionList = {
            "Details Found",
            "OS Version",
            "Fingerprint",
            "SafetyNet Result",
            "CTS Profile",
            "Basic Integrity"};

    //==============================================================================================
    //  Defines the Image View's so they can be referenced later
    //==============================================================================================
    ImageView DetailsFoundImage;
    ImageView OSVersionImage;
    ImageView FingerprintImage;
    ImageView SafetyNetImage;
    ImageView CTSProfileImage;
    ImageView BasicIntegrity;

    protected void onCreate(Bundle savedInstanceState) {
        String StoredFingerprint = "";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.successful_login);
        d = (ListView) findViewById(R.id.success_description);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, descriptionList);
        d.setAdapter(adapter1);

        //==============================================================================================
        //  This initialises all images, so they can be clicked on
        //==============================================================================================
        DetailsFoundImage = (ImageView) findViewById(R.id.DetailsFoundYN); // Will always be true as you cannot log in without details
        OSVersionImage = (ImageView) findViewById(R.id.OSVersionYN); //Will always be true as OS version check had been checked first
        FingerprintImage = (ImageView) findViewById(R.id.FingerprintYN); //Can be yes or no, if no the report generated will be error'ed
        SafetyNetImage = (ImageView) findViewById(R.id.SafetynetResultYN); //Will be either yes or no depending on CTSProfile & BasicIntegrity
        CTSProfileImage = (ImageView) findViewById(R.id.CTSProfileYN); //Can be yes or no, depends on SN response
        BasicIntegrity = (ImageView) findViewById(R.id.BasicIntegrityYN); //Can be yes or no, depends on SN response

        //==============================================================================================
        //  The onClickListener will allow each image to become 'clickable'
        //==============================================================================================
        DetailsFoundImage.setOnClickListener(this);
        OSVersionImage.setOnClickListener(this);
        FingerprintImage.setOnClickListener(this);
        SafetyNetImage.setOnClickListener(this);
        CTSProfileImage.setOnClickListener(this);
        BasicIntegrity.setOnClickListener(this);

        //==============================================================================================
        //  Starts the Fingerprinting method, to ensure that the application has not been copied
        //==============================================================================================
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SafetyNetAPI fragmentSN = new SafetyNetAPI();
        Fingerprint fragmentF = new Fingerprint();
        transaction.add(fragmentSN, SN_TAG);
        transaction.add(fragmentF, F_TAG);
        transaction.commit();

        //==============================================================================================
        //  Initialises ErrorPage's activity so it can be navigated to if any statements are true
        //==============================================================================================
        final Intent ErrorPage = new Intent(this.activity, ErrorPage.class);

        //==============================================================================================
        //  Checks if Fingerprint generated matches the stored fingerprint taken during registration
        //==============================================================================================
        String HashedArray = Fingerprint.getHashArray();
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        StoredFingerprint = userDetails.getString("Fingerprint", "");

        if (HashedArray.equals(StoredFingerprint)) {
        } else {
            startActivity(ErrorPage);
            finish();
        }

        //==============================================================================================
        //  Checks if OS version is correct or not
        //==============================================================================================
        if (android.os.Build.VERSION.SDK_INT <= 21) {
            startActivity(ErrorPage);
            finish();
        }

        //==============================================================================================
        //  Checks if SafetyNet has returned a correct response.
        //  4000ms delay due to the API call taking time
        //==============================================================================================
            //Will wait 3 seconds before calling SafetyNet API
            Handler APICallDelay = new Handler();
            APICallDelay.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JWSObject jwsObject = JWSObject.parse(SafetyNetAPI.mResult);
                        SafetyNetAPI.AttestResult = jwsObject.getPayload().toJSONObject();
                        String ctsProfileMatch = SafetyNetAPI.AttestResult.getAsString("ctsProfileMatch");
                        String basicIntegrity = SafetyNetAPI.AttestResult.getAsString("basicIntegrity");

                        if (ctsProfileMatch.equals("false")){
                            startActivity(ErrorPage);
                            finish();
                        }

                        if (basicIntegrity.equals("false")){
                            startActivity(ErrorPage);
                            finish();
                        }

                    } catch (ParseException | java.lang.NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Stack trace SN", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 4000);

        //==============================================================================================
        //  Required to show the user what each check means and how it could be changed
        //==============================================================================================
        d.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg1, View view,
                                            int position, long id) {
                        AlertDialog alertDialog = new AlertDialog.Builder(SuccessPage.this).create();
                        switch (position)
                        {
                            case 0:
                                alertDialog.setTitle("Details Found");
                                alertDialog.setMessage("This field checks if there are any details included in the database from the user. " +
                                        "This method will always return true as this check is made to log the user in automatically.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                break;
                            case 1:
                                alertDialog.setTitle("OS Version");
                                alertDialog.setMessage("Operating System (OS) Version is checked to ensure that this application would not run on a 'insecure device'. " +
                                        "\nThis check will always be true as it is a requirement for this application to not run on devices that are on Android version 4.4.4 or below.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                break;
                            case 2:
                                alertDialog.setTitle("Fingerprint");
                                alertDialog.setMessage("A device fingerprint is generated every time the application is launched.\n" +
                                        "This ensures that attackers would not be able to run the app if the app detects a device change.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                break;
                            case 3:
                                alertDialog.setTitle("SafetyNet Result");
                                alertDialog.setMessage("SafetyNet Result will depend on both \nCTSProfileMatch and BasicIntegrity to return true in order to make this result positive.\n"
                                        + "Overall this result should return true. If the result returns something else this may indicate the device has been tampered.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                break;
                            case 4:
                                alertDialog.setTitle("CTSProfileMatch");
                                alertDialog.setMessage("CTSProfileMatch is a true/false response which is used to see if the device matches what Google considers normal.\n" +
                                        "This includes bootloader status amongst other boot time checks.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                break;
                            case 5:
                                alertDialog.setTitle("basicIntegrity");
                                alertDialog.setMessage("basicIntegrity checks if the devices has a superuser binary present.\nIf a binary is detected this will return false, otherwise it will turn true.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                break;
                        }
                    }
                }
        );
    }

    //==============================================================================================
    //  onClick event will always return positive messages due to IF statements above handling any
    //  inappropriate data
    //==============================================================================================
    public void onClick(View v)    {
        switch (v.getId()){
            case R.id.DetailsFoundYN:
                Toast.makeText(getApplicationContext(), "The correct logon credentials have been detected.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.OSVersionYN:
                Toast.makeText(getApplicationContext(), "Your version of Android: " + Build.VERSION.RELEASE + " is compatible.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.FingerprintYN:
                Toast.makeText(getApplicationContext(), "The stored fingerprint matches the same fingerprint detected.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.SafetynetResultYN:
                Toast.makeText(getApplicationContext(), "The overall SafetyNet Result has passed.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.CTSProfileYN:
                Toast.makeText(getApplicationContext(), "CTS Profile matches to what is standard for this device.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.BasicIntegrityYN:
                Toast.makeText(getApplicationContext(), "Basic Integrity remains intact.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}