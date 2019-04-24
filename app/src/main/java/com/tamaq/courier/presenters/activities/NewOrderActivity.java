package com.tamaq.courier.presenters.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderFragment;

public class NewOrderActivity extends BaseActivity {


    public static Intent newInstance(Context context, String orderId) {
        return newInstance(context, orderId, false);
    }

    public static Intent newInstance(Context context, String orderId, boolean alreadyAccepted) {
        Intent intent = new Intent(context, NewOrderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NewOrderFragment.ARG_ORDER_ID, orderId);
        bundle.putBoolean(NewOrderFragment.ARG_ALREADY_ACCEPTED, alreadyAccepted);
        intent.putExtras(bundle);
        return intent;
    }

    public static Intent newInstance(Context context, String orderId,
                                     int totalTimerTime, int leastTimerTime) {
        Intent intent = new Intent(context, NewOrderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NewOrderFragment.ARG_ORDER_ID, orderId);
        bundle.putBoolean(NewOrderFragment.ARG_ALREADY_ACCEPTED, false);
        bundle.putInt(NewOrderFragment.ARG_TOTAL_TIMER_TIME, totalTimerTime);
        bundle.putInt(NewOrderFragment.ARG_LEAST_TIMER_TIME, leastTimerTime);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);
        if (getIntent().hasExtra(NewOrderFragment.ARG_TOTAL_TIMER_TIME)) {
            replaceFragment(NewOrderFragment.newInstance(
                    getIntent().getStringExtra(NewOrderFragment.ARG_ORDER_ID),
                    getIntent().getIntExtra(NewOrderFragment.ARG_TOTAL_TIMER_TIME, -1),
                    getIntent().getIntExtra(NewOrderFragment.ARG_LEAST_TIMER_TIME, -1)));
        } else {
            replaceFragment(NewOrderFragment.newInstance(
                    getIntent().getStringExtra(NewOrderFragment.ARG_ORDER_ID),
                    getIntent().getBooleanExtra(NewOrderFragment.ARG_ALREADY_ACCEPTED, false)));
        }
    }

    @Override
    public void onBackPressed() {
        // Do not allow the user to leave the screen

        //super.onBackPressed();
    }
}
