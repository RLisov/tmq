package com.tamaq.courier.utils;


import io.realm.Realm;
import io.realm.RealmObject;

public class RealmHelper {

    public static <T extends RealmObject> T getCopyIfManaged(T realmObject, Realm realm) {
        return realmObject.isManaged() ? realm.copyFromRealm(realmObject) : realmObject;
    }

}
