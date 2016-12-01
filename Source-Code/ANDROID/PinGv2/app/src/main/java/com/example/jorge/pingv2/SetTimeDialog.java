/**
 * Ammended and made right by Zach and Richard and Jorge Torres-Aldana*
 **/

package com.example.jorge.pingv2;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.util.Calendar;


public class SetTimeDialog extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {


    public interface SetTimeDialogListener {
        void onDialogPositiveClick(int hour, int minute);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    SetTimeDialogListener mListener;


    public static SetTimeDialog newInstance(SetTimeDialogListener mListener) {
        SetTimeDialog dialog = new SetTimeDialog();
        dialog.mListener = mListener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, false);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onDialogPositiveClick(hourOfDay, minute);
    }
}
