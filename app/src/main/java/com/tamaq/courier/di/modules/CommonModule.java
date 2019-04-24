package com.tamaq.courier.di.modules;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.di.scopes.CommonScope;
import com.tamaq.courier.presenters.code_verification.CodeVerificationContract;
import com.tamaq.courier.presenters.code_verification.CodeVerificationPresenter;
import com.tamaq.courier.presenters.login.LoginContract;
import com.tamaq.courier.presenters.login.LoginPresenter;
import com.tamaq.courier.presenters.main.MainContract;
import com.tamaq.courier.presenters.main.MainPresenter;
import com.tamaq.courier.presenters.region_select.RegionSelectContract;
import com.tamaq.courier.presenters.region_select.RegionSelectPresenter;
import com.tamaq.courier.presenters.registration.RegistrationContract;
import com.tamaq.courier.presenters.registration.RegistrationPresenter;
import com.tamaq.courier.presenters.registration.city.CityContract;
import com.tamaq.courier.presenters.registration.city.CityPresenter;
import com.tamaq.courier.presenters.registration.country.CountryContract;
import com.tamaq.courier.presenters.registration.country.CountryPresenter;
import com.tamaq.courier.presenters.registration.identifictaion.IdentificationContract;
import com.tamaq.courier.presenters.registration.identifictaion.IdentificationPresenter;
import com.tamaq.courier.presenters.registration.terms_of_use.TermsOfUseContract;
import com.tamaq.courier.presenters.registration.terms_of_use.TermsOfUsePresenter;
import com.tamaq.courier.presenters.splash.SplashScreenContract;
import com.tamaq.courier.presenters.splash.SplashScreenPresenter;
import com.tamaq.courier.presenters.tabs.chat.ChatTabContract;
import com.tamaq.courier.presenters.tabs.chat.ChatTabPresenter;
import com.tamaq.courier.presenters.tabs.chat.concrete_chat.ConcreteChatContract;
import com.tamaq.courier.presenters.tabs.chat.concrete_chat.ConcreteChatPresenter;
import com.tamaq.courier.presenters.tabs.notifications.NotificationsContract;
import com.tamaq.courier.presenters.tabs.notifications.NotificationsPresenter;
import com.tamaq.courier.presenters.tabs.notifications.notification_details.NotificationDetailsContract;
import com.tamaq.courier.presenters.tabs.notifications.notification_details.NotificationDetailsPresenter;
import com.tamaq.courier.presenters.tabs.orders.OrdersTabContract;
import com.tamaq.courier.presenters.tabs.orders.OrdersTabPresenter;
import com.tamaq.courier.presenters.tabs.orders.auto_rate_params.AutoRateContract;
import com.tamaq.courier.presenters.tabs.orders.auto_rate_params.AutoRatePresenter;
import com.tamaq.courier.presenters.tabs.orders.current_order.CurrentOrderContract;
import com.tamaq.courier.presenters.tabs.orders.current_order.CurrentOrderPresenter;
import com.tamaq.courier.presenters.tabs.orders.empty_state.OrdersContract;
import com.tamaq.courier.presenters.tabs.orders.empty_state.OrdersPresenter;
import com.tamaq.courier.presenters.tabs.orders.estimate_client.EstimateClientContract;
import com.tamaq.courier.presenters.tabs.orders.estimate_client.EstimateClientPresenter;
import com.tamaq.courier.presenters.tabs.orders.map_route.MapRouteContract;
import com.tamaq.courier.presenters.tabs.orders.map_route.MapRoutePresenter;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderContract;
import com.tamaq.courier.presenters.tabs.orders.new_order.NewOrderPresenter;
import com.tamaq.courier.presenters.tabs.orders.order_cancel.OrderCancelContract;
import com.tamaq.courier.presenters.tabs.orders.order_cancel.OrderCancelPresenter;
import com.tamaq.courier.presenters.tabs.payments.all_payments.PaymentsContract;
import com.tamaq.courier.presenters.tabs.payments.all_payments.PaymentsPresenter;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentsForPeriodContract;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.PaymentsForPeriodPresenter;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information.PaymentInformationContract;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payment_information.PaymentInformationPresenter;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list.PaymentPeriodListContract;
import com.tamaq.courier.presenters.tabs.payments.payments_for_period.payments_period_list.PaymentPeriodListPresenter;
import com.tamaq.courier.presenters.tabs.profile.ProfileContract;
import com.tamaq.courier.presenters.tabs.profile.ProfilePresenter;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.OrdersArchiveContract;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.OrdersArchivePresenter;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderContract;
import com.tamaq.courier.presenters.tabs.profile.orders_archive.completed_order.CompletedOrderPresenter;
import com.tamaq.courier.presenters.tabs.profile.settings.ProfileSettingsContract;
import com.tamaq.courier.presenters.tabs.profile.settings.ProfileSettingsPresenter;
import com.tamaq.courier.presenters.tutorial.TutorialContract;
import com.tamaq.courier.presenters.tutorial.TutorialPresenter;
import com.tamaq.courier.presenters.welcome.WelcomeContract;
import com.tamaq.courier.presenters.welcome.WelcomePresenter;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.NetworkChangeReceiver;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("WeakerAccess")
@Module
public class CommonModule {

    @Provides
    @CommonScope
    public SplashScreenContract.Presenter provideSplashPresenter(
            TamaqApp app, ServerCommunicator serverCommunicator, NetworkChangeReceiver receiver) {
        return new SplashScreenPresenter(app, serverCommunicator, receiver);
    }

