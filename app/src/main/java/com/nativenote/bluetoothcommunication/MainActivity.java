package com.nativenote.bluetoothcommunication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.nativenote.bluetoothcommunication.R;

import com.nativenote.bluetoothcommunication.adapter.ActionClickCallback;
import com.nativenote.bluetoothcommunication.adapter.ListAdapter;
import com.nativenote.bluetoothcommunication.databinding.ActivityMainBinding;
import com.nativenote.bluetoothcommunication.model.BluetoothItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private String connectedDeviceName = null;

    private CommunicationService communicationService = null;
    private ListAdapter adapter;
    private List<BluetoothItem> bluetoothItems = new ArrayList<>();
    private ActivityMainBinding binding;
    private ActionClickCallback callback = new ActionClickCallback() {
        @Override
        public void onClick() {
            sendMessage(binding.edtMessage.getText().toString());
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGE:
                        Log.e("XXX", "MESSAGE_STATE_CHANGE");
                    switch (message.arg1) {
                        case CommunicationService.STATE_CONNECTED:
                            bluetoothItems.clear();
                            break;
                        case CommunicationService.STATE_CONNECTING:
                            break;
                        case CommunicationService.STATE_LISTEN:
                        case CommunicationService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) message.obj;

                    String writeMessage = new String(writeBuf);
                    bluetoothItems.add(new BluetoothItem("Me:  " + writeMessage));
//                    adapter.setStores(bluetoothItems);
                    adapter.notifyDataSetChanged();
                    Log.e("XXX", "writeMessage: " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) message.obj;

                    String readMessage = new String(readBuf, 0, message.arg1);
                    bluetoothItems.add(new BluetoothItem(connectedDeviceName + ":  " + readMessage));
//                    adapter.setStores(bluetoothItems);
                    adapter.notifyDataSetChanged();
                    Log.e("XXX", connectedDeviceName +" : readMessage : " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    Log.e("XXX", "MESSAGE_DEVICE_NAME");
                    connectedDeviceName = message.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Log.e("XXX", "MESSAGE_TOAST");

                    Toast.makeText(getApplicationContext(),
                            message.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });

    private void sendMessage(String message) {
        if (communicationService.getState() != CommunicationService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            communicationService.write(send);

            outStringBuffer.setLength(0);
            binding.edtMessage.setText(outStringBuffer);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (communicationService == null)
                setupChat();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setCallback(callback);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new ListAdapter(bluetoothItems);
        binding.list.setHasFixedSize(true);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (communicationService != null && communicationService.getState() == CommunicationService.STATE_NONE) {
            communicationService.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (communicationService != null)
            communicationService.stop();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                startActivityForResult(new Intent(this, DeviceList.class), REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                startActivityForResult(new Intent(this, DeviceList.class), REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void setupChat() {
        communicationService = new CommunicationService(handler);

        outStringBuffer = new StringBuffer("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                DeviceList.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        communicationService.connect(device, secure);
    }
}
