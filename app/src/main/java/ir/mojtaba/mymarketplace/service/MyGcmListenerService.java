package ir.mojtaba.mymarketplace.service;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String message = data.getString("message");
    }
}