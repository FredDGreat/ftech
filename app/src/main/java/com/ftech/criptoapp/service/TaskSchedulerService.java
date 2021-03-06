package com.ftech.criptoapp.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;

/**
 * Created by FRED.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TaskSchedulerService extends JobService {

    //TODO: Remove
    public static final String LOG_TAG = TaskSchedulerService.class.getSimpleName();
    private static final String MY_INTENT = "com.ftech.criptoapp.cryptservice.CUSTOM_INTENT";
    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //TODO: Remove
            /*Toast.makeText(getApplicationContext(),
                    "JobService task running", Toast.LENGTH_SHORT)
                    .show();*/
            broadcastIntent();
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }
    });


    @Override
    public boolean onStartJob(JobParameters params) {

        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }

    public void broadcastIntent() {
        Intent intent = new Intent(String.valueOf(this));
        intent.setAction(MY_INTENT);
        getApplicationContext().sendBroadcast(intent);
    }


}
