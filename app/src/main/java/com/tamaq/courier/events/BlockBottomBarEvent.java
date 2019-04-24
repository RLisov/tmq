package com.tamaq.courier.events;

public class BlockBottomBarEvent {

    private boolean mNeedBlock;

    public BlockBottomBarEvent(boolean needBlock) {
        mNeedBlock = needBlock;
    }

    public boolean isNeedBlock() {
        return mNeedBlock;
    }
}
