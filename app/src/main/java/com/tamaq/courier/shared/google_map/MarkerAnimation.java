package com.tamaq.courier.shared.google_map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import rx.functions.Action0;

public class MarkerAnimation {
    private static final String TAG = MarkerAnimation.class.getSimpleName();

    public static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator, Action0 onCompleteAction) {
        final LatLng startPosition = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(animation -> {
            float v = animation.getAnimatedFraction();
            LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
            marker.setPosition(newPosition);
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(3000);
        valueAnimator.start();
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onCompleteAction.call();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void animateMarkerToICS(Marker marker, LatLng finalPosition,
                                          final LatLngInterpolator latLngInterpolator,
                                          long duration,
                                          Action0 onCompleteAction) {
        TypeEvaluator<LatLng> typeEvaluator = latLngInterpolator::interpolate;
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                onCompleteAction.call();
            }
        });
        animator.start();
    }

    public static void rotateMarker(final Marker marker, final float toRotation, Action0 onCompleteAction) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 100;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                Log.d(TAG, "elapsed: " + elapsed);
                float interpolationStep = (float) elapsed / duration;
                Log.d(TAG, "interpolationStep: " + interpolationStep);
                float t = interpolator.getInterpolation(interpolationStep);
                Log.d(TAG, "t: " + t);

                float rot = t * toRotation + (1 - t) * startRotation;
                Log.d(TAG, "toRotation: " + toRotation + "| startRotation: " + startRotation);
                Log.d(TAG, "rot: " + rot);
                float result = -rot > 180 ? rot / 2 : rot;
                if (t >= 1) {
                    result = toRotation;
                    marker.setRotation(result);
                    onCompleteAction.call();
                    handler.removeCallbacks(this);
                    return;
                }
                Log.d(TAG, "result: " + result);
                marker.setRotation(result);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}