package com.tamaq.courier.model.api.request_bodies;

public class PaymentsBody {

    public int from;
    public int count;

    public PaymentsBody(int from, int count) {
        this.from = from;
        this.count = count;
    }
}
