package com.tamaq.courier.model.api.request_bodies;

import com.tamaq.courier.model.database.UserRealm;

import java.util.ArrayList;
import java.util.List;

public class RegistrationRequestBody {

    private String avatar;
    private String name;
    private String lastName;
    private String mobile;
    private String rnn;
    private int age;
    private String countryKey;
    private String workCityKey;
    private String transportType;
    private List<String> identificationPhotos;

    public RegistrationRequestBody(String avatar,
                                   String name,
                                   String lastName,
                                   String mobile,
                                   String rnn,
                                   int age,
                                   String countryKey,
                                   String workCityId,
                                   String transportType,
                                   List<String> identificationPhotos) {
        this.avatar = avatar;
        this.name = name;
        this.lastName = lastName;
        this.mobile = mobile;
        this.rnn = rnn;
        this.age = age;
        this.countryKey = countryKey;
        this.workCityKey = workCityId;
        this.transportType = transportType;
        this.identificationPhotos = identificationPhotos;
    }

    public RegistrationRequestBody(UserRealm user) {
        this.avatar = "avatatFormat"; //TODO change to image format for server
        this.name = user.getName();
        this.lastName = user.getLastName();
        this.mobile = user.getMobile();
        this.rnn = user.getRnn();
        this.age = user.getAge();
        this.countryKey = user.getCountry().getKey();
        this.workCityKey = user.getWorkCity().getKey();
//      this.transportType = user.getTransportType();
        this.identificationPhotos = new ArrayList<>(); //TODO change to image format for server
    }
}
