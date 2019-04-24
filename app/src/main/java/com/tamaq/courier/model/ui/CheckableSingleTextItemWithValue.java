package com.tamaq.courier.model.ui;

public class CheckableSingleTextItemWithValue extends CheckableSingleTextItem {

    private String value;

    public CheckableSingleTextItemWithValue(String title, int id, String value) {
        this(title, String.valueOf(id), value);
    }

    public CheckableSingleTextItemWithValue(String title, String id, String value) {
        super(title, id);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
