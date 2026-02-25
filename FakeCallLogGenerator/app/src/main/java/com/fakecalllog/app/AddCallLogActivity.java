package com.fakecalllog.app;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Calendar;
import java.util.Locale;

public class AddCallLogActivity extends AppCompatActivity {

    // Call Types
    public static final int CALL_TYPE_INCOMING  = CallLog.Calls.INCOMING_TYPE;   // 1
    public static final int CALL_TYPE_OUTGOING  = CallLog.Calls.OUTGOING_TYPE;   // 2
    public static final int CALL_TYPE_MISSED    = CallLog.Calls.MISSED_TYPE;     // 3
    public static final int CALL_TYPE_DID_NOT_CONNECT = 6; // custom / BLOCKED type

    private EditText etName, etNumber, etDuration;
    private Spinner spCallType;
    private TextView tvDate, tvTime;
    private Button btnPickContact, btnSave, btnSchedule;
    private Switch switchSchedule;
    private CardView cardSchedule;

    private Calendar selectedDateTime = Calendar.getInstance();
    private boolean isScheduled = false;

    private final String[] callTypeLabels = {
            "Incoming", "Outgoing", "Missed", "Did Not Connect"
    };
    private final int[] callTypeValues = {
            CALL_TYPE_INCOMING, CALL_TYPE_OUTGOING, CALL_TYPE_MISSED, CALL_TYPE_DID_NOT_CONNECT
    };

    private ActivityResultLauncher<Intent> contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    handleContactPicked(contactUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_call_log);

        initViews();
        setupCallTypeSpinner();
        setupDateTimePickers();
        setupScheduleSwitch();
        setupButtons();

        updateDateTimeDisplay();
    }

    private void initViews() {
        etName        = findViewById(R.id.etName);
        etNumber      = findViewById(R.id.etNumber);
        etDuration    = findViewById(R.id.etDuration);
        spCallType    = findViewById(R.id.spCallType);
        tvDate        = findViewById(R.id.tvDate);
        tvTime        = findViewById(R.id.tvTime);
        btnPickContact= findViewById(R.id.btnPickContact);
        btnSave       = findViewById(R.id.btnSave);
        btnSchedule   = findViewById(R.id.btnScheduleAdd);
        switchSchedule= findViewById(R.id.switchSchedule);
        cardSchedule  = findViewById(R.id.cardSchedule);
    }

    private void setupCallTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, callTypeLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCallType.setAdapter(adapter);

        spCallType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // If Missed or Did Not Connect -> duration makes no sense, hide/disable it
                boolean showDuration = (position == 0 || position == 1);
                etDuration.setEnabled(showDuration);
                etDuration.setAlpha(showDuration ? 1f : 0.4f);
                if (!showDuration) etDuration.setText("0");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDateTimePickers() {
        tvDate.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                        updateDateTimeDisplay();
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        tvTime.setOnClickListener(v -> {
            TimePickerDialog tpd = new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateDateTimeDisplay();
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    false);
            tpd.show();
        });
    }

    private void updateDateTimeDisplay() {
        tvDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d",
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH) + 1,
                selectedDateTime.get(Calendar.DAY_OF_MONTH)));
        tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE)));
    }

    private void setupScheduleSwitch() {
        cardSchedule.setVisibility(View.GONE);
        switchSchedule.setOnCheckedChangeListener((btn, checked) -> {
            isScheduled = checked;
            cardSchedule.setVisibility(checked ? View.VISIBLE : View.GONE);
            btnSave.setVisibility(checked ? View.GONE : View.VISIBLE);
            btnSchedule.setVisibility(checked ? View.VISIBLE : View.GONE);
        });
    }

    private void setupButtons() {
        btnPickContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactPickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> saveCallLog());
        btnSchedule.setOnClickListener(v -> scheduleCallLog());
    }

    private void handleContactPicked(Uri contactUri) {
        Cursor cursor = getContentResolver().query(contactUri,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name   = cursor.getString(0);
            String number = cursor.getString(1);
            etName.setText(name);
            etNumber.setText(number);
            cursor.close();
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etNumber.getText())) {
            etNumber.setError("Phone number is required");
            return false;
        }
        return true;
    }

    private void saveCallLog() {
        if (!validateInputs()) return;

        String name     = etName.getText().toString().trim();
        String number   = etNumber.getText().toString().trim();
        int callType    = callTypeValues[spCallType.getSelectedItemPosition()];
        long timestamp  = selectedDateTime.getTimeInMillis();
        int duration    = 0;
        try { duration = Integer.parseInt(etDuration.getText().toString().trim()); }
        catch (NumberFormatException ignored) {}

        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.TYPE, callType);
        values.put(CallLog.Calls.DATE, timestamp);
        values.put(CallLog.Calls.DURATION, duration);
        if (!TextUtils.isEmpty(name)) {
            values.put(CallLog.Calls.CACHED_NAME, name);
        }
        // Mark "Did Not Connect" in the NEW_TYPE field available on Android 10+
        if (callType == CALL_TYPE_DID_NOT_CONNECT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(CallLog.Calls.MISSED_REASON, 3); // USER_MISSED_NO_ANSWER
            }
        }

        Uri result = getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        if (result != null) {
            Toast.makeText(this, "✅ Call log added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "❌ Failed to add call log.", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleCallLog() {
        if (!validateInputs()) return;

        long triggerTime = selectedDateTime.getTimeInMillis();
        if (triggerTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future date/time for scheduling.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name   = etName.getText().toString().trim();
        String number = etNumber.getText().toString().trim();
        int callType  = callTypeValues[spCallType.getSelectedItemPosition()];
        int duration;
        try { duration = Integer.parseInt(etDuration.getText().toString().trim()); }
        catch (NumberFormatException e) { duration = 0; }

        // Save to SharedPreferences for listing
        int scheduleId = (int) System.currentTimeMillis();
        ScheduledCallStore.save(this, scheduleId, name, number, callType, triggerTime, duration);

        Intent intent = new Intent(this, ScheduledCallReceiver.class);
        intent.putExtra("schedule_id", scheduleId);

        PendingIntent pi = PendingIntent.getBroadcast(this, scheduleId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
        }

        Toast.makeText(this, "⏰ Call log scheduled!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
