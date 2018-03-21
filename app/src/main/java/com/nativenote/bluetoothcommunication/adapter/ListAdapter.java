package com.nativenote.bluetoothcommunication.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nativenote.bluetoothcommunication.R;
import com.nativenote.bluetoothcommunication.databinding.ItemLayoutBinding;
import com.nativenote.bluetoothcommunication.model.BluetoothItem;

import java.util.List;

/**
 * Created by imtiaz on 9/12/17.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.StoreViewHolder> {

    private List<BluetoothItem> storeModels;
    private ItemClickCallback callback;

    public ListAdapter(List<BluetoothItem> storeModels) {
        this.storeModels = storeModels;
    }

    public void setCallback(ItemClickCallback callback) {
        this.callback = callback;
    }

    @Override
    public StoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemLayoutBinding itemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_layout, parent, false);
        itemBinding.setCallback(callback);
        return new StoreViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(StoreViewHolder holder, int position) {
        holder.bindStore(storeModels.get(position));
        holder.itemBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return storeModels.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setStores(List<BluetoothItem> storeModels) {
        this.storeModels.clear();
        this.storeModels = storeModels;
        notifyDataSetChanged();
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {

        ItemLayoutBinding itemBinding;

        public StoreViewHolder(ItemLayoutBinding itemBinding) {
            super(itemBinding.cardView);
            this.itemBinding = itemBinding;
        }

        void bindStore(BluetoothItem storeModel) {
            itemBinding.setItemModel(storeModel);
        }
    }
    
    public interface ItemClickCallback {
        void onClick(BluetoothItem model);
    }
}
