package com.tamaq.courier.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import rx.functions.Action0;

public class HelperCommon {

    public static final String USERNAME_PATTERN = "^[a-zA-Z_0-9+.@]{1,30}$";
    public static final String VIN_PATTERN = "^[a-zA-Z0-9]{0,30}$";
    public static final String WITHOUT_DIGITS_PATTERN = "^[\\D*]{3,15}$";
    public static final String WITHOUT_DIGITS_PATTERN_LONG = "^[\\D*]{3,60}$";
    public static final String CONTAINS_WHITE_SPACE_PATTERN = "\\s";
    public static final String USER_FIRST_NAME_PATTERN = "^[-\\p{L}]{2,}";

    public static double getInchCount(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float widthDpi = metrics.xdpi;
        float heightDpi = metrics.ydpi;
        float widthInches = widthPixels / widthDpi;
        float heightInches = heightPixels / heightDpi;
        double diagonalInches = Math.sqrt(
                (widthInches * widthInches)
                        + (heightInches * heightInches));
        return diagonalInches;
    }

    public static double getInchCount2(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        double screenInches = Math.sqrt(x + y);
        Log.d("debug", "Screen inches : " + screenInches);
        return screenInches;
    }

    public static SpannableString getHighlightedStringInSearch(String searchQuery, String original, Object span) {
        final int startIndex = indexOfSearchQuery(searchQuery, original);

        if (startIndex == -1) {
            return new SpannableString(original);
        } else {
            final SpannableString highlightedString = new SpannableString(original);

            // Sets the span to start at the starting point of the match and end at "length"
            // characters beyond the starting point
            highlightedString.setSpan(span, startIndex,
                    startIndex + searchQuery.length(), 0);
            return highlightedString;
        }
    }

