package com.tamaq.courier.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateHelper {

    public static final String SERVER_DATE_WITHOUT_TIME_FORMAT = "yyyy-MM-dd";
    public static final String SERVER_TIME_FORMAT = "HH:mm:ss";
    public static final String PAYMENT_DATE_FORMAT = "dd.MM.yy, HH:mm";
    public static final String CHART_DATE_FORMAT = "dd.MM.yy";
    public static final String SERVER_DATA_FORMAT_DAY_ACCURACY = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String NOTIFICATION_FORMAT = "dd MMMM, HH:mm";
    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SERVER_TIME_ZONE = "Etc/GMT-0";
    private static final Locale LOCALE_RU = new Locale("ru");
    private static int sDifferenceInTimeWithServer = 0;


    public static void setDiffWithServer(String serverTime) {
        Date date = parseDateFromString(serverTime);
        Calendar calendar = Calendar.getInstance();

        int currentHours = calendar.get(Calendar.HOUR_OF_DAY);

        calendar.setTime(date);
        int serverHours = calendar.get(Calendar.HOUR_OF_DAY);

        sDifferenceInTimeWithServer = currentHours - serverHours;
    }

    public static Date parseDateFromString(String dateString) {
        try {
            DateFormat format = new SimpleDateFormat(DateHelper.SERVER_DATE_FORMAT, Locale.ENGLISH);
            return format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String correctDateWithDifference(String serverTime) {
        Date date = parseDateFromString(serverTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, sDifferenceInTimeWithServer);
        return getStringFromDate(calendar.getTime(), SERVER_DATE_FORMAT);
    }

    public static String getStringFromDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, LOCALE_RU);
        return dateFormat.format(date);
    }

    public static String getCurrentDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SERVER_DATE_FORMAT, LOCALE_RU);
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static Date getCurrentDate() {
        return Calendar.getInstance().getTime();
    }

    public static Date getCurrentDateMinusHours(int hours) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -hours);
        return cal.getTime();
    }

    public static String getStringMinutesAndSeconds(long timeInMillis) {
        long timeInSec = timeInMillis / 1000;
        long minutes = (timeInSec % 3600) / 60;
        long seconds = timeInSec % 60;

        return String.format(LOCALE_RU, "%02d : %02d", minutes, seconds);
    }

    public static String getDateForPayment(String dateString) {
        return getStringFromDate(parseDateFromString(dateString), CHART_DATE_FORMAT);
    }

    public static void setCalendarDayMinimum(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
    }

    public static void setCalendarDayMaximum(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
    }

    public static String getStartDateOfWeek(String dateString) {
        Date date = parseDateFromString(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.clear();
        resultCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        resultCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        resultCalendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR));

        return getStringFromDate(resultCalendar.getTime(), CHART_DATE_FORMAT);
    }

    public static String getEndDateOfWeek(String dateString) {
        Date date = parseDateFromString(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar resultCalendar = Calendar.getInstance();
        resultCalendar.clear();

        resultCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        resultCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        resultCalendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR));

        int daysInWeek = 6; // current day plus 6 more

        int firstDayInWeek = resultCalendar.get(Calendar.DAY_OF_MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonthLength = resultCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if ((currentDayOfMonth + 6) > currentMonthLength) {  // if week has less days then 6 until end of month
            daysInWeek = currentMonthLength - firstDayInWeek; // then subtract for receive days count until end of week
        }

        resultCalendar.add(Calendar.DAY_OF_YEAR, daysInWeek);
        return getStringFromDate(resultCalendar.getTime(), CHART_DATE_FORMAT);
    }

    public static int differenceBetweenInMinutes(Date start, Date end) {
        long duration = end.getTime() - start.getTime();
        return (int) TimeUnit.MILLISECONDS.toMinutes(duration);
    }

    public static int differenceBetweenInHours(Date start, Date end) {
        long duration = end.getTime() - start.getTime();
        return (int) TimeUnit.MILLISECONDS.toHours(duration);
    }

    public static String getStringFromDateLocalToServerTimeZone(Date date, String format) {
        SimpleDateFormat f = new SimpleDateFormat(format);
        f.setTimeZone(TimeZone.getTimeZone(SERVER_TIME_ZONE));
        return f.format(date);
    }

    public static String convertStringDateFromServerToLocalTimeZone(String dateString) {
        Date date = DateHelper.getDateFromStringServerToLocalTimeZone(dateString);
        return getStringFromDate(date, SERVER_DATE_FORMAT);
    }

    public static Date getDateFromStringServerToLocalTimeZone(String dateString) {
        return getDateFromStringServerToLocalTimeZone(dateString, SERVER_DATE_FORMAT);
    }

    /**
     * converts the date from the server to the local time zone
     */
    public static Date getDateFromStringServerToLocalTimeZone(String dateString, String format) {
        try {
            SimpleDateFormat f = new SimpleDateFormat(format);
            f.setTimeZone(TimeZone.getTimeZone(SERVER_TIME_ZONE));
            return f.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Locale.getDefault();
        return null;
    }
}
