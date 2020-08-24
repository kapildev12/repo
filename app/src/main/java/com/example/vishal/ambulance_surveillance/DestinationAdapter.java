package com.example.vishal.ambulance_surveillance;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder> {

    @NonNull
    private final List<Destination> destinationList;

    @NonNull
    private final DestinationListener listener;

    public DestinationAdapter(@NonNull List<Destination> destinationList,
                              @NonNull DestinationListener listener) {
        this.destinationList = destinationList;
        this.listener = listener;
    }

    @Override
    public DestinationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DestinationViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_destination, parent, false));
    }

    @Override
    public void onBindViewHolder(DestinationViewHolder holder, int position) {
        holder.onBind(position, destinationList, listener);
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    public interface DestinationListener {

        void onSelectDestination(@NonNull Destination destination);
    }

    static class DestinationViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView tvName;
        private final AppCompatTextView tvAddress;

        public DestinationViewHolder(View itemView) {
            super(itemView);
            tvName = (AppCompatTextView) itemView.findViewById(R.id.tvName);
            tvAddress = (AppCompatTextView) itemView.findViewById(R.id.tvAddress);
        }

        void onBind(int position, List<Destination> items, DestinationListener listener) {
            Destination item = items.get(position);
            tvName.setText(item.getName());
            tvAddress.setText(item.getAddress());
            itemView.setOnClickListener(v -> listener.onSelectDestination(item));
        }
    }
}
