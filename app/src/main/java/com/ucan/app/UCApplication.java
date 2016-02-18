package com.ucan.app;

import java.io.InvalidClassException;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.ucan.app.base.core.AppManager;
import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.common.helper.CrashHelper;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.UCPreferences;

public class UCApplication extends Application {
	private static UCApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		AppManager.setContext(instance);
		FileAccessor.initFileAccess();
		setChattingContactId();
		CrashHelper.getInstance().init(instance);
	}

	/**
	 * 保存当前的聊天界面所对应的联系人、方便来消息屏蔽通知
	 */
	private void setChattingContactId() {
		try {
			UCPreferences.savePreference(
					PreferenceSettings.SETTING_CHATTING_CONTACTID, "", true);
		} catch (InvalidClassException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 单例，返回一个实例
	 * 
	 * @return
	 */
	public static UCApplication getInstance() {
		if (instance == null) {
            LogUtil.w("[UCApplication] instance is null.");
        }
		return instance;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	public boolean getAlphaSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("ALPHA");
			LogUtil.w("[UCApplication - getAlpha] Alpha is: " + b);
			return b;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 返回配置文件的日志开关
	 * 
	 * @return
	 */
	public boolean getLoggingSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("LOGGING");
			LogUtil.w("[UCApplication - getLogging] logging is: " + b);
			return b;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}
}
