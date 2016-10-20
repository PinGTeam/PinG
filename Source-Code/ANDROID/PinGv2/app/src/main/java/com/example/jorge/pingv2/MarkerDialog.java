//Code by Jorge Torres-Aldana & Arthur Karapateas

package com.example.jorge.pingv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Jorge on 10/11/2016.
 */

public class MarkerDialog extends DialogFragment {

    LayoutInflater inflater;
    EditText name, time, description;
    View v;

    public interface OnDialogClickListener {
        void onDialogPositiveClick(String name, String time, String desc);
    }

    OnDialogClickListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.dialog_marker_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(v).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                name = (EditText) v.findViewById(R.id.eventNameInput);
                time = (EditText) v.findViewById(R.id.eventTimeInput);
                description = (EditText) v.findViewById(R.id.eventDescriptionInput);

                String eName = name.getText().toString();
                String eTime = time.getText().toString();
                String eDesc = description.getText().toString();

                //call it here
                mListener.onDialogPositiveClick(eName, eTime, eDesc);
                dismiss();
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MarkerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //check if context is
        mListener = (OnDialogClickListener) context;
    }
}