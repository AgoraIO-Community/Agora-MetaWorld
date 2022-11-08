package io.agora.metachat.example;

import android.app.Application;

import com.tencent.mmkv.MMKV;

public class MainApplication extends Application {

    public static MainApplication mGlobalApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mGlobalApplication = this;

        init();
    }

    private void init() {
        MMKV.initialize(mGlobalApplication);
    }

}
