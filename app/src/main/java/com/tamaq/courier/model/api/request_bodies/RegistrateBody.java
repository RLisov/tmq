package com.tamaq.courier.model.api.request_bodies;


import com.google.gson.annotations.SerializedName;

public class RegistrateBody {

    public static final String PROVIDER_KEY_PHONE_PASSWORD = "PHONE_PASSWORD";
    /**
     * Phone number of user
     */
    @SerializedName("user_provider_id")
    String userProviderId;
    /**
     * In this app it will always {@link #PROVIDER_KEY_PHONE_PASSWORD}
     */
    @SerializedName("provider_key")
    String providerKey;

    public RegistrateBody(String userProviderId, String providerKey) {
        this.userProviderId = userProviderId;
        this.providerKey = providerKey;
    }

}
