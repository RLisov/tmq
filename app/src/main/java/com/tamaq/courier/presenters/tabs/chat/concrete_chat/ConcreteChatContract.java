package com.tamaq.courier.presenters.tabs.chat.concrete_chat;

import com.tamaq.courier.model.database.MessageRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;

import java.util.List;

public interface ConcreteChatContract {

    interface View extends BaseView {

        void displayMessages(String avatarUrl, String name, List<MessageRealm> messages);

        void messageSent(MessageRealm messageRealm);

        void displayEmptyChat();

        void removeMessage(int position);

        void messageUnsent(int position);

    }

    interface Presenter extends BasePresenter<View> {

        void loadMessages(String chatId);

        void sendMessage(String text, int position);

        void sendPhoto(String picturePath, int position);

        void addNewMessages();

        void resendMessage(String messageId, int position);

        void removeMessage(String messageId, int position);

        void updateMessages();

        void manualUpdateMessages();
    }

}
