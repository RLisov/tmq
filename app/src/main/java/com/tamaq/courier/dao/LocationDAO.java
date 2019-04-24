package com.tamaq.courier.dao;

import com.tamaq.courier.model.database.CityRealm;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.RegionRealm;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class LocationDAO {

    public static LocationDAO getInstance() {
        return InstanceHolder.instance;
    }


    public void addLocations(List<LocationRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
    }

    public void addCountryWithCities(LocationRealm countryWithCities, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(countryWithCities);
        realm.commitTransaction();
    }

    public void addCityWithRegions(LocationRealm countryWithCities, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.copyToRealmOrUpdate(countryWithCities);
        realm.commitTransaction();
    }

    public void addCities(List<CityRealm> list, Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.where(CityRealm.class).findAll().deleteAllFromRealm();
        realm.copyToRealm(list);
        realm.commitTransaction();
    }

    public RegionRealm getRegionById(long regionId, Realm realm) {
        return realm.where(RegionRealm.class)
                .equalTo(RegionRealm.ID_ROW, regionId)
                .findFirst();
    }

    public List<RegionRealm> getRegionsByIds(List<Long> ids, Realm realm) {
        if (ids.size() == 0)
            return new ArrayList<>();

        return realm.where(RegionRealm.class)
                .in(RegionRealm.ID_ROW, ids.toArray(new Long[ids.size()]))
                .findAll();
    }

    public LocationRealm getLocationByKey(String key, Realm realm) {
        return realm.where(LocationRealm.class)
                .equalTo(LocationRealm.KEY_ROW, key)
                .findFirst();
    }

    public LocationRealm getCountryByName(String countryName, Realm realm) {
        return realm.where(LocationRealm.class)
                .equalTo(LocationRealm.NAME_RU_ROW, countryName)
                .findFirst();
    }


    public List<LocationRealm> getCountries(Realm realm) {
        return realm.where(LocationRealm.class)
                .equalTo(LocationRealm.TYPE_ROW, LocationRealm.Type.COUNTRIES.toString())
                .findAll();
    }

    public List<LocationRealm> getCitiesUnmanaged(Realm realm) {
        RealmResults<LocationRealm> all = getCities(realm);
        return realm.copyFromRealm(all);
    }

    public RealmResults<LocationRealm> getCities(Realm realm) {
        return realm.where(LocationRealm.class)
                .equalTo(LocationRealm.TYPE_ROW, LocationRealm.Type.CITIES.toString())
                .findAll();
    }

    public LocationRealm getCityByName(String cityName, Realm realm) {
        return realm.where(LocationRealm.class)
                .equalTo(LocationRealm.TYPE_ROW, LocationRealm.Type.CITIES.toString())
                .beginGroup()
                .contains(LocationRealm.NAME_RU_ROW, cityName)
                .or()
                .contains(LocationRealm.NAME_EN_ROW, cityName)
                .or()
                .contains(LocationRealm.NAME_KZ_ROW, cityName)
                .endGroup()
                .findFirst();
    }

    public void deleteAllCities(Realm realm) {
        if (!realm.isInTransaction()) realm.beginTransaction();
        realm.where(LocationRealm.class)
                .equalTo(LocationRealm.TYPE_ROW, LocationRealm.Type.CITIES.toString())
                .findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    private static class InstanceHolder {
        private static final LocationDAO instance = new LocationDAO();
    }
}
