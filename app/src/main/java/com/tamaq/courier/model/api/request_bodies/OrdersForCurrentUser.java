package com.tamaq.courier.model.api.request_bodies;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OrdersForCurrentUser {

    @SerializedName("from")
    public int from;
    @SerializedName("count")
    public int count;
    @SerializedName("ands")
    public AndsCondition ands;
    @SerializedName("sort")
    public List<SortCondition> sort;

    public OrdersForCurrentUser(String userId) {
        this.from = 0;
        this.count = 100;
        this.ands = new AndsCondition(userId);
        this.sort = new ArrayList<>();
        this.sort.add(new SortCondition());
    }

    private static class AndsCondition {

        @SerializedName("eq")
        List<ConditionsForRequest> eq;

        AndsCondition(String userId) {
            eq = new ArrayList<>();
            eq.add(new ConditionsForRequest(userId));
        }

        static class ConditionsForRequest {

            @SerializedName("field")
            public String field;
            @SerializedName("type")
            public String type;
            @SerializedName("value")
            public String value;

            ConditionsForRequest(String userId) {
                field = "executor.id";
                type = "UUID";
                value = userId;
            }
        }
    }

    private static class SortCondition {

        @SerializedName("field")
        public String field;
        @SerializedName("type")
        public String type;
        @SerializedName("asc")
        public boolean asc;

        SortCondition() {
            field = "created";
            type = "timestamp";
            asc = false;
        }
    }
}
