package com.example.dnevniceapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "settings";
    public static final String KEY_HOURLY_RATE = "hourly_rate";
    public static final String KEY_BAR_PERCENT = "bar_percent";

    private EditText editHourlyRate, editBarPercent;
    private Button btnSaveSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editHourlyRate = findViewById(R.id.editHourlyRate);
        editBarPercent = findViewById(R.id.editBarPercent);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        float hourlyRate = prefs.getFloat(KEY_HOURLY_RATE, 250f);
        float barPercent = prefs.getFloat(KEY_BAR_PERCENT, 5f);

        editHourlyRate.setText(String.valueOf(hourlyRate));
        editBarPercent.setText(String.valueOf(barPercent));
    }

    private void saveSettings() {
        String hourlyRateText = editHourlyRate.getText().toString().trim();
        String barPercentText = editBarPercent.getText().toString().trim();

        if (hourlyRateText.isEmpty() || barPercentText.isEmpty()) {
            Toast.makeText(this, "Unesi oba polja", Toast.LENGTH_SHORT).show();
            return;
        }

        float hourlyRate = Float.parseFloat(hourlyRateText);
        float barPercent = Float.parseFloat(barPercentText);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        prefs.edit()
                .putFloat(KEY_HOURLY_RATE, hourlyRate)
                .putFloat(KEY_BAR_PERCENT, barPercent)
                .apply();

        Toast.makeText(this, "Podešavanja sačuvana", Toast.LENGTH_SHORT).show();

        finish();
    }
}