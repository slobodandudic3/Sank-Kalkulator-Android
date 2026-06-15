package com.example.dnevniceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dnevniceapp.model.MonthEntity;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    public interface OnMonthClickListener {
        void onMonthClick(MonthEntity month);
        void onMonthLongClick(MonthEntity month);
    }

    private ArrayList<MonthEntity> months;
    private ArrayList<Double> totals;
    private OnMonthClickListener listener;
    private DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

    public MonthAdapter(ArrayList<MonthEntity> months,
                        ArrayList<Double> totals,
                        OnMonthClickListener listener) {
        this.months = months;
        this.totals = totals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_month, parent, false);

        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        MonthEntity month = months.get(position);
        double total = totals.get(position);

        holder.textMonthName.setText(month.name);
        holder.textMonthTotal.setText("Ukupno: " + moneyFormat.format(total) + " RSD");

        holder.itemView.setOnClickListener(v -> listener.onMonthClick(month));

        holder.itemView.setOnLongClickListener(v -> {
            listener.onMonthLongClick(month);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {

        TextView textMonthName, textMonthTotal;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);

            textMonthName = itemView.findViewById(R.id.textMonthName);
            textMonthTotal = itemView.findViewById(R.id.textMonthTotal);
        }
    }
}