package com.ucan.app.base.core;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.ContactsContract;

import com.ucan.app.UCApplication;

public class UCContentObservers {

    private static final int CONTACTS_CHANGED = 300;
    private static UCContentObservers ourInstance = new UCContentObservers();

    public static UCContentObservers getInstance() {
        return ourInstance;
    }

    private Context mContext;
    private UCContentObservers() {

        mContext = UCApplication.getInstance().getApplicationContext();
    }


    public void initContentObserver() {
        ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(ContactsContract.Data.CONTENT_URI, true, new MyContactObserver(null));
    }

    private class MyContactObserver extends ContentObserver {
        public MyContactObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            observerHandler.removeMessages(CONTACTS_CHANGED);
            observerHandler.sendEmptyMessageDelayed(CONTACTS_CHANGED, 1000);
        }
    }


    private Handler observerHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CONTACTS_CHANGED:
                    //ContactsCache.getInstance().reload();
                    break;
            }
        };
    };
}
