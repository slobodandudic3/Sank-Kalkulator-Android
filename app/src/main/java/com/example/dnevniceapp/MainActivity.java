package com.example.dnevniceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dnevniceapp.database.AppDatabase;
import com.example.dnevniceapp.model.MonthEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.example.dnevniceapp.backup.BackupData;
import com.example.dnevniceapp.model.DayEntryEntity;

public class MainActivity extends AppCompatActivity {

    private EditText editMonthName;
    private Button btnAddMonth;
    private RecyclerView recyclerMonths;
    private ArrayList<Double> monthTotals;
    private MonthAdapter adapter;
    private ArrayList<MonthEntity> months;

    private AppDatabase db;
    private ExecutorService executorService;
    private TextView textDashboardTotal, textDashboardDays, textDashboardHours;

    public static final String EXTRA_MONTH_ID = "month_id";
    public static final String EXTRA_MONTH_NAME = "month_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editMonthName = findViewById(R.id.editMonthName);
        btnAddMonth = findViewById(R.id.btnAddMonth);
        recyclerMonths = findViewById(R.id.recyclerMonths);
        textDashboardTotal = findViewById(R.id.textDashboardTotal);
        textDashboardDays = findViewById(R.id.textDashboardDays);
        textDashboardHours = findViewById(R.id.textDashboardHours);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        months = new ArrayList<>();
        monthTotals = new ArrayList<>();

        adapter = new MonthAdapter(months, monthTotals, new MonthAdapter.OnMonthClickListener() {
            @Override
            public void onMonthClick(MonthEntity month) {
                Intent intent = new Intent(MainActivity.this, MonthDetailsActivity.class);
                intent.putExtra(EXTRA_MONTH_ID, month.id);
                intent.putExtra(EXTRA_MONTH_NAME, month.name);
                startActivity(intent);
            }
            @Override
            public void onMonthLongClick(MonthEntity month) {
                showMonthOptionsDialog(month);
            }
        });

        recyclerMonths.setLayoutManager(new LinearLayoutManager(this));
        recyclerMonths.setAdapter(adapter);

        loadMonths();

