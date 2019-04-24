package com.tamaq.courier.model.api.request_bodies;


import com.tamaq.courier.utils.DateHelper;

import java.util.Date;

public class ReadNotificationBody {

    String time;

    public static ReadNotificationBody fromDate(Date date) {
        ReadNotificationBody body = new ReadNotificationBody();
        body.time = DateHelper.getStringFromDateLocalToServerTimeZone(date, DateHelper.SERVER_DATE_FORMAT);
        return body;
    }

}
