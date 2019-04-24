package com.tamaq.courier.presenters.tabs.chat.concrete_chat;

import android.os.Handler;
import android.text.TextUtils;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.model.database.MessageRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.PhotoHelper;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ConcreteChatPresenter extends BasePresenterAbs<ConcreteChatContract.View>
        implements ConcreteChatContract.Presenter {

    private ConcreteChatFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private String mChatId;
    private int mOldMessagesCount = 0;
    private boolean mIsRecentlyRefreshed;

    public ConcreteChatPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(ConcreteChatContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (ConcreteChatFragment) view;
    }

    @Override
    public void loadMessages(String chatId) {
        mChatId = chatId;

        getView();
        getLocalDialogs();
        getDialogsFromServer();
    }

    private void getLocalDialogs() {
        DialogRealm dialogByChatId = DialogDAO.getInstance().getDialogByChatId(mChatId, getRealm());
        if (dialogByChatId != null) {
            if (!dialogByChatId.getMessages().isEmpty()) {
                mOldMessagesCount = dialogByChatId.getMessages().size();
                DialogDAO.getInstance().changeMessageStateDialog(dialogByChatId, getRealm(), true);
                DialogRealm dialogRealm = getRealm().copyFromRealm(dialogByChatId);
                getView().displayMessages(dialogByChatId.getAvatarUrl(),
                        dialogByChatId.getUserName(),
                        prepareList(dialogRealm));
            } else if (dialogByChatId.isOrderCompleted()) getView().displayEmptyChat();
        }
    }

    private void getDialogsFromServer() {
        mServerCommunicator.getOrderById(mChatId)
                .map(orderPojo ->
                        DialogDAO.getInstance().convertOrderPojoToDialogRealm(getView().getContext(),
                                getRealm(), Collections.singletonList(orderPojo)))
                .map(list -> list.get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dialogRealm -> {
                    if (getView() == null) return;

                    if (dialogRealm.getMessages() != null && !dialogRealm.getMessages().isEmpty()) {

                        for (MessageRealm message : dialogRealm.getMessages()) {
                            message.setIsRead(true);
                        }

                        DialogDAO.getInstance().addDialog(dialogRealm, getRealm());

                        DialogRealm dialogByChatId = DialogDAO.getInstance().getDialogByChatId(mChatId, getRealm());
                        if (dialogByChatId != null) {
                            List<MessageRealm> messageRealms = prepareList(getRealm().copyFromRealm(dialogByChatId));
                            int currentCount = messageRealms.size();
                            if (currentCount - mOldMessagesCount == 1) addNewMessages();
                            else {
                                mOldMessagesCount = dialogRealm.getMessages().size();
                                getView().displayMessages(dialogRealm.getAvatarUrl(),
                                        dialogRealm.getUserName(), prepareList(dialogRealm));
                            }
                        } else {
                            mOldMessagesCount = dialogRealm.getMessages().size();
                            getView().displayMessages(dialogRealm.getAvatarUrl(),
                                    dialogRealm.getUserName(), prepareList(dialogRealm));
                        }

                    } else if (dialogRealm.isOrderCompleted()) getView().displayEmptyChat();
                }, onError);
    }

    @Override
    public void addNewMessages() {
        if (!mIsRecentlyRefreshed) {
            mIsRecentlyRefreshed = true;
            DialogRealm dialogByChatId = DialogDAO.getInstance().getDialogByChatId(mChatId, getRealm());
            if (dialogByChatId != null) {
                List<MessageRealm> messageRealms = prepareList(getRealm().copyFromRealm(dialogByChatId));
                int currentCount = messageRealms.size();

                while (mOldMessagesCount < currentCount) {
                    MessageRealm messageRealm = messageRealms.get(mOldMessagesCount);
                    getView().messageSent(messageRealm);
                    DialogDAO.getInstance().setMessageRead(messageRealm.getId(), getRealm());
                    mOldMessagesCount++;
                }
            }
            mIsRecentlyRefreshed = false;
            new Handler().postDelayed(() -> {
                if (getView() != null) mIsRecentlyRefreshed = false;
            }, 3000);
        }
    }

    private List<MessageRealm> prepareList(DialogRealm dialogRealm) {
        List<MessageRealm> messages = dialogRealm.getMessages();
        Collections.sort(messages, (o1, o2) -> {
            Date firstDate = DateHelper.parseDateFromString(o1.getTime());
            Date secondDate = DateHelper.parseDateFromString(o2.getTime());
            return firstDate.compareTo(secondDate);
        });
        return messages;
    }

    @Override
    public void resendMessage(String messageId, int position) {
        MessageRealm messageById = DialogDAO.getInstance().getMessageById(messageId, getRealm());
        DialogDAO.getInstance().removeMessageById(messageId, getRealm());
        if (TextUtils.isEmpty(messageById.getImageUrl()))
            sendMessage(messageById.getMessage(), position);
        else sendPhoto(messageById.getImageUrl(), position);
        getView().removeMessage(position);
    }

    @Override
    public void sendMessage(String text, int position) {
        final String currentMessageId = mChatId + "_" + DateHelper.getCurrentDateString();

        MessageRealm message = MessageRealm.createNewMessage(mChatId, text, currentMessageId, true, 0);
        message.setSent(true);
        DialogDAO.getInstance().saveToDialog(mChatId, message, true, getRealm());

        getView().messageSent(message);
        mOldMessagesCount++;

        mServerCommunicator.sendMessageToChat(mChatId, text)
                .map(messageResponse -> DialogDAO.getInstance().convertMessagePojoToRealm(messageResponse, getRealm()))
                .map(messageFromServer -> DialogDAO.getInstance().saveToDialog(mChatId, messageFromServer, currentMessageId, true, true, getRealm()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((messageRealm) -> {
                }, throwable -> {
                    unsuccessfulMessage(currentMessageId, position);
                    onError.accept(throwable);
                });
    }

    @Override
    public void sendPhoto(String picturePath, int position) {
        final String currentMessageId = mChatId + "_" + DateHelper.getCurrentDateString();

        MessageRealm message = MessageRealm.createNewPictureMessage(mChatId, picturePath, currentMessageId, true, 0);
        message.setSent(true);
        DialogDAO.getInstance().saveToDialog(mChatId, message, true, getRealm());

        getView().messageSent(message);
        mOldMessagesCount++;

        PhotoHelper.getSaveBitmapThumbToFileSingle(new File(picturePath), getView().getContext())
                .subscribe(pathAndBitmap ->
                                mServerCommunicator.sendChatPicture(mChatId, new File(pathAndBitmap.first))
                                        .map(messageResponse -> DialogDAO.getInstance().convertMessagePojoToRealm(messageResponse, getRealm()))
                                        .map(messageFromServer -> DialogDAO.getInstance().saveToDialog(mChatId, messageFromServer, currentMessageId, true, true, getRealm()))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe((messageRealm) -> {
                                        }, throwable -> {
                                            unsuccessfulMessage(currentMessageId, position);
                                            onError.accept(throwable);
                                        }),
                        onError);
    }

    private void unsuccessfulMessage(String currentMessageId, int position) {
        DialogDAO.getInstance().saveToDialogUnsent(currentMessageId, getRealm());
        getView().messageUnsent(position);
    }

    @Override
    public void removeMessage(String messageId, int position) {
        DialogDAO.getInstance().removeMessageById(messageId, getRealm());
        getView().removeMessage(position);
    }

    @Override
    public void updateMessages() {
        addNewMessages();
    }

    @Override
    public void manualUpdateMessages() {
        getDialogsFromServer();
    }
}