        btnAddMonth.setOnClickListener(v -> addMonth());


    }
    @Override
    protected void onResume() {
        super.onResume();
        loadMonths();
    }


    private void addMonth() {
        String monthName = editMonthName.getText().toString().trim();

        if (monthName.isEmpty()) {
            Toast.makeText(this, "Unesi naziv meseca", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            MonthEntity month = new MonthEntity(monthName);
            db.monthDao().insertMonth(month);

            runOnUiThread(() -> {
                editMonthName.setText("");
                loadMonths();
            });
        });
    }
    private void createBackup() {

        executorService.execute(() -> {

            try {

                BackupData backupData = new BackupData(
                        db.monthDao().getAllMonths(),
                        db.dayEntryDao().getAllDayEntries()
                );

                Gson gson = new Gson();

                File file = new File(
                        getFilesDir(),
                        "sank_kalkulator_backup.json"
                );

                FileWriter writer = new FileWriter(file);

                gson.toJson(backupData, writer);

                writer.flush();
                writer.close();

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Backup uspešno napravljen",
                                Toast.LENGTH_SHORT
                        ).show()
                );

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Greška pri backup-u",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }
    private void restoreBackup() {

        executorService.execute(() -> {

            try {

                File file = new File(
                        getFilesDir(),
                        "sank_kalkulator_backup.json"
                );

                if (!file.exists()) {

                    runOnUiThread(() ->
                            Toast.makeText(
                                    this,
                                    "Backup ne postoji",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );

                    return;
                }

                Gson gson = new Gson();

                FileReader reader = new FileReader(file);

                BackupData backupData =
                        gson.fromJson(reader, BackupData.class);

                reader.close();

                db.dayEntryDao().deleteAllDayEntries();
                db.monthDao().deleteAllMonths();

                for (MonthEntity month : backupData.months) {
                    db.monthDao().insertMonth(month);
                }

                for (DayEntryEntity day : backupData.days) {
                    db.dayEntryDao().insertDayEntry(day);
                }

                runOnUiThread(() -> {

                    loadMonths();

                    Toast.makeText(
                            this,
                            "Restore uspešan",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Greška pri restore-u",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    private void loadMonths() {
        executorService.execute(() -> {
            List<MonthEntity> loadedMonths = db.monthDao().getAllMonths();
            Double dashboardTotalValue = db.dayEntryDao().getTotalAllMonths();
            final double dashboardTotal = dashboardTotalValue != null ? dashboardTotalValue : 0;

            int dashboardDays = db.dayEntryDao().getTotalWorkDays();

            Double dashboardHoursValue = db.dayEntryDao().getTotalHoursAllMonths();
            final double dashboardHours = dashboardHoursValue != null ? dashboardHoursValue : 0;

            ArrayList<Double> loadedTotals = new ArrayList<>();

            for (MonthEntity month : loadedMonths) {
                Double totalValue = db.dayEntryDao().getTotalForMonth(month.id);
                double total = totalValue != null ? totalValue : 0;

                loadedTotals.add(total);
            }

            runOnUiThread(() -> {
                months.clear();
                monthTotals.clear();

                months.addAll(loadedMonths);
                monthTotals.addAll(loadedTotals);

                textDashboardTotal.setText(
                        "Ukupno svih meseci: " +
                                String.format("%.2f RSD", dashboardTotal)
                );

                textDashboardDays.setText(
                        "Ukupno radnih dana: " + dashboardDays
                );

                textDashboardHours.setText(
                        "Ukupno sati: " +
                                String.format("%.2f", dashboardHours) +
                                "h"
                );
                adapter.notifyDataSetChanged();
            });
        });
    }
    private void showMonthOptionsDialog(MonthEntity month) {

        String[] options = {
                "Izmeni naziv",
                "Obriši mesec"
        };

        new AlertDialog.Builder(this)
                .setTitle(month.name)
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        showEditMonthNameDialog(month);
                    } else if (which == 1) {
                        showDeleteMonthDialog(month);
                    }
                })
                .setNegativeButton("OTKAŽI", null)
                .show();
    }
    private void showEditMonthNameDialog(MonthEntity month) {

        EditText input = new EditText(this);

        input.setText(month.name);
        input.setSelection(input.getText().length());
        input.setSingleLine(true);
        input.setHint("Unesi naziv meseca");

        int horizontalPadding =
                (int) (24 * getResources().getDisplayMetrics().density);

        FrameLayout container = new FrameLayout(this);

        container.setPadding(
                horizontalPadding,
                0,
                horizontalPadding,
                0
        );

        container.addView(input);

        AlertDialog editDialog = new AlertDialog.Builder(this)
                .setTitle("Izmena naziva meseca")
                .setMessage("Unesi novi naziv:")
                .setView(container)
                .setPositiveButton("SAČUVAJ", null)
                .setNegativeButton("OTKAŽI", null)
                .create();

        editDialog.setOnShowListener(dialogInterface -> {

            editDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String newName =
                                input.getText().toString().trim();

                        if (newName.isEmpty()) {
                            input.setError("Naziv meseca ne sme biti prazan");
                            return;
                        }

                        if (newName.equals(month.name)) {
                            editDialog.dismiss();
                            return;
                        }

                        updateMonthName(month, newName);
                        editDialog.dismiss();
                    });
        });

        editDialog.show();
    }
    private void updateMonthName(MonthEntity month, String newName) {

        executorService.execute(() -> {

            db.monthDao().updateMonthName(month.id, newName);

            runOnUiThread(() -> {
                Toast.makeText(
                        this,
                        "Naziv meseca je izmenjen",
                        Toast.LENGTH_SHORT
                ).show();

                loadMonths();
            });
        });
    }
    private void showDeleteMonthDialog(MonthEntity month) {
        new AlertDialog.Builder(this)
                .setTitle("Brisanje meseca")
                .setMessage("Obrisati mesec '" + month.name + "' ?")
                .setPositiveButton("DA", (dialog, which) -> deleteMonth(month))
                .setNegativeButton("NE", null)
                .show();
    }

    private void deleteMonth(MonthEntity month) {
        executorService.execute(() -> {
            db.dayEntryDao().deleteAllDaysForMonth(month.id);
            db.monthDao().deleteMonth(month.id);

            runOnUiThread(() -> {
                Toast.makeText(this, "Mesec obrisan", Toast.LENGTH_SHORT).show();
                loadMonths();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_settings) {

            Intent intent =
                    new Intent(this, SettingsActivity.class);

            startActivity(intent);

            return true;
        }


//        if (id == R.id.menu_statistics) {
//
//            Toast.makeText(
//                    this,
//                    "Statistika uskoro",
//                    Toast.LENGTH_SHORT
//            ).show();
//
//            return true;
//        }

        if (id == R.id.menu_export) {

            Toast.makeText(
                    this,
                    "PDF uskoro",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        }

        if (id == R.id.menu_backup) {
            createBackup();
            return true;
        }

        if (id == R.id.menu_restore) {
            restoreBackup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}