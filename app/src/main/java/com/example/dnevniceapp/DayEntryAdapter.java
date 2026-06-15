package com.example.dnevniceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dnevniceapp.model.DayEntryEntity;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DayEntryAdapter extends RecyclerView.Adapter<DayEntryAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(DayEntryEntity entry);
        void onDayLongClick(DayEntryEntity entry);
    }

    private ArrayList<DayEntryEntity> dayEntries;
    private OnDayClickListener listener;
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

    public DayEntryAdapter(ArrayList<DayEntryEntity> dayEntries, OnDayClickListener listener) {
        this.dayEntries = dayEntries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_entry, parent, false);

        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayEntryEntity entry = dayEntries.get(position);

        holder.textDate.setText(entry.date);

        holder.textDetails.setText(
                "Sati: " + entry.hours +
                        " | Šank: " + moneyFormat.format(entry.barTotal) +
                        " | Ljudi: " + entry.peopleCount
        );

        holder.textEarned.setText(
                "Zarada: " + moneyFormat.format(entry.earned) + " RSD"
        );
//        if (entry.earned < 3000) {
//            holder.itemView.setBackgroundColor(0xFFFFEBEE); // svetlo crveno
//        } else if (entry.earned < 5000) {
//            holder.itemView.setBackgroundColor(0xFFFFF8E1); // svetlo žuto
//        } else {
//            holder.itemView.setBackgroundColor(0xFFE8F5E9); // svetlo zeleno
//        }

        holder.itemView.setOnClickListener(v -> listener.onDayClick(entry));

        holder.itemView.setOnLongClickListener(v -> {
            listener.onDayLongClick(entry);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return dayEntries.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView textDate, textDetails, textEarned;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);

            textDate = itemView.findViewById(R.id.textDate);
            textDetails = itemView.findViewById(R.id.textDetails);
            textEarned = itemView.findViewById(R.id.textEarned);
        }
    }
}