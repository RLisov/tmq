/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tamaq.courier.presenters.main;

import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderRespondedInfo;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

public interface MainContract {

    interface View extends BaseView {

        void openCurrentOrderScreen();

        void serverTimeUpdated();

        void onNeedTrackUserLocation();

        void onNewOrderNotification(OrderRealm orderRealm, NotificationRealm notificationRealm);

        void onNewAutoRateNotification(NotificationRealm notificationRealm, boolean needShowDialog);

        void onNeedShowEstimateScreen(String orderId);

        void onNeedShowNewOrderScreen(OrderRespondedInfo orderRespondedInfo);

        void onDialogsUpdated();
    }

    interface Presenter extends BasePresenter<View> {

        void checkNeedStartChecker();

        void checkIsUserConfirmed();

        void checkNeedTrackUserLocation();

        void saveNotificationWasReadLocally(NotificationRealm notificationRealm);

        void checkNeedShowEstimateScreen();

        void checkIsExecutingOrder();

        void checkNeedShowNewOrderScreen();

        void getServerTime();

        void loadCitiesAndDistricts();

        void updateDialogs();

        void changeWorkStatusToLastActive();
    }
}
