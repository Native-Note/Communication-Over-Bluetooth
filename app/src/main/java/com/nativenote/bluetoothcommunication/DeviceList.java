package com.nativenote.bluetoothcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;


import com.nativenote.bluetoothcommunication.adapter.ActionClickCallback;
import com.nativenote.bluetoothcommunication.adapter.ListAdapter;
import com.nativenote.bluetoothcommunication.model.BluetoothItem;

import com.nativenote.bluetoothcommunication.R;
import com.nativenote.bluetoothcommunication.databinding.ActivityDeviceListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceList extends AppCompatActivity {


    private List<BluetoothItem> pairedDeviceList = new ArrayList<>();
    private List<BluetoothItem> newDeviceList = new ArrayList<>();
    private ListAdapter pairedDeviceAdapter;
    private ListAdapter newDeviceAdapter;
    private ActivityDeviceListBinding binding;

    public static String DEVICE_ADDRESS = "deviceAddress";
    private BluetoothAdapter bluetoothAdapter;
    ListAdapter.ItemClickCallback callback = model -> {
        bluetoothAdapter.cancelDiscovery();

        String info = model.getName();
        String address = info.substring(info.length() - 17);

        Intent intent = new Intent();
        intent.putExtra(DEVICE_ADDRESS, address);

        setResult(Activity.RESULT_OK, intent);
        finish();
    };

    private ActionClickCallback  callbackScan = () -> {
        binding.setIsLoadingNew(false);
        startDiscovery();
    };
    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDeviceList.add(new BluetoothItem(device.getName() + "\n" + device.getAddress()));
                    newDeviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                binding.setIsLoadingNew(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_device_list);
        binding.setIsLoading(true);
        binding.setIsLoadingNew(true);
        binding.setCallback(callbackScan);

        pairedDeviceAdapter = new ListAdapter(pairedDeviceList);
        pairedDeviceAdapter.setCallback(callback);
        binding.listPairedDevice.setHasFixedSize(true);
        binding.listPairedDevice.setLayoutManager(new LinearLayoutManager(this));
        binding.listPairedDevice.setAdapter(pairedDeviceAdapter);

        newDeviceAdapter = new ListAdapter(newDeviceList);
        newDeviceAdapter.setCallback(callback);
        binding.listNewDevice.setHasFixedSize(true);
        binding.listNewDevice.setLayoutManager(new LinearLayoutManager(this));
        binding.listNewDevice.setAdapter(newDeviceAdapter);

        configurePairedList();
    }

    private void startDiscovery() {
        setProgressBarIndeterminateVisibility(true);

        binding.setIsLoadingNew(false);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    private void configurePairedList() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            binding.setIsLoading(false);
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceList.add(new BluetoothItem(device.getName() + "\n" + device.getAddress()));
            }
        }

        pairedDeviceAdapter.notifyDataSetChanged();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(discoveryFinishReceiver);
    }
}
