package com.tamaq.courier.shared;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerAppComponent;
import com.tamaq.courier.di.modules.AppModule;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class TamaqApp extends Application {

    private AppComponent appComponent;

    public static TamaqApp get(Context context) {
        return (TamaqApp) context.getApplicationContext();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Fabric.with(this, new Crashlytics());
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        appComponent.inject(this);

        Realm.init(getApplicationContext());
        // create your Realm configuration
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("_metadata.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        RxJavaPlugins.setErrorHandler(e -> {
            if (BuildConfig.DEBUG) Log.d("Application", "RxJavaPlugins error: ", e);
        });

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
    }
}
