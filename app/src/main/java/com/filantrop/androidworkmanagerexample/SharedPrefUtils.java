package com.filantrop.androidworkmanagerexample;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {

    /* SNI */
    public static String getSniHostname(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APPLICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.CURRENT_SNI_SHARED_PREF_KEY, context.getString(R.string.default_sni));
    }

    public static void saveSniHostname(Context context, String newSni) {
        if (newSni != null && !newSni.isBlank()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APPLICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(Constants.CURRENT_SNI_SHARED_PREF_KEY, newSni).apply();
        }
    }

    public static void resetToDefaultSniHostname(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.APPLICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(Constants.CURRENT_SNI_SHARED_PREF_KEY, context.getString(R.string.default_sni)).apply();
    }

}
