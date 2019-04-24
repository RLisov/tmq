/*
  Copyright 2016 Google Inc. All Rights Reserved.
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.tamaq.courier.controllers.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tamaq.courier.R;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.dao.NotificationsDAO;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.presenters.activities.ConcreteChatActivity;
import com.tamaq.courier.presenters.main.MainActivity;
import com.tamaq.courier.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final int TYPE_ORDER_CANCELED = 1;
    public static final int TYPE_NEW_MESSAGE = 2;
    public static final int TYPE_MULTIPLY_MESSAGES = -2;
    private static final String TAG = "MyFirebaseMsgService";
    private static final String TYPE_KEY = "type";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String MESSAGE_KEY = "message";
    private static final String MESSAGE_TYPE_KEY = "msg_type";

    private static int sLastUserId = -1;
    private static List<String> sNameList = new ArrayList<>();
    private static int sCount;

    public static void resetChatFields() {
        sLastUserId = -1;
        sNameList = new ArrayList<>();
        sCount = 0;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        sendNotification(remoteMessage);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            Map<String, String> data = remoteMessage.getData();
            int type = Integer.valueOf(data.get(TYPE_KEY));
            switch (type) {
                case 1:
                    showCancelOrderNotification(notificationManager, data);
                    break;
                case 2:
                    parseNewMessage(notificationManager, data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCancelOrderNotification(NotificationManagerCompat notificationManager, Map<String, String> data) {
        int type = Integer.valueOf(data.get(MESSAGE_TYPE_KEY));
        PendingIntent pendingIntent = getPendingIntentToTask(type, data);
        String notificationTitle = getTitleToType(type);
        String notificationDescription = getDescriptionToType(type, data);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_tamaq_status_bar)
                .setLargeIcon(getLargeIcon())
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setSound(getDefaultSoundUri())
                .setContentIntent(pendingIntent)
                .setLights(Notification.DEFAULT_LIGHTS, 3000, 3000);

        notificationManager.notify(-1, notificationBuilder.build());
    }

    private void parseNewMessage(NotificationManagerCompat notificationManager, Map<String, String> data) {
        int userId = Integer.valueOf(data.get(USER_ID_KEY));
        saveNewMessage(data);

        if (PrefsHelper.isChatScreenIsShown(getApplicationContext())) return;

        if (sLastUserId == -1 || userId == sLastUserId && sNameList.size() <= 1) {
            showUserOneMessage(notificationManager, data);
        } else {
            showMultiplyUserMessages(notificationManager, data);
        }
    }

    private PendingIntent getPendingIntentToTask(int typeId, Map<String, String> data) {
        Intent intent = null;
        switch (typeId) {
            case TYPE_ORDER_CANCELED:
//                int orderId = Integer.valueOf(data.get("order_id"));

                Observable.create(e -> OrderDAO.getInstance().cancelOrderFromClient())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
                intent = MainActivity.newInstance(this);
                break;

            case TYPE_NEW_MESSAGE:
                String user_id = String.valueOf(data.get(USER_ID_KEY));
                intent = ConcreteChatActivity.newInstance(this, user_id);
                break;

            case TYPE_MULTIPLY_MESSAGES:
                intent = MainActivity.newInstance(getApplicationContext(), 1);
                break;
        }

        if (intent == null)
            return null;

        Observable.create(e -> NotificationsDAO.getInstance().newNotificationAdded())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        return PendingIntent.getActivity(this, new Random().nextInt(), intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * The switch is used because there will be many different types of push notifications
     */
    private String getTitleToType(int type) {
        switch (type) {
            case TYPE_ORDER_CANCELED:
                return getString(R.string.order_canceled);
            case TYPE_NEW_MESSAGE:
                return getString(R.string.new_message);
        }
        return getString(R.string.app_name);
    }

    private String getDescriptionToType(int type, Map<String, String> data) {
        switch (type) {
            case TYPE_ORDER_CANCELED:
                return getString(R.string.contact_dispatcher_if_necessary);
            case TYPE_NEW_MESSAGE:
                return data.get(MESSAGE_KEY);
        }
        return getString(R.string.app_name);
    }

    private Bitmap getLargeIcon() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
    }

    private Uri getDefaultSoundUri() {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    private void saveNewMessage(Map<String, String> data) {
        String user_id = data.get(USER_ID_KEY);
        String message = data.get(MESSAGE_KEY);
        int type = Integer.valueOf(data.get(MESSAGE_TYPE_KEY));

        Observable.create(e -> {
            DialogRealm dialogsByUserId = DialogDAO.getInstance().getDialogByChatId(user_id);
            if (dialogsByUserId != null) {
                DialogDAO.getInstance().addNewMessageToDialog(user_id, message, type);
            } else {
                //TODO get new dialog from server. Access to ServerComunicator?
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void showUserOneMessage(NotificationManagerCompat notificationManager, Map<String, String> data) {
        sLastUserId = Integer.valueOf(data.get(USER_ID_KEY));
        sCount++;
        String userName = data.get(USER_NAME_KEY);
        if (sNameList.isEmpty()) sNameList.add(userName);
        else sNameList.set(0, userName);
        PendingIntent pendingIntent = getPendingIntentToTask(2, data);
        String resultDescription;
        if (sCount == 1) resultDescription = data.get(MESSAGE_KEY);
        else resultDescription = String.format("%s %s", getString(R.string.new_messages), sCount);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_tamaq_status_bar)
                .setLargeIcon(getLargeIcon())
                .setContentTitle(userName)
                .setContentText(resultDescription)
                .setAutoCancel(true)
                .setSound(getDefaultSoundUri())
                .setContentIntent(pendingIntent)
                .setLights(Notification.DEFAULT_LIGHTS, 3000, 3000);

        notificationManager.notify(sLastUserId, notificationBuilder.build());
    }

    private void showMultiplyUserMessages(NotificationManagerCompat notificationManager, Map<String, String> data) {
        PendingIntent pendingIntent = getPendingIntentToTask(-2, data);
        String userName = data.get(USER_NAME_KEY);
        sNameList.add(userName);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_tamaq_status_bar)
                .setLargeIcon(getLargeIcon())
                .setContentTitle(getString(R.string.new_messages_in_chat))
                .setAutoCancel(true)
                .setSound(getDefaultSoundUri())
                .setContentIntent(pendingIntent)
                .setLights(Notification.DEFAULT_LIGHTS, 3000, 3000))
                .setBigContentTitle(getString(R.string.new_messages_from));

        for (String name : sNameList) inboxStyle.addLine(name);

        notificationManager.notify(sLastUserId, inboxStyle.build());
    }

    private int getNotificationIdByType(int type, Map<String, String> data) {
        switch (type) {
            case TYPE_NEW_MESSAGE:
                return Integer.valueOf(data.get(USER_ID_KEY));
            default:
                return -1;
        }
    }
}
