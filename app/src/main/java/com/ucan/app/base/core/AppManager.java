package com.ucan.app.base.core;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;


import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.ui.base.BaseActivity;

public class AppManager {


	/** Android 应用上下文 */
	private static Context mContext = null;
	/** 包名 */
	public static String pkgName = "com.ucan.app";
	/** SharedPreferences 存储名字前缀 */
	public static final String PREFIX = "com.ucan.app_";
	public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x10000000;
	/** IM功能UserData字段默认文字 */
	public static final String USER_DATA = "ucan.app";
	public static HashMap<String, Integer> mPhotoCache = new HashMap<String, Integer>();
	public static ArrayList<BaseActivity> activities = new ArrayList<BaseActivity>();
	public static String getPackageName() {
		return pkgName;
	}

	private static UserInfo mUserInfo;

	/**
	 * 返回SharePreference配置文件名称
	 * 
	 * @return
	 */
	public static String getSharePreferenceName() {
		return pkgName + "_preferences";
	}

	public static SharedPreferences getSharePreference() {
		if (mContext != null) {
			return mContext.getSharedPreferences(getSharePreferenceName(), 0);
		}
		return null;
	}

	/**
	 * 返回上下文对象
	 * 
	 * @return
	 */
	public static Context getContext() {
		return mContext;
	}

	public static void sendRemoveMemberBR() {

		getContext().sendBroadcast(
				new Intent("com.ucan.app.removemember"));
	}

	/**
	 * 设置上下文对象
	 * 
	 * @param context
	 */
	public static void setContext(Context context) {
		mContext = context;
		pkgName = context.getPackageName();
		LogUtil.d(LogUtil.getLogUtilsTag(AppManager.class),
				"setup application context for package: " + pkgName);
	}

	/**
	 * 缓存账号注册信息
	 * 
	 * @param user
	 */
	public static void setUserInfo(UserInfo user) {
		mUserInfo = user;
	}

	/**
	 * 保存注册账号信息
	 * 
	 * @return
	 */
	public  static UserInfo getUserInfo() {
		if (mUserInfo != null) {
			return mUserInfo;
		}
		String registAccount = getAutoRegistAccount();
		if (!TextUtils.isEmpty(registAccount)) {
			mUserInfo = new UserInfo("");
			return mUserInfo.from(registAccount);
		}
		return null;
	}

	public static String getUserId() {
		return getUserInfo().getAccount();
	}

	private static String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = UCPreferences
				.getSharedPreferences();
		PreferenceSettings registAuto = PreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}

	/**
	 * 获取应用程序版本名称
	 * 
	 * @return
	 */
	public static String getVersion() {
		String version = "0.0.0";
		if (mContext == null) {
			return version;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}

	/**
	 * 获取应用版本号
	 * 
	 * @return 版本号
	 */
	public static int getVersionCode() {
		int code = 1;
		if (mContext == null) {
			return code;
		}
		try {
			PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageInfo(getPackageName(), 0);
			code = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return code;
	}

	public static void addActivity(BaseActivity activity) {
		activities.add(activity);
	}

	public static void clearActivity() {
		for (BaseActivity activity : activities) {
			if (activity != null) {
				activity.finish();
				activity = null;
			}
			activities.clear();
		}
	}

	/**
	 * 打开浏览器下载新版本
	 * 
	 * @param context
	 */
	public static void startUpdater(Context context) {
		Uri uri = Uri.parse("http://dwz.cn/F8Amj");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(intent);
	}

	public static HashMap<String, Object> prefValues = new HashMap<String, Object>();

	/**
	 *
	 * @param key
	 * @param value
	 */
	public static void putPref(String key, Object value) {
		prefValues.put(key, value);
	}

	public static Object getPref(String key) {
		return prefValues.remove(key);
	}

	public static void removePref(String key) {
		prefValues.remove(key);
	}
	
}
