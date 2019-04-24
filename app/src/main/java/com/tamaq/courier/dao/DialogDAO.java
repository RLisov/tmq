package com.tamaq.courier.dao;

import android.content.Context;

import com.tamaq.courier.model.api.common.OrderPojo;
import com.tamaq.courier.model.api.response.ChatMessageResponse;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.model.database.MessageRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.functions.Action0;

public class DialogDAO {

    private List<Action0> mNewMessageRecievedActions = new ArrayList<>();
    private List<Action0> mUpdateBadgeActions = new ArrayList<>();
    private List<Action0> mMessageUpdatedActions = new ArrayList<>();

    public static DialogDAO getInstance() {
        return InstanceHolder.instance;
    }

    public void addDialogs(List<DialogRealm> list, Realm realm) {
        list.removeAll(getHiddenDialogs(realm));

        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
        callListenerUpdateBadge();
    }

    private List<DialogRealm> getHiddenDialogs(Realm realm) {
        return realm.copyFromRealm(realm.where(DialogRealm.class)
                .equalTo(DialogRealm.HIDE_FROM_LIST_ROW, true)
                .findAll());
    }

    private void callListenerUpdateBadge() {
        if (mUpdateBadgeActions != null && !mUpdateBadgeActions.isEmpty()) {
            for (Action0 action : mUpdateBadgeActions) {
                action.call();
            }
        }
    }

    public void addDialog(DialogRealm dialog, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(dialog);
        realm.commitTransaction();
        callListenerUpdateBadge();
    }

