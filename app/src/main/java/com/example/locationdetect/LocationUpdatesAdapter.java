package com.example.locationdetect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationUpdatesAdapter extends RecyclerView.Adapter<LocationUpdatesAdapter.LocationViewHolder> {

    private List<LocationUpdateModel> locationList;

    public LocationUpdatesAdapter(List<LocationUpdateModel> locationList) {
        this.locationList = locationList;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.samplelayout, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationUpdateModel locationUpdate = locationList.get(position);

        holder.addressTextView.setText(locationUpdate.getAddress());
        holder.dateTextView.setText(locationUpdate.getDate());
        holder.timeTextView.setText(locationUpdate.getTime12hr());
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView addressTextView;
        TextView dateTextView;
        TextView timeTextView;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}
