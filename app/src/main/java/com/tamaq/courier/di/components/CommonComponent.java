package com.tamaq.courier.di.components;

import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.di.scopes.CommonScope;
import com.tamaq.courier.presenters.code_verification.CodeVerificationFragment;
import com.tamaq.courier.presenters.login.LoginFragment;
import com.tamaq.courier.presenters.main.MainActivity;
import com.tamaq.courier.presenters.region_select.RegionSelectFragment;
import com.tamaq.courier.presenters.registration.RegistrationFragment;
import com.tamaq.courier.presenters.registration.city.CityFragment;
import com.tamaq.courier.presenters.registration.country.CountryFragment;
import com.tamaq.courier.presenters.registration.identifictaion.IdentificationFragment;
import com.tamaq.courier.presenters.registration.terms_of_use.TermsOfUseFragment;
import com.tamaq.courier.presenters.splash.SplashScreenFragment;
import com.tamaq.courier.presenters.tabs.chat.ChatTabFragment;
import com.tamaq.courier.presenters.tabs.chat.concrete_chat.ConcreteChatFragment;
import com.tamaq.courier.presenters.tabs.notifications.NotificationsFragment;
import com.tamaq.courier.presenters.tabs.notifications.notification_details.NotificationDetailsFragment;
import com.tamaq.courier.presenters.tabs.orders.OrdersTabFragment;
import com.tamaq.courier.presenters.tabs.orders.auto_rate_params.AutoRateFragment;
import com.tamaq.courier.presenters.tabs.orders.current_order.CurrentOrderFragment;
import com.tamaq.courier.presenters.tabs.orders.empty_state.OrdersFragment;
import com.tamaq.courier.presenters.tabs.orders.estimate_client.EstimateClientFragment;
import com.tamaq.courier.presenters.tabs.orders.map_route.MapRouteFragment;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderFragment;
import com.tamaq.courier.presenters.tabs.orders.order_cancel.OrderCancelFragment;
import com.tamaq.courier.presenters.tabs.payments.all_payments.PaymentsFragment;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentsForPeriodFragment;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information.PaymentInformationFragment;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list.PaymentPeriodListFragment;
import com.tamaq.courier.presenters.tabs.profile.ProfileFragment;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.OrdersArchiveFragment;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderFragment;
import com.tamaq.courier.presenters.tabs.profile.settings.ProfileSettingsFragment;
import com.tamaq.courier.presenters.tutorial.TutorialFragment;
import com.tamaq.courier.presenters.welcome.WelcomeFragment;

import dagger.Component;


@SuppressWarnings("WeakerAccess")
@CommonScope
@Component(modules = {CommonModule.class}, dependencies = AppComponent.class)
public interface CommonComponent {

    void inject(SplashScreenFragment splashScreenFragment);

    void inject(IdentificationFragment identificationFragment);

    void inject(WelcomeFragment welcomeFragment);

    void inject(RegistrationFragment registrationFragment);

    void inject(TutorialFragment tutorialFragment);

    void inject(LoginFragment loginFragment);

    void inject(CodeVerificationFragment fragment);

    void inject(OrdersFragment fragment);

    void inject(AutoRateFragment fragment);

    void inject(CountryFragment fragment);

    void inject(CityFragment fragment);

    void inject(RegionSelectFragment fragment);

    void inject(NewOrderFragment fragment);

    void inject(ChatTabFragment fragment);

    void inject(ConcreteChatFragment fragment);

    void inject(CurrentOrderFragment fragment);

    void inject(OrdersTabFragment fragment);

    void inject(OrderCancelFragment fragment);

    void inject(TermsOfUseFragment fragment);

    void inject(PaymentsFragment fragment);

    void inject(PaymentsForPeriodFragment fragment);

    void inject(MapRouteFragment fragment);

    void inject(PaymentInformationFragment fragment);

    void inject(EstimateClientFragment fragment);

    void inject(PaymentPeriodListFragment fragment);

    void inject(ProfileFragment fragment);

    void inject(NotificationsFragment fragment);

    void inject(NotificationDetailsFragment fragment);

    void inject(ProfileSettingsFragment fragment);

    void inject(OrdersArchiveFragment fragment);

    void inject(CompletedOrderFragment fragment);

    void inject(MainActivity activity);
}
