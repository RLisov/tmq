package com.tamaq.courier.presenters.tabs.notifications.notification_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.events.OpenOrderTabEvent;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.presenters.activities.ConcreteChatActivity;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.presenters.tabs.notifications.NotificationType;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderFragment;
import com.tamaq.courier.utils.DateHelper;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationDetailsFragment extends BaseFragment implements NotificationDetailsContract.View {

    public static final String NOTIFICATION_ID_KEY = "notification_type_id_key";

    @Inject
    NotificationDetailsContract.Presenter presenter;

    @BindView(R.id.dateTextView)
    TextView dateTextView;

    @BindView(R.id.descriptionTextView)
    TextView descriptionTextView;

    @BindView(R.id.goToOrderButton)
    Button goToOrderButton;

    @BindView(R.id.goToChatButton)
    Button goToChatButton;


    private NotificationType mType;

    public NotificationDetailsFragment() {
    }

    public static NotificationDetailsFragment newInstance(String notificationId) {
        Bundle args = new Bundle();
        args.putString(NOTIFICATION_ID_KEY, notificationId);

        NotificationDetailsFragment fragment = new NotificationDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_notification_datails, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);

        initializeNavigationBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        setChangeToolbarColor(false);

        String notificationId = getArguments().getString(NOTIFICATION_ID_KEY);
        presenter.loadNotification(notificationId);

        return rootView;
    }

    @Override
    public void displayNotification(NotificationRealm notification, NotificationType notificationType) {
        getSupportActionBar().setTitle(notification.getTitleUI(getContext()));

        dateTextView.setText(getFormattedDate(notification.getDate()));
        descriptionTextView.setText(notification.getDescription());
    }

    @Override
    public void onOrderCompleted(String orderId, boolean orderCompleted, boolean needShowOrderButton, boolean needShowChatButton) {
        if (needShowOrderButton) {
            goToOrderButton.setVisibility(View.VISIBLE);
            RxView.clicks(goToOrderButton).subscribe(o -> {
                if (orderCompleted) replaceFragment(CompletedOrderFragment.newInstance(orderId));
                else EventBus.getDefault().post(new OpenOrderTabEvent());
            });
        }

        if (needShowChatButton) {
            goToChatButton.setVisibility(View.VISIBLE);
            RxView.clicks(goToChatButton).subscribe(o ->
                    startActivity(ConcreteChatActivity.newInstance(getContext(), orderId)));
        }
    }
    private String getFormattedDate(String date) {
        return DateHelper.getStringFromDate(
                DateHelper.parseDateFromString(date),
                DateHelper.NOTIFICATION_FORMAT);
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(appComponent)
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
    }
}
