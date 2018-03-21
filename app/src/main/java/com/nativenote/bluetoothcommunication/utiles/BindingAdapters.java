package com.nativenote.bluetoothcommunication.utiles;

import android.databinding.BindingAdapter;
import android.view.View;

/**
 * Created by imtiaz on 3/20/18.
 */

public class BindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}