    public void changeMessageStateDialog(DialogRealm dialog, Realm realm, boolean isRead) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        for (MessageRealm messageRealm : dialog.getMessages()) messageRealm.setIsRead(isRead);
        realm.copyToRealmOrUpdate(dialog);
        realm.commitTransaction();
        callListenerUpdateBadge();
    }

    public void addNewMessageToDialog(String chatId, String message, int type) {
        Realm realm = Realm.getDefaultInstance();
        if (!realm.isInTransaction()) realm.beginTransaction();

        DialogRealm dialogsByUserId = getDialogByChatId(chatId);
        dialogsByUserId.getMessages().add(MessageRealm.createNewMessage(chatId, message,
                "", false, type)); //TODO ID FOR CHAT

        realm.copyToRealmOrUpdate(dialogsByUserId);
        realm.commitTransaction();
        callListenerUpdateBadge();
        callListenerNewMessageReceived();
    }

    public DialogRealm getDialogByChatId(String chatId) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(DialogRealm.class).equalTo(DialogRealm.CHAT_ID_ROW, chatId).findFirst();
    }

    public void callListenerNewMessageReceived() {
        if (mNewMessageRecievedActions != null && !mNewMessageRecievedActions.isEmpty()) {
            for (Action0 action : mNewMessageRecievedActions) {
                action.call();
            }
        }
    }

    public void callListenerMessageUpdated() {
        if (mMessageUpdatedActions != null && !mMessageUpdatedActions.isEmpty()) {
            for (Action0 action : mMessageUpdatedActions) {
                action.call();
            }
        }
    }

    public void setMessageRead(String messageId, Realm realm) {
        realm.executeTransaction(realm1 -> {
            MessageRealm messageRealm = realm1
                    .where(MessageRealm.class)
                    .equalTo(MessageRealm.KEY_ID, messageId)
                    .findFirst();
            if (messageRealm != null) messageRealm.setIsRead(true);
        });
    }

    public List<DialogRealm> getDialogs(Realm realm) {
        return realm.where(DialogRealm.class)
                .notEqualTo(DialogRealm.HIDE_FROM_LIST_ROW, true)
                .findAll();
    }

    public boolean isUnreadedMessages() {
        Realm realm = Realm.getDefaultInstance();
        List<DialogRealm> allDialogs = realm.where(DialogRealm.class).findAll();
        for (DialogRealm dialog : allDialogs) {
            for (MessageRealm message : dialog.getMessages()) {
                if (!message.isRead()) return true;
            }
        }
        return false;
    }

    public void updateBadge() {
        callListenerUpdateBadge();
    }

    public MessageRealm saveToDialog(String chatId, MessageRealm messageRealm, Realm realm) {
        realm.executeTransaction(realm1 ->
                getDialogByChatId(chatId, realm1).getMessages().add(messageRealm));
        return messageRealm;
    }

    public DialogRealm getDialogByChatId(String chatId, Realm realm) {
        return realm.where(DialogRealm.class).equalTo(DialogRealm.CHAT_ID_ROW, chatId).findFirst();
    }

    public MessageRealm saveToDialog(String chatId, MessageRealm messageRealm, boolean isRead, Realm realm) {
        messageRealm.setIsRead(isRead);
        if (!realm.isInTransaction()) realm.beginTransaction();
        MessageRealm messageRealmResult = realm.copyToRealmOrUpdate(messageRealm);
        getDialogByChatId(chatId, realm).getMessages().add(messageRealmResult);
        realm.commitTransaction();
        return messageRealmResult;
    }

    public MessageRealm saveToDialog(String chatId, MessageRealm messageFromServer, String localMessageId, boolean isSent, boolean isRead, Realm realm) {
        messageFromServer.setIsRead(isRead);
        if (!realm.isInTransaction()) realm.beginTransaction();
        MessageRealm messageFromRealm = realm.where(MessageRealm.class).equalTo(MessageRealm.KEY_ID, localMessageId).findFirst();
        messageFromRealm.deleteFromRealm();

        messageFromServer.setSent(isSent);

        MessageRealm messageRealmResult = realm.copyToRealmOrUpdate(messageFromServer);
        getDialogByChatId(chatId, realm).getMessages().add(messageRealmResult);
        realm.commitTransaction();
        return messageRealmResult;
    }

    public void saveToDialogUnsent(String localMessageId, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        MessageRealm messageFromRealm = realm.where(MessageRealm.class).equalTo(MessageRealm.KEY_ID, localMessageId).findFirst();
        messageFromRealm.setSent(false);
        realm.commitTransaction();
    }

    @NonNull
    public List<OrderPojo> filterOrdersFor24Hours(List<OrderPojo> orderPojos) {
        List<OrderPojo> resultList = new ArrayList<>();
        Date yesterday = DateHelper.getCurrentDateMinusHours(24);
        Date orderDate;
        for (OrderPojo currentOrder : orderPojos) {
            if (currentOrder.getDateStringForDialogCreated() == null) continue;

            orderDate = DateHelper.parseDateFromString(currentOrder.getDateStringForDialogCreated());
            if (OrderStatus.isOrderCompleted(OrderStatus.fromString(currentOrder.getStatus()))) {
                if (!orderDate.equals(yesterday) && orderDate.before(yesterday)) {
                    continue;
                }
            }

            resultList.add(currentOrder);
        }
        return resultList;
    }

    @NonNull
    public List<DialogRealm> convertOrderPojoToDialogRealm(Context context, Realm realm, List<OrderPojo> orderPojoList) {
        List<DialogRealm> resultList = new ArrayList<>();
        DialogRealm dialogRealm;
        for (OrderPojo orderPojo : orderPojoList) {
            if (orderPojo.getCustomer() != null && orderPojo.getCustomer().getId() != null) {
                dialogRealm = new DialogRealm();
                String customerId = orderPojo.getCustomer().getId();

                dialogRealm.setChatId(orderPojo.getId());
                dialogRealm.setUserId(customerId);
                dialogRealm.setCreatedDate(DateHelper.correctDateWithDifference(orderPojo.getCreated()));
                dialogRealm.setUserName(orderPojo.getCustomer().getName());


                String resultAddress = "";
                if (orderPojo.getAddress() != null)
                    resultAddress = orderPojo.getAddress().getFormattedAddressForArchive(context);
                if (orderPojo.getServiceAddress() != null) {
                    if (!resultAddress.isEmpty()) resultAddress += " - ";
                    resultAddress += orderPojo.getServiceAddress().getFormattedAddressForArchive(context);
                }

                dialogRealm.setUserAddress(resultAddress);
                dialogRealm.setAvatarUrl(String.format(
                        "https://tamaq.kz/imgs/%s_avatar.png", orderPojo.getCustomer().getId()));

                dialogRealm.setOrderStatus(OrderStatus.isOrderCompleted(OrderStatus.fromString(orderPojo.getStatus())));

                RealmList<MessageRealm> messageList = new RealmList<>();
                List<MessageRealm> unsentMessagesByDialogId = getUnsentMessagesByDialogId(orderPojo.getId(), realm);

                for (ChatMessageResponse chatMessage : orderPojo.getChats()) {
                    MessageRealm message = convertMessagePojoToRealm(chatMessage, realm);
                    messageList.add(message);
                }

                messageList.addAll(unsentMessagesByDialogId);

                dialogRealm.setMessages(messageList);
                resultList.add(dialogRealm);

            }
        }
        return resultList;
    }

    public List<MessageRealm> getUnsentMessagesByDialogId(String chatId, Realm realm) {

        RealmResults<MessageRealm> unsentMessages = realm.where(MessageRealm.class)
                .equalTo(MessageRealm.KEY_CHAT_ID, chatId)
                .findAll();

        return unsentMessages != null && !unsentMessages.isEmpty()
                ? realm.copyFromRealm(unsentMessages)
                : new ArrayList<>();
    }

    @NonNull
    public MessageRealm convertMessagePojoToRealm(ChatMessageResponse chatMessage, Realm realm) {
        MessageRealm message = new MessageRealm();
        message.setId(chatMessage.getId());
        message.setIsRead(DialogDAO.getInstance().isMessageRead(realm, chatMessage.getId()));

        MessageRealm.MessageType messageType;
        String userId = UserDAO.getInstance().getUser(realm).getId();
        if (userId.equals(chatMessage.getUser().getId())) messageType = MessageRealm.MessageType.ME;
        else if (chatMessage.isFromTamaq()) messageType = MessageRealm.MessageType.TAMAQ;
        else messageType = MessageRealm.MessageType.CUSTOMER;
        message.setTypeByEnum(messageType);

        message.setMessage(chatMessage.getMsg());
        message.setTime(DateHelper.correctDateWithDifference(chatMessage.getCreated()));
        message.setImageUrl(chatMessage.getChatPhotoUrl());
        message.setWidth(chatMessage.getChatPhotoWidth());
        message.setHeight(chatMessage.getChatPhotoHeight());
        message.setSent(true);
        return message;
    }

    public boolean isMessageRead(Realm realm, String messageId) {
        MessageRealm messageRealm = realm
                .where(MessageRealm.class)
                .equalTo(MessageRealm.KEY_ID, messageId)
                .findFirst();
        return messageRealm != null ? messageRealm.isRead() : false;
    }

    @NonNull
    public void deleteDialog(String chatId, Realm realm) {
        realm.executeTransaction(realm1 ->
                getDialogByChatId(chatId, realm1).setHideFromList(true));
    }

    public void removeDialogById(String chatId, Realm realm) {
        realm.executeTransaction(realm1 ->
                realm.where(DialogRealm.class)
                        .equalTo(DialogRealm.CHAT_ID_ROW, chatId)
                        .findFirst()
                        .deleteFromRealm());
    }

    public void removeMessageById(String messageId, Realm realm) {
        realm.executeTransaction(realm1 ->
                realm1.where(MessageRealm.class)
                        .equalTo(MessageRealm.KEY_ID, messageId)
                        .findFirst()
                        .deleteFromRealm());
    }

    public MessageRealm getMessageById(String messageId, Realm realm) {
        MessageRealm message = realm
                .where(MessageRealm.class)
                .equalTo(MessageRealm.KEY_ID, messageId)
                .findFirst();
        return realm.copyFromRealm(message);
    }

    public void setNewMessageRecievedActions(Action0 newMessageRecievedAction) {
        mNewMessageRecievedActions.add(newMessageRecievedAction);
    }

    public void setUpdateBadgeActions(Action0 updateBadgeAction) {
        mUpdateBadgeActions.add(updateBadgeAction);
    }

    public void setMessageUpdatedActions(Action0 messageUpdatedAction) {
        mMessageUpdatedActions.add(messageUpdatedAction);
    }

    private static class InstanceHolder {
        private static final DialogDAO instance = new DialogDAO();
    }

}
