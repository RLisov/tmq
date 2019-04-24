package com.tamaq.courier.presenters.tabs.chat;

import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public interface ChatTabContract {

    interface View extends BaseView {

        void onUserChecked(boolean active);

        void displayDialogs(List<DialogRealm> dialogRealmList);

        void displayNoDialogs();

        void displayLoader();

        void hideLoader();

    }

    interface Presenter extends BasePresenter<View> {

        void checkUserState();

        void loadDialogs();

        void updateBadge();

        void deleteDialog(String chatId);

        void updateDialogs();

        void manualUpdateMessages();
    }

}
