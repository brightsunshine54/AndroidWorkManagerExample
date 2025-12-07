package com.filantrop.androidworkmanagerexample.sni;

import android.util.Log;

import java.util.Random;

public class SniCheckService {
    private final String TAG = this.getClass().getSimpleName();

    private final Random random = new Random();

    public boolean testSni(String server, String sni) throws InterruptedException {
        Log.i(TAG,"Trying connect to: " + server + " with SNI: " + sni);

        Thread.sleep(random.nextInt(5) * 1000 + 500L);

        return random.nextInt(10000) > 9500;
    }
}
