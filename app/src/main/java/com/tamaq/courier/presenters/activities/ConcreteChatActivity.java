package com.tamaq.courier.presenters.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.tabs.chat.concrete_chat.ConcreteChatFragment;

public class ConcreteChatActivity extends BaseActivity {

    private static final String CHAT_ID = "chat_id";
    private static final String USER_NAME = "user_name";
    private static final String ORDER_STATUS = "order_status";
    private static final String NEW_MESSAGE = "new_message";

    public static Intent newInstance(Context context, String chatId, String userName, boolean orderCompleted) {
        Bundle args = new Bundle();
        args.putString(CHAT_ID, chatId);
        args.putString(USER_NAME, userName);
        args.putBoolean(ORDER_STATUS, orderCompleted);

        Intent intent = new Intent(context, ConcreteChatActivity.class);
        intent.putExtras(args);
        return intent;
    }

    public static Intent newInstance(Context context, String chatId) {
        Bundle args = new Bundle();
        args.putString(CHAT_ID, chatId);

        Intent intent = new Intent(context, ConcreteChatActivity.class);
        intent.putExtras(args);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concrete_chat);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        Bundle extras = getIntent().getExtras();
        String chatId = extras.getString(CHAT_ID);
        String userName = extras.getString(USER_NAME, "-");
        boolean orderCompleted = extras.getBoolean(ORDER_STATUS, false);

        if (extras.containsKey(USER_NAME)) {
            replaceFragment(ConcreteChatFragment.newInstance(chatId, userName, orderCompleted));
        } else {
            replaceFragment(ConcreteChatFragment.newInstance(chatId, false));
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return false;
    }
}
