package com.mycompany.createtemporarycontact.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mycompany.createtemporarycontact.R;
import com.mycompany.createtemporarycontact.model.Logs;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {
    List<Logs> mLogs;
    ClickListener clickListener;

    public LogsAdapter(List<Logs> mLogs, ClickListener clickListener) {
        this.mLogs = mLogs;
        this.clickListener = clickListener;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.logs_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Logs logs = mLogs.get(position);
        switch (logs.getCallType()) {
            case "OUTGOING":
                holder.itemView.setBackgroundColor(Color.parseColor("#B992EF"));
                break;
            case "INCOMING":
                holder.itemView.setBackgroundColor(Color.parseColor("#b0ddf2"));
                break;
            case "MISSED":
                holder.itemView.setBackgroundColor(Color.parseColor("#F66263"));
                break;
        }
        holder.number.setText(logs.getNumber());
        holder.date.setText(logs.getDatetime());
        holder.type.setText(logs.getCallType());
        holder.itemView.setOnClickListener(view -> {
            clickListener.onClick(position);
        });

    }

    @Override
    public int getItemCount() {
        return mLogs.size();
    }

    public interface ClickListener {
        void onClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView number, date, type;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            date = itemView.findViewById(R.id.time);
            type = itemView.findViewById(R.id.type);
        }
    }
}
