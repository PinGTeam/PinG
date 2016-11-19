package com.example.jorge.pingv2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

/**
 * Created by Jorge on 11/14/2016.
 */


public class SelectTimeDialog extends DialogFragment{

    private LayoutInflater inflater;
    private View v;
    private int startTimeHour, endTimeHour, startTimeMinute, endTimeMinute;
    private TimePicker startPicker;

    public interface OnDialogClickListener {
        void onDialogPositiveClick(String name, String time, String desc);
    }

    OnDialogClickListener mListener;

    //TEST

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.dialog_select_time_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(v).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                startPicker = (TimePicker)v.findViewById(R.id.startTimePicker);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    startTimeHour = startPicker.getHour();
                    startTimeMinute = startPicker.getMinute();
                }
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnDialogClickListener) context;
    }
}