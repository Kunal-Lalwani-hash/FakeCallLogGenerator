package com.fakecalllog.app;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;

public class ScheduledCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int scheduleId = intent.getIntExtra("schedule_id", -1);
        if (scheduleId == -1) return;

        ScheduledCallStore.ScheduledEntry entry = null;
        for (ScheduledCallStore.ScheduledEntry e : ScheduledCallStore.getAll(context)) {
            if (e.id == scheduleId) { entry = e; break; }
        }
        if (entry == null) return;

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, entry.number);
        values.put(CallLog.Calls.TYPE, entry.callType);
        values.put(CallLog.Calls.DATE, entry.triggerTime);
        values.put(CallLog.Calls.DURATION, entry.duration);
        if (entry.name != null && !entry.name.isEmpty()) {
            values.put(CallLog.Calls.CACHED_NAME, entry.name);
        }
        if (entry.callType == AddCallLogActivity.CALL_TYPE_DID_NOT_CONNECT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(CallLog.Calls.MISSED_REASON, 3);
            }
        }

        context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

        // Remove from store
        ScheduledCallStore.delete(context, scheduleId);
    }
}
