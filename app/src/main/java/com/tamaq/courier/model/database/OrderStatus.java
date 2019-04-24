package com.tamaq.courier.model.database;

import android.content.Context;

import com.tamaq.courier.R;

public enum OrderStatus {

    CONFIRMED("Confirmed"),
    PUBLISHED("Published"),
    CANCEL_EXECUTOR("CancelExecutor"),
    CANCEL_CUSTOMER("CancelCustomer"),
    OVERDUE("Overdue"),
    DELETED("Deleted"),

    DRAFT("Draft"),
    FIND_EXECUTOR("FindExecutor"),
    EXECUTOR_SELECTED("ExecutorSelected"),
    GOING_TO_RESTAURANT("GoingToProvider"),
    ARRIVED_AT_RESTAURANT("ArrivedToProvider"),
    GOING_TO_CUSTOMER("GoingToCustomer"),
    ARRIVED_TO_CUSTOMER("ArrivedToCustomer"),
    COMPLETE("Done");

    private String mValue;

    OrderStatus(String value) {
        mValue = value;
    }

    public static OrderStatus fromString(String value) {
        switch (value) {
            case "Draft":
                return DRAFT;
            case "FindExecutor":
                return FIND_EXECUTOR;
            case "ExecutorSelected":
                return EXECUTOR_SELECTED;
            case "GoingToProvider":
                return GOING_TO_RESTAURANT;
            case "ArrivedToProvider":
                return ARRIVED_AT_RESTAURANT;
            case "GoingToCustomer":
                return GOING_TO_CUSTOMER;
            case "ArrivedToCustomer":
                return ARRIVED_TO_CUSTOMER;
            case "Confirmed":
                return CONFIRMED;
            case "Done":
                return COMPLETE;
            case "CancelExecutor":
                return CANCEL_EXECUTOR;
            case "CancelCustomer":
                return CANCEL_CUSTOMER;
            case "Overdue":
                return OVERDUE;
            case "Deleted":
                return DELETED;
            case "Published":
                return PUBLISHED;
            default:
                return EXECUTOR_SELECTED;
        }
    }

    public static OrderStatus getNextStatus(OrderStatus orderStatus) {
        if (orderStatus.equals(FIND_EXECUTOR))
            return GOING_TO_RESTAURANT;
        int orderStatusOrdinal = orderStatus.ordinal();
        if (orderStatusOrdinal == 0)
            orderStatusOrdinal = 1;
        else if (orderStatusOrdinal < OrderStatus.values().length - 1)
            orderStatusOrdinal++;
        else
            return null;
        return OrderStatus.values()[orderStatusOrdinal];
    }

    public static String getTitleUI(OrderStatus orderStatus, Context context) {
        switch (orderStatus) {
            case DRAFT:
                return context.getString(R.string.draft);
            case FIND_EXECUTOR:
                return context.getString(R.string.find_executor);
            case PUBLISHED:
                return context.getString(R.string.order_statistics_published);
            case EXECUTOR_SELECTED:
                return context.getString(R.string.executor_selected);
            case GOING_TO_RESTAURANT:
                return context.getString(R.string.going_to_restaurant);
            case ARRIVED_AT_RESTAURANT:
                return context.getString(R.string.arrived_at_restaurant);
            case GOING_TO_CUSTOMER:
                return context.getString(R.string.going_to_customer);
            case ARRIVED_TO_CUSTOMER:
                return context.getString(R.string.arrived_to_customer);
            case COMPLETE:
                return context.getString(R.string.order_completed_without_dot);
            case OVERDUE:
                return context.getString(R.string.order_statistics_overdue);
            case CANCEL_CUSTOMER:
            case DELETED:
                return context.getString(R.string.order_statistics_cancel_customer);
            case CANCEL_EXECUTOR:
                return context.getString(R.string.order_statistics_cancel_executor);
            case CONFIRMED:
                return context.getString(R.string.status_order_confirmed);
            default:
                return "-";
        }
    }

    public static boolean isOrderCompleted(OrderStatus orderStatus) {
        switch (orderStatus) {
            case EXECUTOR_SELECTED:
            case GOING_TO_RESTAURANT:
            case ARRIVED_AT_RESTAURANT:
            case GOING_TO_CUSTOMER:
            case ARRIVED_TO_CUSTOMER:
                return false;
            case COMPLETE:
            case OVERDUE:
            case CANCEL_CUSTOMER:
            case CANCEL_EXECUTOR:
            default:
                return true;
        }
    }

    public static boolean isActive(OrderStatus orderStatus) {
        return !(orderStatus.equals(OrderStatus.COMPLETE)
                || orderStatus.equals(OrderStatus.OVERDUE)
                || orderStatus.equals(OrderStatus.CANCEL_CUSTOMER)
                || orderStatus.equals(OrderStatus.CANCEL_EXECUTOR));
    }

    public String getValue() {
        return mValue;
    }
}
