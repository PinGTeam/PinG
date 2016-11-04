package com.example.jorge.pingv2;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Jorge on 11/3/2016.
 */

public abstract class OnInfoWindowElemTouchListener implements View.OnTouchListener {
    private final View view;
    private final Drawable bgDrawableNormal;
    private final Drawable bgDrawablePressed;
    private final Handler handler = new Handler();

    private Marker marker;
    private boolean pressed = false;

    public GoogleMap.OnInfoWindowClickListener(View view, Drawable bgDrawableNormal, Drawable bgDrawablePressed) {
        this.view = view;
    }
}
