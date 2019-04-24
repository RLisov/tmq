package com.tamaq.courier.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import com.tamaq.courier.R;


public class DialogHelper {

    public static void showDialog(final Activity activity, String title, String message,
                                  @StringRes int positiveButtonText,
                                  @StringRes int negativeButtonText,
                                  DialogInterface.OnClickListener positionOnClickListener,
                                  DialogInterface.OnClickListener negativeOnClickListener) {
        AlertDialog alertDialog = buildDialog(activity, title, message, positiveButtonText,
                negativeButtonText, positionOnClickListener, negativeOnClickListener);
        if (alertDialog != null)
            alertDialog.show();
    }

    public static AlertDialog buildDialog(final Activity activity, String title, String message,
                                          @StringRes int positiveButtonText,
                                          @StringRes int negativeButtonText,
                                          DialogInterface.OnClickListener positionOnClickListener,
                                          DialogInterface.OnClickListener negativeOnClickListener) {
        try {
            AlertDialog.Builder adb = new AlertDialog.Builder(activity, R.style.AlertDialogBaseTheme);

            if (!title.isEmpty()) adb.setTitle(title);
            if (!message.isEmpty()) adb.setMessage(message);
            if (negativeButtonText != 0)
                adb.setNegativeButton(negativeButtonText, negativeOnClickListener);
            adb.setPositiveButton(positiveButtonText, positionOnClickListener);
            final AlertDialog alertDialog = adb.create();
            alertDialog.setOnShowListener(dialog -> {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(activity, R.color.dark_sky_blue));
            });
            return alertDialog;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void showDialog(final Activity activity, @StringRes int title, @StringRes int message,
                                  @StringRes int positiveButtonText,
                                  @StringRes int negativeButtonText,
                                  DialogInterface.OnClickListener positionOnClickListener,
                                  DialogInterface.OnClickListener negativeOnClickListener) {
        AlertDialog alertDialog = buildDialog(activity, title, message, positiveButtonText,
                negativeButtonText, positionOnClickListener, negativeOnClickListener);
        if (alertDialog != null) alertDialog.show();
    }

    public static AlertDialog buildDialog(final Activity activity, @StringRes int title, @StringRes int message,
                                          @StringRes int positiveButtonText,
                                          @StringRes int negativeButtonText,
                                          DialogInterface.OnClickListener positionOnClickListener,
                                          DialogInterface.OnClickListener negativeOnClickListener) {
        String titleString = title != 0 ? activity.getString(title) : "";
        String messageString = message != 0 ? activity.getString(message) : "";

        return buildDialog(activity, titleString, messageString, positiveButtonText,
                negativeButtonText, positionOnClickListener, negativeOnClickListener);
    }

    public static ProgressDialog showProgressDialog(Context context, @StringRes int resId) {
        String message = context.getString(resId);
        return showProgressDialog(context, message);
    }

    public static ProgressDialog showProgressDialog(Context context, String message) {
        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.ProgressDialogTheme);
        progressDialog.setMessage(message);
        progressDialog.show();
        return progressDialog;
    }

    public static ProgressDialog showProgressDialog(Context context, @StringRes int resId, boolean cancelable) {
        String message = context.getString(resId);
        return showProgressDialog(context, message, cancelable);
    }

    public static ProgressDialog showProgressDialog(Context context, String message, boolean cancelable) {
        final ProgressDialog progressDialog = new ProgressDialog(context, R.style.ProgressDialogTheme);
        progressDialog.setCancelable(cancelable);
        progressDialog.setMessage(message);
        progressDialog.show();
        return progressDialog;
    }

}
