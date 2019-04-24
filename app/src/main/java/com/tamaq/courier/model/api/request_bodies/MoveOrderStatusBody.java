package com.tamaq.courier.model.api.request_bodies;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.database.OrderRealm;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldCanBeLocal"})
public class MoveOrderStatusBody {

    @SerializedName("id")
    private String id;
    @SerializedName("to_status")
    private String toStatus;
    @SerializedName("msg")
    private String msg;
    @SerializedName("reasons")
    private List<KeyObject> reasons;

    public MoveOrderStatusBody(String id, String toStatus, String msg) {
        this(id, toStatus, msg, null);
    }

    public MoveOrderStatusBody(String id, String toStatus, String msg,
                               List<OrderRealm.CancelReason> reasons) {
        this.id = id;
        this.toStatus = toStatus;
        this.msg = msg;
        if (reasons != null) {
            this.reasons = new ArrayList<>();
            for (OrderRealm.CancelReason cancelReason : reasons) {
                this.reasons.add(new KeyObject(cancelReason.getValue()));
            }
        }
    }

    private class KeyObject {

        @SerializedName("key")
        public String key;

        KeyObject(String key) {
            this.key = key;
        }
    }
}
