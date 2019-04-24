package com.tamaq.courier.presenters.tabs.chat;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChatTabPresenter extends BasePresenterAbs<ChatTabContract.View>
        implements ChatTabContract.Presenter {

    private ChatTabFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private String mUserId;

    public ChatTabPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        mApp = app;
        mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(ChatTabContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (ChatTabFragment) view;
    }

    @Override
    public void checkUserState() {
        mServerCommunicator.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userPojo -> getView().onUserChecked(!userPojo.isBlocked()), onError);
    }

    @Override
    public void loadDialogs() {
        getView().displayLoader();

        getLocalDialogs();
        getDialogsFromServer();
    }

    private void getLocalDialogs() {
        List<DialogRealm> dialogs = new ArrayList<>(DialogDAO.getInstance().getDialogs(getRealm()));
        if (!dialogs.isEmpty()) {
            dialogs = removeDialogsOlder24Hours(dialogs);
            getView().displayDialogs(sortDialogsByDate(dialogs));
            getView().hideLoader();
        }
    }

    private void getDialogsFromServer() {
        mUserId = UserDAO.getInstance().getUser(getRealm()).getId();
        mServerCommunicator.getOrdersCurrentUser(mUserId)
                .map(orderPojoList ->
                        DialogDAO.getInstance().filterOrdersFor24Hours(orderPojoList))
                .map(orderPojoList ->
                        DialogDAO.getInstance().convertOrderPojoToDialogRealm(
                                getView().getContext(), getRealm(), orderPojoList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dialogList -> {
                    if (getView() == null) return;//todo hotfix

                    if (!dialogList.isEmpty()) {
                        DialogDAO.getInstance().addDialogs(dialogList, getRealm());
                        List<DialogRealm> dialogRealms = new ArrayList<>(DialogDAO.getInstance().getDialogs(getRealm()));
                        getView().displayDialogs(sortDialogsByDate(dialogRealms));
                    } else getView().displayNoDialogs();

                    getView().hideLoader();
                }, throwable -> {
                    if (getView() == null) return;
                    onError.accept(throwable);
                    getView().hideLoader();
                });
    }

    private List<DialogRealm> removeDialogsOlder24Hours(List<DialogRealm> dialogs) {
        Iterator<DialogRealm> iterator = dialogs.iterator();
        Date yesterday = DateHelper.getCurrentDateMinusHours(24);
        Date dialogDate;

        while (iterator.hasNext()) {
            DialogRealm dialog = iterator.next();
            dialogDate = DateHelper.parseDateFromString(dialog.getCreatedDate());

            if (dialog.isOrderCompleted()) {
                if (!dialogDate.equals(yesterday) && dialogDate.before(yesterday)) {
                    DialogDAO.getInstance().removeDialogById(dialog.getChatId(), getRealm());
                    iterator.remove();
                }
            }
        }

        return dialogs;
    }

    private List<DialogRealm> sortDialogsByDate(List<DialogRealm> list) {

        if (list.size() < 2) return list;
        Collections.sort(list, (o1, o2) -> {

            String firstDateString = (o1.getMessages() == null || o1.getMessages().isEmpty())
                    ? o1.getCreatedDate()
                    : o1.getMessages().get(o1.getLastMessageIndex()).getTime();

            String secondDateString = (o2.getMessages() == null || o2.getMessages().isEmpty())
                    ? o2.getCreatedDate()
                    : o2.getMessages().get(o2.getLastMessageIndex()).getTime();

            Date firstDate = DateHelper.parseDateFromString(firstDateString);
            Date secondDate = DateHelper.parseDateFromString(secondDateString);

            return secondDate.compareTo(firstDate);
        });

        return list;
    }

    @Override
    public void updateBadge() {
        DialogDAO.getInstance().updateBadge();
    }

    @Override
    public void deleteDialog(String chatId) {
        DialogDAO.getInstance().deleteDialog(chatId, getRealm());
    }

    @Override
    public void updateDialogs() {
        getLocalDialogs();
    }

    @Override
    public void manualUpdateMessages() {
        getDialogsFromServer();
    }
}
