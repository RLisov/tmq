package com.tamaq.courier.presenters.tabs.payments.payments_for_period;

import android.annotation.SuppressLint;
import android.content.Context;

import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.utils.HelperCommon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.tamaq.courier.utils.DateHelper.CHART_DATE_FORMAT;
import static com.tamaq.courier.utils.DateHelper.getEndDateOfWeek;
import static com.tamaq.courier.utils.DateHelper.getStartDateOfWeek;
import static com.tamaq.courier.utils.DateHelper.getStringFromDate;
import static com.tamaq.courier.utils.DateHelper.parseDateFromString;

public class PaymentParser {

//    private static DateFormatSymbols myDateFormat = new DateFormatSymbols() {
//
//        @Override
//        public String[] getMonths() {
//            return new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
//                    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
//        }
//
//    };

    private static String getMonthName(int monthIndex) {
        switch (monthIndex) {
            case 0:
                return "Январь";
            case 1:
                return "Февраль";
            case 2:
                return "Март";
            case 3:
                return "Апрель";
            case 4:
                return "Май";
            case 5:
                return "Июнь";
            case 6:
                return "Июль";
            case 7:
                return "Август";
            case 8:
                return "Сентябрь";
            case 9:
                return "Октябрь";
            case 10:
                return "Ноябрь";
            case 11:
            default:
                return "Декабрь";
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static List<PaymentSortedRealm> prepareDataForCalendarPeriod(List<PaymentRealm> payments,
                                                                        int calendarPeriod,
                                                                        PaymentPeriod paymentPeriod) {
        List<PaymentSortedRealm> sortedPayments = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        for (int i = 0; i < payments.size() - 1; i++) {
            List<PaymentRealm> paymentList = new ArrayList<>();
            PaymentRealm firstPayment = payments.get(i);
            paymentList.add(firstPayment);
            Date firstDate = parseDateFromString(firstPayment.getFullDate());
            calendar.setTime(firstDate);
            int timeGap = calendarPeriod != Calendar.WEEK_OF_YEAR
                    ? calendar.get(calendarPeriod)
                    : calendar.get(Calendar.WEEK_OF_YEAR);
            String firstDateString = String.valueOf(calendar.get(calendarPeriod));
            double resultSum = firstPayment.getMoney();

            String resultDateString = firstDateString;
            String monthShortString = "";

            if (calendarPeriod == Calendar.MONTH) {
                resultDateString = getMonthName(calendar.get(Calendar.MONTH));

//                resultDateString = new SimpleDateFormat("MMM", myDateFormat).format(calendar.getTime()); // on some devices month has not correct display name
//                resultDateString = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());

//                resultDateString = Locale.getDefault().getLanguage().equals("ru")
//                        ? new SimpleDateFormat("MMMM", myDateFormat).format(calendar.getTime())
//                        : calendar.getDisplayName(calendarPeriod, Calendar.MONTH, new Locale("ru"));
                resultDateString = HelperCommon.capitalizeFirstLetter(resultDateString);
                monthShortString = resultDateString.substring(0, 3);
                resultDateString = String.format("%s %s", resultDateString, calendar.get(Calendar.YEAR));
            }


            for (int j = i + 1; j < payments.size(); j++) {
                PaymentRealm secondPayment = payments.get(j);
                Date secondDate = parseDateFromString(secondPayment.getFullDate());
                calendar.setTime(secondDate);
                String secondDateString;
                secondDateString = String.valueOf(calendar.get(calendarPeriod));

                if (firstDateString.equals(secondDateString)) {
                    resultSum += secondPayment.getMoney();

                    paymentList.add(secondPayment);

                    i = j;
                }
            }

            if (calendarPeriod == Calendar.WEEK_OF_YEAR) {
                resultDateString = String.format(new Locale("ru"), "%s - %s",
                        getStartDateOfWeek(firstPayment.getFullDate()),
                        getEndDateOfWeek(firstPayment.getFullDate())
                );
            }

            PaymentSortedRealm sortedPayment = new PaymentSortedRealm(resultDateString, resultSum, paymentPeriod, paymentList);
            sortedPayment.setTimeGap(timeGap);
            sortedPayment.setMonthShortString(monthShortString);
            sortedPayments.add(sortedPayment);
        }
        return sortedPayments;
    }

    static List<PaymentSortedRealm> prepareChartDataForAllTime(List<PaymentRealm> payments,
                                                               PaymentPeriod paymentPeriod) {
        List<PaymentSortedRealm> sortedPayments = new ArrayList<>();

        for (int i = 0; i < payments.size(); i++) {
            List<PaymentRealm> paymentList = new ArrayList<>();
            PaymentRealm firstPayment = payments.get(i);
            paymentList.add(firstPayment);
            double resultSum = firstPayment.getMoney();

            Calendar firstDate = Calendar.getInstance();
            firstDate.setTime(parseDateFromString(firstPayment.getFullDate()));

            String resultDate = getStringFromDate(firstDate.getTime(), CHART_DATE_FORMAT);

            int firstDay = firstDate.get(Calendar.DAY_OF_YEAR);
            for (int j = i + 1; j < payments.size(); j++) {
                PaymentRealm secondPayment = payments.get(j);
                Calendar secondDate = Calendar.getInstance();
                secondDate.setTime(parseDateFromString(secondPayment.getFullDate()));

                int secondDay = secondDate.get(Calendar.DAY_OF_YEAR);
                if (firstDay == secondDay) {
                    resultSum += secondPayment.getMoney();
                    i = j;
                }
            }
            sortedPayments.add(new PaymentSortedRealm(resultDate, resultSum, paymentPeriod, paymentList, true));
        }
        return sortedPayments;
    }

    public static List<PaymentSortedRealm> prepareListDataForAllTime(List<PaymentRealm> payments,
                                                                     PaymentPeriod paymentPeriod,
                                                                     Context context) {
        List<PaymentSortedRealm> sortedPayments = new ArrayList<>();

        for (int i = 0; i < payments.size(); i++) {
            List<PaymentRealm> paymentList = new ArrayList<>();
            PaymentRealm payment = payments.get(i);
            paymentList.add(payment);

            String resultDateString = String.format(
                    "%s. %s", payment.getFullDate(), payment.getTypeUI(context));
            sortedPayments.add(new PaymentSortedRealm(
                    resultDateString, payment.getMoney(), paymentPeriod, paymentList));
        }
        return sortedPayments;
    }


}
