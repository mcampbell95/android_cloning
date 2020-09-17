package com.dissertation.cmpmcamp.myapplication;

import android.content.DialogInterface;
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

public class ErrorPage extends AppCompatActivity implements View.OnClickListener{

    public static final String F_TAG = "Fingerprint";
    ListView d;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unsuccessful_login);
        d = (ListView) findViewById(R.id.error_description);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, descriptionList);
        d.setAdapter(adapter1);

        //==============================================================================================
        //  Lets the user that something went wrong & to check the report (this activity)
        //==============================================================================================
        Toast.makeText(getApplicationContext(), "Error detected!!!\nPlease view the above report.", Toast.LENGTH_LONG).show();

        //==============================================================================================
        //  This initialises all images, so they can be clicked on
        //==============================================================================================
        DetailsFoundImage = (ImageView) findViewById(R.id.ErrorDetailsFoundYN); // Will always be true as you cannot log in without details
        OSVersionImage = (ImageView) findViewById(R.id.ErrorOSVersionYN); //Will always be true as OS version check had been checked first
        FingerprintImage = (ImageView) findViewById(R.id.ErrorFingerprintYN); //Can be yes or no, if no the report generated will be error'ed
        SafetyNetImage = (ImageView) findViewById(R.id.ErrorSafetynetResultYN); //Will be either yes or no depending on CTSProfile & BasicIntegrity
        CTSProfileImage = (ImageView) findViewById(R.id.ErrorCTSProfileYN); //Can be yes or no, depends on SN response
        BasicIntegrity = (ImageView) findViewById(R.id.ErrorBasicIntegrityYN); //Can be yes or no, depends on SN response

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
        Fingerprint fragmentF = new Fingerprint();
        transaction.add(fragmentF, F_TAG);
        transaction.commit();

        //==============================================================================================
        //  Checks if Fingerprint generated matches the stored fingerprint taken during registration
        //==============================================================================================
        String HashedArray = null;
        HashedArray = Fingerprint.getHashArray();
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        String StoredFingerprint = userDetails.getString("Fingerprint", "");
        if (HashedArray.equals(StoredFingerprint)) {
        }
        else {
            FingerprintImage = (ImageView) findViewById(R.id.ErrorFingerprintYN); //Can be yes or no, if no the report generated will be error'ed
            FingerprintImage.setImageResource(R.drawable.ic_x_button);
        }

        //==============================================================================================
        //  Checks if OS version is correct or not
        //=============================================================================================
        if (android.os.Build.VERSION.SDK_INT <= 21){
            OSVersionImage = (ImageView) findViewById(R.id.ErrorOSVersionYN); //Can be yes or no, if no the report generated will be error'ed
            OSVersionImage.setImageResource(R.drawable.ic_x_button);
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
        //==============================================================================================
        //  Checks if SafetyNet has returned a correct response
        //==============================================================================================
        try {
            final JWSObject jwsObject = JWSObject.parse(SafetyNetAPI.mResult);
            SafetyNetAPI.AttestResult = jwsObject.getPayload().toJSONObject();
            String ctsProfileMatch = SafetyNetAPI.AttestResult.getAsString("ctsProfileMatch");
            String basicIntegrity = SafetyNetAPI.AttestResult.getAsString("basicIntegrity");

            if (ctsProfileMatch.equals("false")){
                CTSProfileImage = (ImageView) findViewById(R.id.ErrorCTSProfileYN); //Can be yes or no, if no the report generated will be error'ed
                CTSProfileImage.setImageResource(R.drawable.ic_x_button);
                // As CTSProfile is false the resulting SafetyNet will revert to failed
                SafetyNetImage = (ImageView) findViewById(R.id.ErrorSafetynetResultYN); //Can be yes or no, if no the report generated will be error'ed
                SafetyNetImage.setImageResource(R.drawable.ic_x_button);
            }
            if (basicIntegrity.equals("false")){
                BasicIntegrity = (ImageView) findViewById(R.id.ErrorBasicIntegrityYN); //Can be yes or no, if no the report generated will be error'ed
                BasicIntegrity.setImageResource(R.drawable.ic_x_button);
                // As CTSProfile is false the resulting SafetyNet will revert to failed
                SafetyNetImage = (ImageView) findViewById(R.id.ErrorSafetynetResultYN); //Can be yes or no, if no the report generated will be error'ed
                SafetyNetImage.setImageResource(R.drawable.ic_x_button);
            }
            //todo add a check for null, if the app has went straight to error screen (under the time limit of 3000ms)

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
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> arg1, View view, int position, long id) {
                        AlertDialog alertDialog = new AlertDialog.Builder(ErrorPage.this).create();
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
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
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
    //  onClick event unlike SuccessPage needs to check what is being shown (tick or x).
    //  IF statement will then provide the user will the correct output
    //==============================================================================================
    public void onClick(View v)    {
        String HashedArray = Fingerprint.getHashArray();
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        String StoredFingerprint = userDetails.getString("Fingerprint", "");
        String ctsProfileMatch = SafetyNetAPI.AttestResult.getAsString("ctsProfileMatch");
        String basicIntegrity = SafetyNetAPI.AttestResult.getAsString("basicIntegrity");
        String advice = SafetyNetAPI.AttestResult.getAsString("advice");

        switch (v.getId()){
            case R.id.ErrorDetailsFoundYN:
                Toast.makeText(getApplicationContext(), "The correct logon credentials have been detected.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ErrorOSVersionYN:
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    Toast.makeText(getApplicationContext(), "Your version of Android: " + Build.VERSION.RELEASE + " is compatible.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    Toast.makeText(getApplicationContext(), "This version of Android is not supported.", Toast.LENGTH_SHORT).show();
                    break;
                }
            case R.id.ErrorFingerprintYN:
                if (HashedArray.equals(StoredFingerprint)) {
                    Toast.makeText(getApplicationContext(), "Success your device fingerprints matches", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Device fingerprint generated:\n" + HashedArray + "\n\nDoes not match the stored fingerprint:\n"
                            +StoredFingerprint, Toast.LENGTH_LONG).show();
                    break;
                }
            case R.id.ErrorSafetynetResultYN:
                if (ctsProfileMatch.equals("true") && (basicIntegrity.equals("true"))){
                    Toast.makeText(getApplicationContext(), "The overall SafetyNet Result has passed.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Both CTSProfile & BasicIntegrity must return true for SafetyNet to pass.\nSee below error(s) for details.", Toast.LENGTH_LONG).show();
                    break;
                }
            case R.id.ErrorCTSProfileYN:
                if (ctsProfileMatch.equals("true")) {
                    Toast.makeText(getApplicationContext(), "CTSProfile matches to what is standard for this device.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    Toast.makeText(getApplicationContext(), "CTSProfile has returned false for this device. Google advices to:\n" + advice, Toast.LENGTH_LONG).show();
                    break;
                }
            case R.id.ErrorBasicIntegrityYN:
                if (basicIntegrity.equals("true")) {
                    Toast.makeText(getApplicationContext(), "Basic Integrity remains intact.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    Toast.makeText(getApplicationContext(), "BasicIntegrity has returned false for this device. Google advices to:\n" + advice, Toast.LENGTH_SHORT).show();
                    break;
                }
        }
    }
}