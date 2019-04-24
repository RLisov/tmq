package com.tamaq.courier.presenters.tabs.orders.order_cancel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderFragment;

public class OrderCancelActivity extends BaseActivity {


    public static Intent newInstance(Context context, String orderId) {
        Intent intent = new Intent(context, OrderCancelActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NewOrderFragment.ARG_ORDER_ID, orderId);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        replaceFragment(OrderCancelFragment.newInstance(
                getIntent().getStringExtra(OrderCancelFragment.ARG_ORDER_ID)));
    }

    @Override
    public void onBackPressed() {
        // Do not allow the user to leave the screen

        //super.onBackPressed();
    }
}
