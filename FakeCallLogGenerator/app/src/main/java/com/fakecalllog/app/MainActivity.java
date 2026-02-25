package com.fakecalllog.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        Button btnAddCallLog = findViewById(R.id.btnAddCallLog);
        Button btnScheduledLogs = findViewById(R.id.btnScheduledLogs);

        btnAddCallLog.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, AddCallLogActivity.class));
            } else {
                checkAndRequestPermissions();
                Toast.makeText(this, "Please grant permissions first", Toast.LENGTH_SHORT).show();
            }
        });

        btnScheduledLogs.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScheduleListActivity.class));
        });
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS
        };
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Some permissions were denied. App may not work properly.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
