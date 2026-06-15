package com.example.dnevniceapp;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.view.View;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dnevniceapp.database.AppDatabase;
import com.example.dnevniceapp.model.DayEntryEntity;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonthDetailsActivity extends AppCompatActivity {

    private TextView textMonthTitle, textTotal;
    private Button btnPickDate, btnAddDay, btnCancelEdit;
    private EditText editHours, editBarTotal, editPeopleCount;
    private DayEntryEntity editingEntry = null;

    private RecyclerView recyclerDays;
    private ArrayList<DayEntryEntity> dayEntries;
    private DayEntryAdapter adapter;

    private Calendar selectedDate;
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

    private AppDatabase db;
    private ExecutorService executorService;

    private int monthId;
    private String monthName;

    private TextView textWorkDays;
    private TextView textAverage;
    private TextView textBestDay;

    private Button btnToggleForm;
    private LinearLayout layoutEntryForm;
    private TextView textTotalHours;


//    private static final double HOURLY_RATE = 250.0;
//    private static final double BAR_PERCENT = 0.05;
    private double hourlyRate;
    private double barPercent;

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

        hourlyRate = prefs.getFloat(SettingsActivity.KEY_HOURLY_RATE, 250f);

        float barPercentValue = prefs.getFloat(SettingsActivity.KEY_BAR_PERCENT, 5f);
        barPercent = barPercentValue / 100.0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_details);

        textMonthTitle = findViewById(R.id.textMonthTitle);
        textTotal = findViewById(R.id.textTotal);
        textWorkDays = findViewById(R.id.textWorkDays);
        textAverage = findViewById(R.id.textAverage);
        textBestDay = findViewById(R.id.textBestDay);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnAddDay = findViewById(R.id.btnAddDay);
        editHours = findViewById(R.id.editHours);
        editBarTotal = findViewById(R.id.editBarTotal);
        editPeopleCount = findViewById(R.id.editPeopleCount);
        recyclerDays = findViewById(R.id.recyclerDays);
        textTotalHours = findViewById(R.id.textTotalHours);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        btnCancelEdit.setOnClickListener(v -> cancelEdit());

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        monthId = getIntent().getIntExtra(MainActivity.EXTRA_MONTH_ID, -1);
        monthName = getIntent().getStringExtra(MainActivity.EXTRA_MONTH_NAME);

        if (monthId == -1) {
            Toast.makeText(this, "Greška: mesec nije pronađen", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnToggleForm = findViewById(R.id.btnToggleForm);
        layoutEntryForm = findViewById(R.id.layoutEntryForm);

        btnToggleForm.setOnClickListener(v -> {
            if (layoutEntryForm.getVisibility() == View.VISIBLE) {
                layoutEntryForm.setVisibility(View.GONE);
                btnToggleForm.setText("+ Novi dan");
            } else {
                layoutEntryForm.setVisibility(View.VISIBLE);
                btnToggleForm.setText("Sakrij unos");
            }
        });

        textMonthTitle.setText(monthName);

        selectedDate = Calendar.getInstance();
        updateDateButtonText();

        dayEntries = new ArrayList<>();

        adapter = new DayEntryAdapter(dayEntries, new DayEntryAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(DayEntryEntity entry) {
                editingEntry = entry;

                layoutEntryForm.setVisibility(View.VISIBLE);
                btnToggleForm.setText("Sakrij unos");

                btnPickDate.setText(entry.date);
                editHours.setText(String.valueOf(entry.hours));
                editBarTotal.setText(String.valueOf(entry.barTotal));
                editPeopleCount.setText(String.valueOf(entry.peopleCount));

                btnAddDay.setText("Sačuvaj");
                btnCancelEdit.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDayLongClick(DayEntryEntity entry) {
                showDeleteDialog(entry);
            }
        });

        recyclerDays.setLayoutManager(new LinearLayoutManager(this));
        recyclerDays.setAdapter(adapter);


        btnPickDate.setOnClickListener(v -> openDatePicker());

        btnAddDay.setOnClickListener(v -> addDay());


        loadSettings();
        loadDays();
    }
    private void cancelEdit() {
        editHours.setText("");
        editBarTotal.setText("");
        editPeopleCount.setText("");

        editingEntry = null;

        btnAddDay.setText("+ Dodaj dan");
        btnCancelEdit.setVisibility(View.GONE);

        layoutEntryForm.setVisibility(View.GONE);
        btnToggleForm.setText("+ Novi dan");

        Toast.makeText(this, "Izmena otkazana", Toast.LENGTH_SHORT).show();
    }
    private void exportPdfAndShare() {
        executorService.execute(() -> {
            List<DayEntryEntity> entries = db.dayEntryDao().getDaysForMonth(monthId);

            try {
                PdfDocument pdfDocument = new PdfDocument();

                Paint paint = new Paint();
                paint.setTextSize(14);

                Paint titlePaint = new Paint();
                titlePaint.setTextSize(22);
                titlePaint.setFakeBoldText(true);

                PdfDocument.PageInfo pageInfo =
                        new PdfDocument.PageInfo.Builder(595, 842, 1).create();

                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                int y = 40;

                canvas.drawText("ŠANK KALKULATOR", 40, y, titlePaint);
                y += 35;

                canvas.drawText("Mesec: " + monthName, 40, y, paint);
                y += 25;

                double total = 0;
                double totalHours = 0;
                double bestDay = 0;

                for (DayEntryEntity entry : entries) {
                    total += entry.earned;
                    totalHours += entry.hours;

                    if (entry.earned > bestDay) {
                        bestDay = entry.earned;
                    }
                }

                double average = entries.size() > 0 ? total / entries.size() : 0;

                canvas.drawText("Ukupno: " + moneyFormat.format(total) + " RSD", 40, y, paint);
                y += 22;

                canvas.drawText("Radnih dana: " + entries.size(), 40, y, paint);
                y += 22;

                canvas.drawText("Ukupno sati: " + moneyFormat.format(totalHours) + "h", 40, y, paint);
                y += 22;

                canvas.drawText("Prosek po danu: " + moneyFormat.format(average) + " RSD", 40, y, paint);
                y += 22;

                canvas.drawText("Najbolji dan: " + moneyFormat.format(bestDay) + " RSD", 40, y, paint);
                y += 35;

                canvas.drawLine(40, y, 555, y, paint);
                y += 25;

                for (DayEntryEntity entry : entries) {

                    if (y > 780) {
                        pdfDocument.finishPage(page);

                        pageInfo = new PdfDocument.PageInfo.Builder(
                                595,
                                842,
                                pdfDocument.getPages().size() + 1
                        ).create();

                        page = pdfDocument.startPage(pageInfo);
                        canvas = page.getCanvas();
                        y = 40;
                    }

                    canvas.drawText("Datum: " + entry.date, 40, y, paint);
                    y += 20;

                    canvas.drawText("Sati: " + entry.hours, 60, y, paint);
                    y += 20;

                    canvas.drawText("Šank: " + moneyFormat.format(entry.barTotal) + " RSD", 60, y, paint);
                    y += 20;

                    canvas.drawText("Ljudi: " + entry.peopleCount, 60, y, paint);
                    y += 20;

                    canvas.drawText("Zarada: " + moneyFormat.format(entry.earned) + " RSD", 60, y, paint);
                    y += 25;

                    canvas.drawLine(40, y, 555, y, paint);
                    y += 20;
                }

                pdfDocument.finishPage(page);

                File file = new File(
                        getCacheDir(),
                        monthName.replace(" ", "_") + "_SankKalkulator.pdf"
                );

                FileOutputStream outputStream = new FileOutputStream(file);
                pdfDocument.writeTo(outputStream);

                pdfDocument.close();
                outputStream.close();

                Uri uri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        file
                );

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> {
                    startActivity(Intent.createChooser(shareIntent, "Podeli PDF"));
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri exportu PDF-a", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(Calendar.YEAR, selectedYear);
                    selectedDate.set(Calendar.MONTH, selectedMonth);
                    selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);
                    updateDateButtonText();
                },
                year,
                month,
                day
        );

        dialog.show();
    }

    private void updateDateButtonText() {
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);
        int month = selectedDate.get(Calendar.MONTH) + 1;
        int year = selectedDate.get(Calendar.YEAR);

        btnPickDate.setText(day + "." + month + "." + year);
    }
    private void confirmUpdate(
            String dateText,
            double hours,
            double barTotal,
            int peopleCount,
            double earned
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Potvrda izmene")
                .setMessage("Da li želiš da sačuvaš izmene za ovaj dan?")
                .setPositiveButton("DA", (dialog, which) -> {

                    executorService.execute(() -> {

                        db.dayEntryDao().updateDayEntry(
                                editingEntry.id,
                                dateText,
                                hours,
                                barTotal,
                                peopleCount,
                                earned
                        );

                        runOnUiThread(() -> {

                            editHours.setText("");
                            editBarTotal.setText("");
                            editPeopleCount.setText("");

                            editingEntry = null;

                            btnAddDay.setText("+ Dodaj dan");

                            layoutEntryForm.setVisibility(View.GONE);
                            btnToggleForm.setText("+ Novi dan");

                            loadDays();

                            Toast.makeText(
                                    this,
                                    "Izmena sačuvana",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    });

                })
                .setNegativeButton("NE", null)
                .show();
    }

    private void addDay() {
        String hoursText = editHours.getText().toString().trim();
        String barTotalText = editBarTotal.getText().toString().trim();
        String peopleCountText = editPeopleCount.getText().toString().trim();

        if (hoursText.isEmpty() || barTotalText.isEmpty() || peopleCountText.isEmpty()) {
            Toast.makeText(this, "Unesi broj sati, cifru šanka i broj ljudi", Toast.LENGTH_SHORT).show();
            return;
        }

        double hours = Double.parseDouble(hoursText);
        double barTotal = Double.parseDouble(barTotalText);
        int peopleCount = Integer.parseInt(peopleCountText);

        if (peopleCount <= 0) {
            Toast.makeText(this, "Broj ljudi mora biti veći od 0", Toast.LENGTH_SHORT).show();
            return;
        }

        double hourlyPart = hours * hourlyRate;
        double barPartPerPerson = (barTotal * barPercent) / peopleCount;
        double earned = hourlyPart + barPartPerPerson;

        String dateText = btnPickDate.getText().toString();

        if (editingEntry == null) {
            DayEntryEntity dayEntry = new DayEntryEntity(
                    monthId,
                    dateText,
                    hours,
                    barTotal,
                    peopleCount,
                    earned
            );

            executorService.execute(() -> {
                db.dayEntryDao().insertDayEntry(dayEntry);

                runOnUiThread(() -> {
                    clearFormAfterSave();
                    loadDays();
                });
            });

        } else {
            confirmUpdate(dateText, hours, barTotal, peopleCount, earned);
        }
    }

    private void clearFormAfterSave() {
        editHours.setText("");
        editBarTotal.setText("");
        editPeopleCount.setText("");

        editingEntry = null;
        btnAddDay.setText("+ Dodaj dan");

        layoutEntryForm.setVisibility(View.GONE);
        btnToggleForm.setText("+ Novi dan");
        btnCancelEdit.setVisibility(View.GONE);
    }

    private void loadDays() {
        executorService.execute(() -> {
            List<DayEntryEntity> loadedDays = db.dayEntryDao().getDaysForMonth(monthId);

            Double totalValue = db.dayEntryDao().getTotalForMonth(monthId);
            final double total = totalValue != null ? totalValue : 0;

            runOnUiThread(() -> {
                dayEntries.clear();
                dayEntries.addAll(loadedDays);

                adapter.notifyDataSetChanged();
                int workDays = loadedDays.size();

                double average = 0;
                double bestDay = 0;
                double totalHours = 0;

                for (DayEntryEntity entry : loadedDays) {
                    totalHours += entry.hours;
                }

                if (workDays > 0) {
                    average = total / workDays;
                }

                for (DayEntryEntity entry : loadedDays) {
                    if (entry.earned > bestDay) {
                        bestDay = entry.earned;
                    }
                }

                textWorkDays.setText("Radnih dana: " + workDays);

                textAverage.setText(
                        "Prosek po danu: " +
                                moneyFormat.format(average) +
                                " RSD"
                );
                textTotalHours.setText(
                        "Ukupno sati: " + moneyFormat.format(totalHours) + "h"
                );

                textBestDay.setText(
                        "Najbolji dan: " +
                                moneyFormat.format(bestDay) +
                                " RSD"
                );

                textTotal.setText("Ukupno: " + moneyFormat.format(total) + " RSD");
            });
        });
    }
    private void showDeleteDialog(DayEntryEntity entry) {
        new AlertDialog.Builder(this)
                .setTitle("Brisanje dana")
                .setMessage("Da li želiš da obrišeš ovaj dan?")
                .setPositiveButton("DA", (dialog, which) -> deleteDay(entry))
                .setNegativeButton("NE", null)
                .show();
    }

    private void deleteDay(DayEntryEntity entry) {
        executorService.execute(() -> {
            db.dayEntryDao().deleteDayEntry(entry.id);

            runOnUiThread(() -> {
                Toast.makeText(this, "Dan obrisan", Toast.LENGTH_SHORT).show();
                loadDays();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();

        if (adapter != null) {
            loadDays();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_export) {
            exportPdfAndShare();
            return true;
        }

        if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}