package com.ucan.app.ui;
 
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.ucan.app.R;
import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.helper.SDKCoreHelper;
import com.ucan.app.ui.activities.LoginActivity;
import com.ucan.app.ui.activities.MainActivity;
import com.ucan.app.ui.base.BaseActivity;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

public class Splash extends BaseActivity {
	private String account;
	private Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		ctx = this;
		account = getAutoRegistAccount();
		if (!TextUtils.isEmpty(account)) {
			UserInfo user = new UserInfo("").from(account);
			AppManager.setUserInfo(user);
			SDKCoreHelper.init(ctx);
		}
		ECHandlerHelper.postDelayedRunnOnUI(initRunnable, 3000);

	}

	private Runnable initRunnable = new Runnable() {
		@Override
		public void run() {
			init();
		}
	};

	public void init() {
		if (!TextUtils.isEmpty(account)) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("launch_from", 0x28);
			startActivity(intent);
			finish();
			return;
		} else {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;

		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			return true;
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
