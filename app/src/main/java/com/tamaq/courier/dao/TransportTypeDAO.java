package com.tamaq.courier.dao;

import com.tamaq.courier.model.database.TransportTypeRealm;

import java.util.List;

import io.realm.Realm;

public class TransportTypeDAO {


    public static TransportTypeDAO getInstance() {
        return TransportTypeDAO.InstanceHolder.instance;
    }

    public List<TransportTypeRealm> getAll(Realm realm) {
        return realm.where(TransportTypeRealm.class)
                .findAll();
    }

    public TransportTypeRealm getTransportByKey(String key, Realm realm) {
        return realm.where(TransportTypeRealm.class)
                .equalTo(TransportTypeRealm.KEY_ROW, key)
                .findFirst();
    }

    public TransportTypeRealm getTransportByName(String name, Realm realm) {
        return realm.where(TransportTypeRealm.class)
                .beginGroup()
                .equalTo(TransportTypeRealm.NAME_EN_ROW, name)
                .or()
                .equalTo(TransportTypeRealm.NAME_RU_ROW, name)
                .or()
                .equalTo(TransportTypeRealm.NAME_KZ_ROW, name)
                .endGroup()
                .findFirst();
    }

    private static class InstanceHolder {
        private static final TransportTypeDAO instance = new TransportTypeDAO();
    }
}
