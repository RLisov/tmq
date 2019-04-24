package com.tamaq.courier.model.api.request_bodies;


public class LoginBody extends RegistrateBody {

    /**
     * Password - SMS confirmation
     */
    String password;

    public LoginBody(String userProviderId, String providerKey, String password) {
        super(userProviderId, providerKey);
        this.password = password;
    }

}
