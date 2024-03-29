package com.example.zooseeker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Adapter for RecyclerView to interact with the database of locations
 */
public class LocationsListAdapter extends RecyclerView.Adapter<LocationsListAdapter.ViewHolder>{
    private List<LocationsListItem> locationItems = Collections.emptyList();
    private Consumer<LocationsListItem> onDeleteClicked;

    private OnAdapterItemClickListener onAdapterItemClickListener = null;

    public LocationsListAdapter(OnAdapterItemClickListener listener) {
        this.onAdapterItemClickListener = listener;
    }

    public void setLocationsListItems(List<LocationsListItem> newLocationsItems) {
        this.locationItems.clear();
        this.locationItems = newLocationsItems;
        notifyDataSetChanged();
    }

    public void setOnDeleteClickedHandler(Consumer<LocationsListItem> onDeleteClicked) {
        this.onDeleteClicked = onDeleteClicked;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.locations_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setLocationItem(locationItems.get(position) );
    }

    @Override
    public int getItemCount() {
        return locationItems.size();
    }

    @Override
    public long getItemId(int position) {
        return locationItems.get(position).id;
    }

    /**
     * ViewHolder for entries in the locations list
     */
    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView;
        private final TextView distanceView;
        private LocationsListItem locationItem;
        private final TextView delete;
        private final Button planningBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.location_item_text);
            this.distanceView = itemView.findViewById(R.id.distance_text);
            this.delete = itemView.findViewById(R.id.delete_btn);
            this.planningBtn = itemView.findViewById(R.id.view_list_btn);

            setTextSize(25);

            this.delete.setOnClickListener(view -> {
                if (onDeleteClicked == null) return;
                onDeleteClicked.accept(locationItem);
                onAdapterItemClickListener.onAdapterItemClickListener();
            });
        }

        public void setTextSize(int size) {
            this.textView.setTextSize(size);
            this.distanceView.setTextSize(size);
        }

        public LocationsListItem getLocationItem() {return locationItem; }
        public double getDistance() {return locationItem.distance; }

        public void setLocationItem(LocationsListItem locationItem) {
            this.locationItem = locationItem;
            this.textView.setText(locationItem.text);
            this.distanceView.setText(String.format(Locale.getDefault(), "%.1f", locationItem.distance));
        }
    }

}
