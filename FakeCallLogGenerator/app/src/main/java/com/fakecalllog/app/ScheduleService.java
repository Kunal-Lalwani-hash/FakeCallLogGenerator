package com.fakecalllog.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ScheduleService extends Service {
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