    @Provides
    @CommonScope
    public WelcomeContract.Presenter provideWelcomePresenter(
            TamaqApp app, ServerCommunicator serverCommunicator) {
        return new WelcomePresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public RegistrationContract.Presenter provideRegistrationPresenter(
            TamaqApp app, ServerCommunicator serverCommunicator) {
        return new RegistrationPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public IdentificationContract.Presenter provideIdentificationPresenter(
            TamaqApp app, ServerCommunicator serverCommunicator) {
        return new IdentificationPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public TutorialContract.Presenter provideTutorialPresenter(
            TamaqApp app, ServerCommunicator serverCommunicator) {
        return new TutorialPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public LoginContract.Presenter provideLoginPresenter(TamaqApp app,
                                                         ServerCommunicator serverCommunicator) {
        return new LoginPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public CodeVerificationContract.Presenter provideCodeVerificationPresenter(TamaqApp app,
                                                                               ServerCommunicator serverCommunicator) {
        return new CodeVerificationPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public OrdersContract.Presenter provideOrdersPresenter(TamaqApp app,
                                                           ServerCommunicator serverCommunicator) {
        return new OrdersPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public AutoRateContract.Presenter provideAutoRatePresenter(TamaqApp app,
                                                               ServerCommunicator serverCommunicator) {
        return new AutoRatePresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public RegionSelectContract.Presenter provideRegionSelectPresenter(TamaqApp app,
                                                                       ServerCommunicator serverCommunicator) {
        return new RegionSelectPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public NewOrderContract.Presenter provideNewOrderPresenter(TamaqApp app,
                                                               ServerCommunicator serverCommunicator) {
        return new NewOrderPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public CountryContract.Presenter provideCountryPresenter(TamaqApp app,
                                                             ServerCommunicator serverCommunicator) {
        return new CountryPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public CityContract.Presenter provideCityPresenter(TamaqApp app,
                                                       ServerCommunicator serverCommunicator) {
        return new CityPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public ChatTabContract.Presenter provideChatTabPresenter(TamaqApp app,
                                                             ServerCommunicator serverCommunicator) {
        return new ChatTabPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public ConcreteChatContract.Presenter provideConcreteChatPresenter(TamaqApp app,
                                                                       ServerCommunicator serverCommunicator) {
        return new ConcreteChatPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public CurrentOrderContract.Presenter provideCurrentOrderPresenter(TamaqApp app,
                                                                       ServerCommunicator serverCommunicator) {
        return new CurrentOrderPresenter(app, serverCommunicator);
    }


    @Provides
    @CommonScope
    public OrdersTabContract.Presenter provideOrdersTabPresenter(TamaqApp app,
                                                                 ServerCommunicator serverCommunicator) {
        return new OrdersTabPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public OrderCancelContract.Presenter provideOrdersCancelPresenter(TamaqApp app,
                                                                      ServerCommunicator serverCommunicator) {
        return new OrderCancelPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public TermsOfUseContract.Presenter provideTermsOfUsePresenter(TamaqApp app,
                                                                   ServerCommunicator serverCommunicator) {
        return new TermsOfUsePresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public PaymentsContract.Presenter providePaymentsPresenter(TamaqApp app,
                                                               ServerCommunicator serverCommunicator) {
        return new PaymentsPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public PaymentsForPeriodContract.Presenter providePaymentsForPeriodPresenter(TamaqApp app,
                                                                                 ServerCommunicator serverCommunicator) {
        return new PaymentsForPeriodPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public MapRouteContract.Presenter provideMapRoutePresenter(TamaqApp app,
                                                               ServerCommunicator serverCommunicator) {
        return new MapRoutePresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public PaymentInformationContract.Presenter providePaymentInformationPresenter(TamaqApp app,
                                                                                   ServerCommunicator serverCommunicator) {
        return new PaymentInformationPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public EstimateClientContract.Presenter provideEstimateClientPresenter(TamaqApp app,
                                                                           ServerCommunicator serverCommunicator) {
        return new EstimateClientPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public PaymentPeriodListContract.Presenter providePaymentPeriodListPresenter(TamaqApp app,
                                                                                 ServerCommunicator serverCommunicator) {
        return new PaymentPeriodListPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public ProfileContract.Presenter provideProfilePresenter(TamaqApp app,
                                                             ServerCommunicator serverCommunicator) {
        return new ProfilePresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public NotificationsContract.Presenter provideNotificationsPresenter(TamaqApp app,
                                                                         ServerCommunicator serverCommunicator) {
        return new NotificationsPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public NotificationDetailsContract.Presenter provideNotificationDetailsPresenter(TamaqApp app,
                                                                                     ServerCommunicator serverCommunicator) {
        return new NotificationDetailsPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public ProfileSettingsContract.Presenter provideProfileSettingsPresenter(TamaqApp app,
                                                                             ServerCommunicator serverCommunicator) {
        return new ProfileSettingsPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public OrdersArchiveContract.Presenter provideOrdersArchivePresenter(TamaqApp app,
                                                                         ServerCommunicator serverCommunicator) {
        return new OrdersArchivePresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public CompletedOrderContract.Presenter provideCompletedOrderPresenter(TamaqApp app,
                                                                           ServerCommunicator serverCommunicator) {
        return new CompletedOrderPresenter(app, serverCommunicator);
    }

    @Provides
    @CommonScope
    public MainContract.Presenter provideChatEditPresenter(
            ServerCommunicator serverCommunicator) {
        return new MainPresenter(serverCommunicator);
    }
}
