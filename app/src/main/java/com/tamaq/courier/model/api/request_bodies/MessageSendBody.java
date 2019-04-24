package com.tamaq.courier.model.api.request_bodies;


public class MessageSendBody {

    private String msg;

    public MessageSendBody(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
