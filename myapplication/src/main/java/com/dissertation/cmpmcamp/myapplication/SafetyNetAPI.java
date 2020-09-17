package com.dissertation.cmpmcamp.myapplication;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import net.minidev.json.JSONObject;

//==============================================================================================
//  SafetyNetAPI based on the following Google Sample:
//  https://github.com/googlesamples/android-play-safetynet/tree/master/client/java/SafetyNetSample/
//  JSONObject will be passed to other activities in order to be referenced for checks
//==============================================================================================
public class SafetyNetAPI extends android.app.Fragment {

    private static final String TAG = "SafetyNetAPI";
    private final Random mRandom = new SecureRandom();
    public static String mResult;
    public static JSONObject AttestResult;

    //==============================================================================================
    //  onCreate starts the SafetyNet call once this activity is started
    //==============================================================================================
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendSafetyNetRequest();
    }

    //==============================================================================================
    //  Sends the SafetyNet response along with a nonce so it can be confirmed by Google
    //==============================================================================================
    public void sendSafetyNetRequest() {
        String nonceData = "SafetyNet Request" + System.currentTimeMillis();
        byte[] nonce = getRequestNonce(nonceData);
        SafetyNetClient client = SafetyNet.getClient(getActivity());
        Task<SafetyNetApi.AttestationResponse> task = client.attest(nonce, BuildConfig.API_KEY);
        task.addOnSuccessListener(getActivity(), mSuccessListener);
    }

    //==============================================================================================
    //  Creates a secure random number which is attached to the SafetyNet attestation
    //==============================================================================================
    private byte[] getRequestNonce(String data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[24];
        mRandom.nextBytes(bytes);
        try {
            byteStream.write(bytes);
            byteStream.write(data.getBytes());
        } catch (IOException e) {
            return null;
        }
        return byteStream.toByteArray();
    }

    //==============================================================================================
    //  Returns a long string which is later decoded to give the JSON response
    //==============================================================================================
    private OnSuccessListener<SafetyNetApi.AttestationResponse> mSuccessListener =
            new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
                @Override
                public void onSuccess(SafetyNetApi.AttestationResponse attestationResponse) {
                    mResult = attestationResponse.getJwsResult();
                    Log.d(TAG, "Success! SafetyNet result:\n" + mResult + "\n");

                }
            };
}