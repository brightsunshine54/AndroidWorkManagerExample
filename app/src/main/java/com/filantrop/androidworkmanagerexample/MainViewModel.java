package com.filantrop.androidworkmanagerexample;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();

    // LiveData for SNI text field
    private final MutableLiveData<String> currentSni = new MutableLiveData<>("yandex.ru");

    public MainViewModel(@NonNull Application application) {
        super(application);

        String sniHostname = SharedPrefUtils.getSniHostname(application);
        currentSni.postValue(sniHostname);
    }

    public LiveData<String> getCurrentSni() {
        return currentSni;
    }

    public void handleLoadSniClick() {
        Log.i(TAG, "Handling load SNI from file request in ViewModel");
        // TODO: Implement loading SNI from file functionality
        // This method will be called when "Load SNI from file" button is clicked
    }

    public void handleDeleteSniClick() {
        Log.i(TAG, "Handling delete loaded SNI request in ViewModel");
        // TODO: Implement deleting loaded SNI functionality
        // This method will be called when "Delete loaded SNI" button is clicked
    }

    public void handleAutoSelectSniClick() {
        Log.i(TAG, "Handling auto select SNI request in ViewModel");
        // TODO: Implement auto selecting SNI functionality
        // This method will be called when "Auto select SNI" button is clicked
    }

    public void updateCurrentSni(String newSniValue) {
        Log.i(TAG, "Updating SNI value to: " + newSniValue);
        SharedPrefUtils.saveSniHostname(getApplication(), newSniValue);
        currentSni.postValue(newSniValue);
    }

    public void resetSniToDefult() {
        Log.i(TAG, "Reset SNI to default");
        SharedPrefUtils.resetToDefaultSniHostname(getApplication());
        currentSni.postValue(getApplication().getString(R.string.default_sni));
    }
}