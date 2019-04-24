package com.tamaq.courier.presenters.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tamaq.courier.R;
import com.tamaq.courier.presenters.base.BaseActivity;
import com.tamaq.courier.presenters.login.LoginFragment;
import com.tamaq.courier.presenters.splash.SplashScreenFragment;

public class SplashScreenActivity extends BaseActivity {

    public static final String ARG_SCREEN_TO_OPEN = "ARG_SCREEN_TO_OPEN";
    public static final String ARG_NEED_LOGOUT = "ARG_NEED_LOGOUT";

    public static Intent newInstance(Context context) {
        return newInstance(context, ScreenToOpen.SPLASH);
    }

    public static Intent newInstance(Context context, boolean needLogout) {
        Intent intent = newInstance(context, ScreenToOpen.SPLASH);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(ARG_NEED_LOGOUT, needLogout);
        return intent;
    }


    public static Intent newInstance(Context context, ScreenToOpen screenToOpen) {
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.putExtra(ARG_SCREEN_TO_OPEN, screenToOpen);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ScreenToOpen screenToOpen = (ScreenToOpen)
                getIntent().getSerializableExtra(ARG_SCREEN_TO_OPEN);

        boolean needLogout = getIntent().getBooleanExtra(ARG_NEED_LOGOUT, false);

        if (screenToOpen == null) {
            replaceFragment(SplashScreenFragment.newInstance(needLogout));
        } else {
            switch (screenToOpen) {
                case SPLASH:
                    replaceFragment(SplashScreenFragment.newInstance(needLogout));
                    break;
                case LOGIN:
                    replaceFragment(LoginFragment.newInstance());
                    break;
            }
        }
    }

    public enum ScreenToOpen {SPLASH, LOGIN}

}
