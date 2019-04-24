package com.tamaq.courier.model.ui;

public class CheckableSingleTextItem implements CheckableUIItem, SingleTextUIItem {

    protected boolean isChecked;
    protected String title;
    protected String id;
    public CheckableSingleTextItem(String title, String id) {
        this.title = title;
        this.id = id;
    }

    @Override
    public boolean isCheckedUI() {
        return isChecked;
    }

    @Override
    public void setCheckedUI(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String getTitleUI() {
        return title;
    }

    @Override
    public String getIdUI() {
        return id;
    }

}
