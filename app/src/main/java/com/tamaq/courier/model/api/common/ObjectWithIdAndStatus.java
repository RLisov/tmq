package com.tamaq.courier.model.api.common;


public class ObjectWithIdAndStatus extends ObjectWithId {

    public String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
