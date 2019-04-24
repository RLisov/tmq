package com.tamaq.courier.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private WeakReference<NetworkStateChangedListener> listener;

    public interface NetworkStateChangedListener {
        void onNetworkChanged(boolean isNetworkEnabled);
    }

    public void setNetworkStateChangedListener(NetworkStateChangedListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener != null && listener.get() != null) {
            listener.get().onNetworkChanged(isNetworkAvailable(context));
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}