    private static int indexOfSearchQuery(String searchQuery, String original) {
        if (!TextUtils.isEmpty(searchQuery)) {
            return original.toLowerCase(Locale.getDefault()).indexOf(
                    searchQuery.toLowerCase(Locale.getDefault()));
        }
        return -1;
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target)
                && Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
                + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
                + "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,25}" + ")+").matcher(target).matches();
    }

    public static boolean isValidUsername(CharSequence username) {
        if (TextUtils.isEmpty(username))
            return false;
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        return pattern.matcher(username).matches();
    }

    public static boolean isValidVIN(CharSequence username) {
        if (TextUtils.isEmpty(username))
            return false;
        Pattern pattern = Pattern.compile(VIN_PATTERN);
        return pattern.matcher(username).matches();
    }

    public static boolean isValidFirstName(CharSequence username) {
        if (TextUtils.isEmpty(username))
            return false;

        Pattern pattern = Pattern.compile(USER_FIRST_NAME_PATTERN);
        return pattern.matcher(username).matches();
    }

    public static boolean isWithoutDigits(CharSequence username) {
        if (TextUtils.isEmpty(username))
            return false;

        Pattern pattern = Pattern.compile(WITHOUT_DIGITS_PATTERN);
        return pattern.matcher(username).matches();
    }

    public static boolean isWithoutDigitsLong(CharSequence username) {
        if (TextUtils.isEmpty(username))
            return false;

        Pattern pattern = Pattern.compile(WITHOUT_DIGITS_PATTERN_LONG);
        return pattern.matcher(username).matches();
    }

    public static boolean isValidPassword(CharSequence target) {
        if (TextUtils.isEmpty(target))
            return false;
        if (containsWhiteSpace(target))
            return false;
//        return target.length() >= 6;
        return true;
    }

    public static boolean containsWhiteSpace(CharSequence target) {
        if (TextUtils.isEmpty(target))
            return false;
        Pattern pattern = Pattern.compile(CONTAINS_WHITE_SPACE_PATTERN);
        return pattern.matcher(target).find();
    }

    public static Bitmap overlayBitmap(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);

        int width = bmp1.getWidth();
        int height = bmp1.getHeight();
        float centerX = (width - bmp2.getWidth()) * 0.5f;
        float centerY = (height - bmp2.getHeight()) * 0.5f;

        canvas.drawBitmap(bmp2, centerX, centerY, null);
        return bmOverlay;
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideDialogKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void showKeyboardForsed(Context context, View v) {
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void showKeyboard(Context context, View v) {
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static String formatThousandsSeparator(long value) {
        return formatThousandsSeparator((int) value);
    }

    public static String formatThousandsSeparator(int value) {
        return String.format(Locale.ENGLISH, "%,d", value).replace(",", " ");
    }

    /**
     * x and y - local parent coordinates
     */
    public static List<View> getViewsByPositionInParent(int x, int y, ViewGroup viewGroup) {
        ArrayList<View> views = new ArrayList<>();
        for (int numChildren = viewGroup.getChildCount(); numChildren > 0; --numChildren) {
            View child = viewGroup.getChildAt(numChildren);
            Rect bounds = new Rect();
            if (child == null)
                continue;
            child.getHitRect(bounds);
            if (bounds.contains(x, y))
                views.add(child);
        }
        return views;
    }

    public static Point convertPoint(Point fromPoint, View fromView, View toView) {
        int[] fromCoord = new int[2];
        int[] toCoord = new int[2];
        fromView.getLocationOnScreen(fromCoord);
        toView.getLocationOnScreen(toCoord);

        Point toPoint = new Point(fromCoord[0] - toCoord[0] + fromPoint.x,
                fromCoord[1] - toCoord[1] + fromPoint.y);

        return toPoint;
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getDeviceIMEI(Context context) {
        String identifier = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null)
            identifier = tm.getDeviceId();
        if (identifier == null || identifier.length() == 0)
            identifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return identifier;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnected();
        return isConnected;
    }

    public static String printFingerprintForFacebook(Context context) {
        try {

            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.DEFAULT);
                Log.d("KeyHash:", keyHash);
                return keyHash;
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("name not found", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        }
        return null;
    }

    public static boolean inputErrorNullOrEmpty(TextInputLayout inputLayout) {
        return inputLayout.getError() == null || inputLayout.getError().toString().isEmpty();
    }

    public static String niceFormatDistance(float distance) {
        DecimalFormat dFormat;
        String postfix;
        if (distance < 1000) {
            dFormat = new DecimalFormat("###");
            postfix = " м";
        } else {
            distance /= 1000;
            dFormat = new DecimalFormat("#.#");
            postfix = " км";
        }

        return dFormat.format(distance) + postfix;
    }

    public static String niceFormatDistanceShort(float distance) {
        DecimalFormat dFormat;
        String postfix;
        String formatString;
        if (distance < 1000) {
            formatString = "#";
            postfix = " м";
        } else {
            if (distance < 10000)
                formatString = "#.#";
            else
                formatString = "#";
            distance /= 1000;
            postfix = " км";
        }
        dFormat = new DecimalFormat(formatString);

        return dFormat.format(distance) + postfix;
    }

    public static String uppercaseFirstLetter(String s) {
        if (s.length() == 1)
            return Character.toString(s.charAt(0)).toUpperCase();
        return Character.toString(s.charAt(0)).toUpperCase() + s.substring(1);
    }

    public static BitSet convertToBitSet(long value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    public static long convertFromBitSet(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    public static String spawSlashesToHttp(String s) {
        if (s.startsWith("//"))
            return s.replace("//", "http://");
        return s;
    }

    public static String removeNonDigitChars(@NonNull String s) {
        return s.replaceAll("[^0-9]", "");
    }

    public static int parseBalanceToInt(String balance) {
        if (balance == null)
            return 0;
        String stringPrice = balance.replaceAll("[^0-9,]", "").replaceAll(",", ".");
        return Double.valueOf(stringPrice).intValue();
    }

    public static boolean isOnlyChars(String text) {
        return text.matches("[a-zA-Z]+");
    }

    public static boolean checkNotNull(Object object) {
        return object != null;
    }

    public static boolean checkNotZero(int object) {
        return object != 0;
    }

    public static boolean isLocationEnabledByMax(Context context) {
        if (hasGpsFeature(context))
            return isGpsLocationEnabled(context) && isNetworkLocationEnabled(context);
        return isNetworkLocationEnabled(context);
    }

    public static boolean hasGpsFeature(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    public static boolean isGpsLocationEnabled(Context context) {
        final LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isNetworkLocationEnabled(Context context) {
        final LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static int getIntSafe(Integer integer) {
        return integer == null ? 0 : integer;
    }

    public static void grantedCallPhonePermission(Activity activity, Action0 onGranted) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        if (rxPermissions.isGranted(Manifest.permission.CALL_PHONE)) {
            onGranted.call();
            return;
        }
        rxPermissions.request(Manifest.permission.CALL_PHONE)
                .subscribe(isGranted -> {
                    if (isGranted)
                        onGranted.call();
                });
    }
}
