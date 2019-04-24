package com.tamaq.courier.model.api.request_bodies;

import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.api.common.KeyObject;

public class EstimateBody {

    @SerializedName("msg_valuation")
    public String msgValuation;
    @SerializedName("valuation")
    public boolean valuation;
    @SerializedName("typerate")
    public KeyObject typerate;
    @SerializedName("tips")
    public boolean tips;

    public EstimateBody(String msgValuation, boolean valuation, boolean tips) {
        this.msgValuation = msgValuation;
        this.valuation = valuation;
        this.typerate = new KeyObject("rate_client");
        this.tips = tips;
    }
}
