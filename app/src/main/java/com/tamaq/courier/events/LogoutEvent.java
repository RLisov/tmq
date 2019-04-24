package com.tamaq.courier.events;

public class LogoutEvent {

    private boolean mNeedLogout;

    public LogoutEvent(boolean needLogout) {
        mNeedLogout = needLogout;
    }

    public boolean isNeedLogout() {
        return mNeedLogout;
    }
}
