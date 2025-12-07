package com.filantrop.androidworkmanagerexample.sni;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SniAutoSelectWorker extends Worker {
    public static final String SERVER_KEY = "SERVER_KEY";
    public static final String WORK_TAG = "SniAutoSelectWorker";
    public static final String FOUNDED_SNI_KEY = "FOUNDED_SNI_KEY";
    public static final String CHECKED_COUNT_KEY = "CHECKED_COUND_KEY";
    public static final String ERROR_KEY = "error";
    private final String TAG = this.getClass().getSimpleName();

    public SniAutoSelectWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String selectedServer = getInputData().getString(SERVER_KEY);

            SniDatabase db = SniDatabase.getInstance(getApplicationContext());
            SniDao sniDao = db.sniDao();
            SniCheckService connectionService = new SniCheckService();

            int total = sniDao.getTotalCount();
            int checkedCount = sniDao.getCheckedCount();

            while (checkedCount < total) {
                boolean stopped = isStopped();
                if (stopped) break;
                SniDto sniDto = sniDao.getNextUnchecked();
                if (sniDto == null) break;

                checkedCount++;
                setProgressAsync(createProgressData(checkedCount));

                String sni = sniDto.getSni();
                boolean success = connectionService.testSni(selectedServer, sni);

                if (success) {
                    setProgressAsync(createProgressData(checkedCount));
                    return Result.success(createSuccessData(sni));
                } else {
                    sniDao.setChecked(sniDto.getId());
                }
            }

            return Result.success(createSuccessData(null));
        } catch (Exception e) {
            Log.e(TAG, "Error during sni check", e);
            return Result.failure(createErrorData(e.getMessage()));
        }
    }

    private Data createProgressData(int current) {
        return new Data.Builder()
                .putInt(CHECKED_COUNT_KEY, current)
                .build();
    }

    private Data createSuccessData(String sni) {
        return new Data.Builder()
                .putString(FOUNDED_SNI_KEY, sni)
                .build();
    }

    private Data createErrorData(String error) {
        return new Data.Builder()
                .putString(ERROR_KEY, error)
                .build();
    }
}