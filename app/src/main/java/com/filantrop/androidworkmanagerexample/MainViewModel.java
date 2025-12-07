package com.filantrop.androidworkmanagerexample;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.filantrop.androidworkmanagerexample.sni.SniAutoSelectWorker;
import com.filantrop.androidworkmanagerexample.sni.SniDto;
import com.filantrop.androidworkmanagerexample.sni.SniRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

public class MainViewModel extends AndroidViewModel {
    private final String TAG = this.getClass().getSimpleName();

    // LiveData for SNI text field
    @Getter
    private final MutableLiveData<String> currentSni = new MutableLiveData<>("yandex.ru");

    @Getter
    private final List<String> servers = List.of("Sweden", "Denmark", "Italy");

    private final WorkManager workManager;

    @Getter
    private final MutableLiveData<String> progressCount = new MutableLiveData<>();

    @Getter
    private final MutableLiveData<Boolean> searchInProgress = new MutableLiveData<>(false);

    private final SniRepository sniRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);

        workManager = WorkManager.getInstance(application);

        String sniHostname = SharedPrefUtils.getSniHostname(application);
        currentSni.postValue(sniHostname);

        sniRepository = new SniRepository(getApplication());

        observeWork();
    }

    private void observeWork() {
        workManager.getWorkInfosForUniqueWorkLiveData(SniAutoSelectWorker.WORK_TAG)
                .observeForever(workInfos -> {
                    if (workInfos == null || workInfos.isEmpty()) {
                        searchInProgress.postValue(false);
                        return;
                    }

                    WorkInfo workInfo = workInfos.get(0);
                    if (workInfo.getState() == WorkInfo.State.RUNNING) {
                        searchInProgress.postValue(true);
                        Data progress = workInfo.getProgress();
                        int checked = progress.getInt(SniAutoSelectWorker.CHECKED_COUNT_KEY, 0);
                        Log.i(TAG, "Work in progress, checked: " + checked);

                        progressCount.postValue(checked + "/10000");
                    } else {
                        searchInProgress.postValue(false);

                        Log.i(TAG, "Work finished: " + workInfo.getState());

                        Data output = workInfo.getOutputData();
                        String sni = output.getString(SniAutoSelectWorker.FOUNDED_SNI_KEY);
                        Log.i(TAG, "Found working SNI: " + sni);

                        String error = output.getString(SniAutoSelectWorker.ERROR_KEY);
                        Log.i(TAG, "Error: " + error);
                    }
                });


    }

    public void updateCurrentSni(String newSniValue) {
        Log.i(TAG, "Updating SNI value to: " + newSniValue);
        SharedPrefUtils.saveSniHostname(getApplication(), newSniValue);
        currentSni.postValue(newSniValue);
    }

    public void resetSniToDefault() {
        Log.i(TAG, "Reset SNI to default");
        SharedPrefUtils.resetToDefaultSniHostname(getApplication());
        currentSni.postValue(getApplication().getString(R.string.default_sni));
    }

    public void startSNISearch(String selectedServer) {
        Log.d(TAG, "Starting SNI auto-select for server: " + selectedServer);
        Toast.makeText(getApplication(), "Starting auto-select for " +selectedServer, Toast.LENGTH_SHORT).show();

        sniRepository.resetCheckedListenable().addListener(()->{
            workManager.cancelUniqueWork(SniAutoSelectWorker.WORK_TAG);

            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            Map<String, String> params = Map.of(SniAutoSelectWorker.SERVER_KEY, selectedServer);
            Data data = new Data(params);

            OneTimeWorkRequest matchingRequest = new OneTimeWorkRequest.Builder(
                    SniAutoSelectWorker.class)
                    .setConstraints(constraints)
                    .setInputData(data)
                    .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS)
                    .build();

            workManager.enqueueUniqueWork(
                    SniAutoSelectWorker.WORK_TAG,
                    ExistingWorkPolicy.REPLACE,
                    matchingRequest);
        }, Executors.newSingleThreadExecutor());
    }

    public void stopSNISearch() {
        workManager.cancelUniqueWork(SniAutoSelectWorker.WORK_TAG);
    }

    public void readFileContent(Context context, Uri uri) {
        Log.d(TAG, "File selected: " + uri.getPath());

        List<SniDto> sniList = new ArrayList<>();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Trim whitespace and ignore empty or commented lines
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty() && !trimmedLine.startsWith("#")) {
                    sniList.add(new SniDto(trimmedLine));
                }
            }

            if (!sniList.isEmpty()) {
                sniRepository.insertAll(sniList);
                Log.d(TAG, "Successfully inserted " + sniList.size() + " SNIs into the database.");
            } else {
                Log.d(TAG, "No valid SNIs found in the selected file.");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error reading SNI file", e);
        }
    }

    public void deleteAllSni() {
        sniRepository.deleteAll();
    }
}