package com.tamaq.courier.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PrefsHelper {

    public static final String PREFS_COOKIES = "prefs_cookies";
    public static final String TMP_PUSH_NOTIFICATION_TOKEN = "tmp_push_notification_token";
    private static final String SHOW_CHAT_PUSH_NOTIFICATION = "show_chat_push_notification";
    private static final String BLOCKED_USER = "blocked_user";
    private static final String CONFIRMED_USER = "confirmed_user";
    private static final String AUTHORIZED_USER = "authorized_user";
    private static final String NEED_ESTIMATE = "need_estimate";
//    private static final String RESPONDED_NEW_ORDER_ID = "RESPONDED_NEW_ORDER_ID";

    public static void saveTmpPushNotificationToken(Context context, String token) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(TMP_PUSH_NOTIFICATION_TOKEN, token);
        ed.apply();
    }

    public static SharedPreferences getCommonSharedPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static String getTmpPushNotificationToken(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getString(TMP_PUSH_NOTIFICATION_TOKEN, "");
    }

    public static void clearTmpPushNotificationToken(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(TMP_PUSH_NOTIFICATION_TOKEN, "");
        ed.apply();
    }

    @SuppressLint("ApplySharedPref")
    public static void saveChatScreenIsActive(Context context, boolean isActive) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(SHOW_CHAT_PUSH_NOTIFICATION, isActive);
        ed.commit();
    }

    public static boolean isChatScreenIsShown(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getBoolean(SHOW_CHAT_PUSH_NOTIFICATION, false);
    }

    public static void setUserBlocked(boolean userBlocked, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(BLOCKED_USER, userBlocked);
        ed.apply();
    }

    public static boolean isUserBlocked(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getBoolean(BLOCKED_USER, false);
    }

    public static void setUserConfirmed(boolean userConfirmed, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(CONFIRMED_USER, userConfirmed);
        ed.apply();
    }

    public static boolean isUserConfirmed(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getBoolean(CONFIRMED_USER, false);
    }

    public static void setUserAuthorized(boolean userAuthorized, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(AUTHORIZED_USER, userAuthorized);
        ed.apply();
    }

    public static boolean isUserAuthorized(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getBoolean(AUTHORIZED_USER, false);
    }

    public static void setNeedEstimateLastOrder(boolean needEstimate, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(NEED_ESTIMATE, needEstimate);
        ed.apply();
    }

    public static boolean isNeedEstimateLastOrder(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getBoolean(NEED_ESTIMATE, false);
    }

    public static void setTimerActive(boolean timerActive, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean("TimerActive", timerActive);
        ed.apply();
    }

    public static boolean isTimerActive(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getBoolean("TimerActive", false);
    }

    public static void setLastTimerValue(int lastValue, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt("LastTimerValue", lastValue);
        ed.apply();
    }

    public static int getLastTimerValue(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getInt("LastTimerValue", 0);
    }

    public static void setLastMilliseconds(long lastValue, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putLong("LastMilliseconds", lastValue);
        ed.apply();
    }

    public static long getLastMilliseconds(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getLong("LastMilliseconds", 0);
    }

    public static void setLastWorkType(String workType, Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString("LastWorkType", workType);
        ed.apply();
    }

    public static String getLastWorkType(Context context) {
        SharedPreferences sharedPreferences = getCommonSharedPref(context);
        return sharedPreferences.getString("LastWorkType", "");
    }


//    public static void setRespondedNewOrderId(Context context, String orderId){
//        SharedPreferences sharedPreferences = getCommonSharedPref(context);
//        SharedPreferences.Editor ed = sharedPreferences.edit();
//        ed.putString(RESPONDED_NEW_ORDER_ID, orderId);
//        ed.commit();
//    }

}
