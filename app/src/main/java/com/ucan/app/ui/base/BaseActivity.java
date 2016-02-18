package com.ucan.app.ui.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.ui.view.SystemBarTintManager;
import com.ucan.app.common.helper.SDKCoreHelper;

public class BaseActivity extends AppCompatActivity {
	private final static String TAG = "UCAN.BaseActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow()
                .setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
    }

    /**
     * hide inputMethod
	 */
	public void hideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			View localView = this.getCurrentFocus();
			if (localView != null && localView.getWindowToken() != null) {
				IBinder windowToken = localView.getWindowToken();
				inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	protected void handleReceiver(Context context, Intent intent) {
		// 广播处理
		if (intent == null) {
			return;
		}
		if (SDKCoreHelper.ACTION_KICK_OFF.equals(intent.getAction())) {
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void setSatutsBarTint(Activity activity, int color) {
		setTranslucentStatus();
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintResource(color);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarDarkMode(true, activity);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	protected void setTranslucentStatus() {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		} else {
			winParams.flags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		}
		win.setAttributes(winParams);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return;
		}
		super.onSaveInstanceState(outState);
	}

	/**
     * 检查是否需要自动登录
	 *
	 * @return
	 */
	protected String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = UCPreferences
				.getSharedPreferences();
		PreferenceSettings registAuto = PreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}

}
