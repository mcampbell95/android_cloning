package com.dissertation.cmpmcamp.myapplication;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

//Todo: change variable names in the future to ensure that its not the same as sample!!
public class Fingerprint extends android.app.Fragment {

    private static final String TAG = "Fingerprint"; //Defines this java file so it can be used elsewhere
    //==============================================================================================
    //  Makes strings static so that can be referenced in getHashArray method
    //==============================================================================================
    static String android_id;
    static String phone_model;
    static String build_release;
    static String timezone;
    static String kernel_version;
    static String mac_address;
    static int width_height;
    String HashedArray = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //==============================================================================================
        //  This gets AndroidID
        //==============================================================================================
        android_id = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //==============================================================================================
        //  This gets the phone model
        //==============================================================================================
        phone_model = Build.MODEL;

        //==============================================================================================
        //  This gets the build release
        //==============================================================================================
        build_release = Build.VERSION.RELEASE;

        //==============================================================================================
        //  This gets the kernel version
        //==============================================================================================
        kernel_version = System.getProperty("os.version");

        //==============================================================================================
        //  This gets the mac address
        //==============================================================================================
        WifiManager wifiManager = (WifiManager) (getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        mac_address = wInfo.getMacAddress();

        //==============================================================================================
        //  This returns the height x width of the display and adds them together
        //  to get an additional int
        //==============================================================================================
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        width_height = width|height;
    }

    public static String getHashArray() {
        //==============================================================================================
        //  This creates the array of all values ready to be hashed
        //==============================================================================================
        Object[] fp_values = new Object[]{};
        ArrayList<Object> nonHashedArray = new ArrayList<>(Arrays.asList(fp_values));
        nonHashedArray.add(android_id);
        nonHashedArray.add(phone_model);
        nonHashedArray.add(build_release);
        nonHashedArray.add(timezone);
        nonHashedArray.add(kernel_version);
        nonHashedArray.add(mac_address);
        nonHashedArray.add(width_height);

        //==============================================================================================
        //  This logs all values to the debug log for debugging purposes
        //==============================================================================================
        Log.d(TAG, "Android ID\n" + android_id);
        Log.d(TAG, "Phone Model\n" + phone_model);
        Log.d(TAG, "Build Release\n" + build_release);
        Log.d(TAG, "Timezone\n" + timezone);
        Log.d(TAG, "Kernel Version\n" + kernel_version);
        Log.d(TAG, "Mac_address\n" + mac_address);
        Log.d(TAG, "Width Height\n" + width_height);

        String plaintext = nonHashedArray.toString();  //This takes the hashed array and later converts it into a String.
        Log.d(TAG, "Non Hashed array is as follows:\n" + plaintext);

        //==============================================================================================
        //  Method to convert the Array into a SHA256 hash for quickly identify a device
        //==============================================================================================
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("SHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(plaintext.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashedArray = bigInt.toString(16);
        while (hashedArray.length() < 64) {
            hashedArray = "0" + hashedArray;
        }
        Log.d(TAG, "Success!! Hashed text\n" + hashedArray);
        return hashedArray; // returns hashedArray as a value so it can be passed to the MainActivity
    }
}
