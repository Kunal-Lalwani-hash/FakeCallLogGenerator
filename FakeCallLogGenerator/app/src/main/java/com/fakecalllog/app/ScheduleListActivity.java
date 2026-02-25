package com.fakecalllog.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private List<ScheduledCallStore.ScheduledEntry> entries;

    private static final String[] CALL_TYPE_LABELS = {"Incoming", "Outgoing", "Missed", "Did Not Connect"};
    private static final int[] CALL_TYPE_VALUES = {1, 2, 3, 6};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty      = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadEntries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntries();
    }

    private void loadEntries() {
        entries = ScheduledCallStore.getAll(this);
        if (entries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new ScheduleAdapter());
        }
    }

    private String callTypeLabel(int type) {
        for (int i = 0; i < CALL_TYPE_VALUES.length; i++) {
            if (CALL_TYPE_VALUES[i] == type) return CALL_TYPE_LABELS[i];
        }
        return "Unknown";
    }

    private void cancelEntry(ScheduledCallStore.ScheduledEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Schedule")
                .setMessage("Cancel this scheduled call log?")
                .setPositiveButton("Yes", (d, w) -> {
                    Intent intent = new Intent(this, ScheduledCallReceiver.class);
                    intent.putExtra("schedule_id", entry.id);
                    PendingIntent pi = PendingIntent.getBroadcast(this, entry.id, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (am != null) am.cancel(pi);
                    ScheduledCallStore.delete(this, entry.id);
                    loadEntries();
                })
                .setNegativeButton("No", null)
                .show();
    }

    class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scheduled_call, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ScheduledCallStore.ScheduledEntry entry = entries.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String timeStr = sdf.format(new Date(entry.triggerTime));
            String displayName = entry.name.isEmpty() ? entry.number : entry.name + " (" + entry.number + ")";
            holder.tvInfo.setText(displayName + "\n" + callTypeLabel(entry.callType) + " • " + timeStr);
            holder.btnCancel.setOnClickListener(v -> cancelEntry(entry));
        }

        @Override
        public int getItemCount() { return entries.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvInfo;
            Button btnCancel;
            VH(View v) {
                super(v);
                tvInfo    = v.findViewById(R.id.tvInfo);
                btnCancel = v.findViewById(R.id.btnCancel);
            }
        }
    }
}
