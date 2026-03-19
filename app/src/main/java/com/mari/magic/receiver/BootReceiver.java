package com.mari.magic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.mari.magic.worker.AnimeWorker;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            PeriodicWorkRequest workRequest =
                    new PeriodicWorkRequest.Builder(
                            AnimeWorker.class,
                            15, TimeUnit.MINUTES
                    ).build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "anime_check",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
            );
        }
    }
}