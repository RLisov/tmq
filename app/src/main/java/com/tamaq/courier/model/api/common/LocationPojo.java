package com.tamaq.courier.model.api.common;


import com.tamaq.courier.model.database.DetailsRealm;

import java.util.List;

public class LocationPojo {

    private String type;
    private String valueRu;
    private String valueEn;
    private String valueKz;
    private DetailsRealm details;
    private List<LocationPojo> childes;
    private String key;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValueRu() {
        return valueRu;
    }

    public void setValueRu(String valueRu) {
        this.valueRu = valueRu;
    }

    public String getValueEn() {
        return valueEn;
    }

    public void setValueEn(String valueEn) {
        this.valueEn = valueEn;
    }

    public String getValueKz() {
        return valueKz;
    }

    public void setValueKz(String valueKz) {
        this.valueKz = valueKz;
    }

    public DetailsRealm getDetails() {
        return details;
    }

    public void setDetails(DetailsRealm details) {
        this.details = details;
    }

    public List<LocationPojo> getChildes() {
        return childes;
    }

    public void setChildes(List<LocationPojo> childes) {
        this.childes = childes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
