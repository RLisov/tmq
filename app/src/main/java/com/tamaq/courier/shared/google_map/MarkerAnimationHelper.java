package com.tamaq.courier.shared.google_map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Property;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;


public class MarkerAnimationHelper {

    private static final String TAG = MarkerAnimationHelper.class.getSimpleName();
    boolean isStarted;
    boolean isRotating;
    float lastTargetRotation;
    Handler handler = new Handler();
    RotationRunnable runnable;
    ObjectAnimator animator;
    boolean finishedMove;
    private Marker marker;
    private List<MarkerAnimationInfo> pointsList = new ArrayList<>();

    public MarkerAnimationHelper(Marker marker) {
        this.marker = marker;
    }

    public void startIfNotStarted() {
        if (isStarted)
            return;
        if (pointsList.isEmpty())
            return;
        isStarted = true;
        marker.setRotation(pointsList.get(0).toAngle);
        animateMarker(pointsList.get(0), new Action0() {
            @Override
            public void call() {
                if (pointsList.size() > 1)
                    pointsList.remove(0);
                else {
                    isStarted = false;
                    return;
                }
                animateMarker(pointsList.get(0), this);
                rotateMarker(pointsList.get(0));
            }
        });
    }

    private void animateMarker(MarkerAnimationInfo markerAnimationInfo,
                               Action0 onCompleteAction) {
        finishedMove = false;
        TypeEvaluator<LatLng> typeEvaluator = new LatLngInterpolator.LinearFixed()::interpolate;
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, markerAnimationInfo.toPosition);
        animator.setDuration(markerAnimationInfo.duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!finishedMove)
                    onCompleteAction.call();
            }
        });
        animator.start();
    }

    public void rotateMarker(MarkerAnimationInfo markerAnimationInfo) {
        Log.d(TAG, "toRotation(1): " + markerAnimationInfo.toAngle);
        if (isRotating) {
            handler.removeCallbacks(runnable);
            marker.setRotation(markerAnimationInfo.toAngle);
            isRotating = false;
            lastTargetRotation = markerAnimationInfo.toAngle;
        }
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();
        lastTargetRotation = markerAnimationInfo.toAngle;
        runnable = new RotationRunnable(start, duration, interpolator, marker, startRotation, markerAnimationInfo.toAngle);
        handler.post(runnable);
        isRotating = true;
    }

    public void addPoints(List<MarkerAnimationInfo> list) {
        pointsList.addAll(list);
    }

    private class RotationRunnable implements Runnable {

        long start;
        long duration;
        Interpolator interpolator;
        Marker marker;
        float startRotation;
        float toRotation;

        public RotationRunnable(long start, long duration, Interpolator interpolator, Marker marker, float startRotation, float toRotation) {
            this.start = start;
            this.duration = duration;
            this.interpolator = interpolator;
            this.marker = marker;
            this.startRotation = startRotation;
            this.toRotation = toRotation;
        }

        @Override
        public void run() {
            long elapsed = SystemClock.uptimeMillis() - start;
            float interpolationStep = (float) elapsed / duration;
            float t = interpolator.getInterpolation(interpolationStep);

            float rot = t * toRotation + (1 - t) * startRotation;
            float result = -rot > 180 ? rot / 2 : rot;
            if (t >= 1) {
                marker.setRotation(toRotation);
                handler.removeCallbacks(this);
                isRotating = false;
                return;
            }
            marker.setRotation(result);
            if (t < 1.0) {
                // Post again 16ms later.
                handler.postDelayed(this, 10);
            }
        }
    }
}
