package com.tamaq.courier.presenters.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.orders.map_route.MapRouteFragment;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderFragment;
import com.tamaq.courier.utils.DialogHelper;

import rx.functions.Action0;

public class MapRouteActivity extends BaseActivity {

    public static final int RESULT_ORDER_FINISHED = 12;
    private Action0 onCurrentOrderCanceledByClientListener = () -> {
        Dialog alert = DialogHelper.buildDialog(this, R.string.order_canceled,
                R.string.contact_dispatcher_if_necessary,
                R.string.ok,
                0,
                (dialog, which) -> {
                    setResult(MapRouteActivity.RESULT_ORDER_FINISHED);
                    finish();
                }, null);
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();
    };

    public static Intent newInstance(Context context, String orderId) {
        return newInstance(context, orderId, null);
    }

    public static Intent newInstance(Context context, String orderId,
                                     MapRouteFragment.TargetMarkerType targetMarkerType) {
        Intent intent = new Intent(context, MapRouteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(NewOrderFragment.ARG_ORDER_ID, orderId);
        bundle.putSerializable(MapRouteFragment.ARG_TARGET_MARKER_TYPE, targetMarkerType);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);

        MapRouteFragment.TargetMarkerType targetMarkerType = null;
        if (getIntent().getSerializableExtra(MapRouteFragment.ARG_TARGET_MARKER_TYPE) != null)
            targetMarkerType = (MapRouteFragment.TargetMarkerType)
                    getIntent().getSerializableExtra(MapRouteFragment.ARG_TARGET_MARKER_TYPE);
        replaceFragment(MapRouteFragment.newInstance(
                getIntent().getStringExtra(MapRouteFragment.ARG_ORDER_ID),
                targetMarkerType));

        OrderDAO.getInstance().addOrderCanceledFromClientListener(
                onCurrentOrderCanceledByClientListener);
    }

    @Override
    protected void onDestroy() {
        OrderDAO.getInstance().removeOrderCanceledFromClientListener(
                onCurrentOrderCanceledByClientListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
