package com.tamaq.courier.model.api.response;


import com.tamaq.courier.model.api.common.ObjectWithId;
import com.tamaq.courier.model.api.common.ObjectWithIdAndStatus;

public class NotificationPojo {

    public String id;
    public String created;
    public String type;
    public ObjectWithId user;
    public String text;
    public ObjectWithIdAndStatus order;
    public boolean read;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ObjectWithId getUser() {
        return user;
    }

    public void setUser(ObjectWithId user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ObjectWithIdAndStatus getOrder() {
        return order;
    }

    public void setOrder(ObjectWithIdAndStatus order) {
        this.order = order;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
