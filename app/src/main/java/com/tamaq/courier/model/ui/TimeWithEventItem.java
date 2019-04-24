package com.tamaq.courier.model.ui;

public class TimeWithEventItem implements TimeWithEventUIItem {

    private String fullDate;
    private String date;
    private String title;

    public TimeWithEventItem(String fullDate, String date, String title) {
        this.fullDate = fullDate;
        this.date = date;
        this.title = title;
    }

    public String getFullDate() {
        return fullDate;
    }

    public void setFullDate(String fullDate) {
        this.fullDate = fullDate;
    }

    public String getEventTitleUI() {
        return getTitle();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEventTimeUI() {
        return getDate();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
