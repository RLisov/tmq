package com.tamaq.courier.presenters.tabs.notifications;

import android.content.Context;

import com.tamaq.courier.R;

public enum NotificationType {

    SIMPLE,
    CAN_MAKE_IT,
    YOU_SELECTED_EXECUTOR,
    YOU_NOT_SELECTED_EXECUTOR,
    CHAT,
    ORDER_PUBLISHED,
    ORDER_EXECUTOR_SELECTED,
    ORDER_EXECUTOR_TO_PROVIDER,
    ORDER_EXECUTOR_AT_PROVIDER,
    ORDER_EXECUTOR_TO_CUSTOMER,
    ORDER_EXECUTOR_AT_CUSTOMER,
    ORDER_CONFIRMED,
    ORDER_DONE,
    ORDER_EXECUTOR_CANCEL,
    ORDER_CUSTOMER_CANCEL,
    ORDER_OVERDUE,
    COMPLETED_ORDER,
    NEW_MESSAGE,
    CANCELED_ORDER;

    public static NotificationType getNotificationType(String notificationTypeString) {
        try {
            return NotificationType.valueOf(notificationTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotificationType.SIMPLE;
        }
    }

    public static boolean isOrderActive(NotificationType notificationType) {
        switch (notificationType) {
            case YOU_SELECTED_EXECUTOR:
            case ORDER_PUBLISHED:
            case ORDER_EXECUTOR_SELECTED:
            case ORDER_CONFIRMED:
                return true;
            case ORDER_DONE:
            case ORDER_EXECUTOR_CANCEL:
            case ORDER_CUSTOMER_CANCEL:
            case ORDER_OVERDUE:
            case COMPLETED_ORDER:
            case CANCELED_ORDER:
            default:
                return false;
        }
    }

    public static String getNotificationTitleUI(NotificationType notificationType, Context context) {
        switch (notificationType) {
            case SIMPLE:
                return context.getString(R.string.info);
            case CAN_MAKE_IT:
                return context.getString(R.string.can_make_it);
            case YOU_SELECTED_EXECUTOR:
                return context.getString(R.string.you_selected_executor);
            case YOU_NOT_SELECTED_EXECUTOR:
                return context.getString(R.string.you_not_selected_executor);
            case CHAT:
                return context.getString(R.string.new_chat);
            case ORDER_PUBLISHED:
                return context.getString(R.string.order_published);
            case ORDER_EXECUTOR_SELECTED:
                return context.getString(R.string.order_executor_selected);
            case ORDER_CONFIRMED:
                return context.getString(R.string.order_confirmed);
            case ORDER_DONE:
                return context.getString(R.string.order_done);
            case ORDER_EXECUTOR_CANCEL:
                return context.getString(R.string.order_executor_cancel);
            case ORDER_CUSTOMER_CANCEL:
                return context.getString(R.string.order_customer_cancel);
            case ORDER_OVERDUE:
                return context.getString(R.string.order_overdue);
            case COMPLETED_ORDER:
                return context.getString(R.string.order_completed_without_dot);
            case NEW_MESSAGE:
                return context.getString(R.string.notification_new_message_description);
            case CANCELED_ORDER:
                return context.getString(R.string.order_canceled);
            default:
                return context.getString(R.string.info);
        }
    }

    public String getTypeId() {
        return this.name().toLowerCase();
    }
}
