package com.nativenote.bluetoothcommunication.model;

/**
 * Created by imtiaz on 3/20/18.
 */

public class BluetoothItem {
    private String name;

    public BluetoothItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
