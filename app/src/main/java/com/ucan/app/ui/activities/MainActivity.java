package com.ucan.app.ui.activities;

import java.io.InvalidClassException;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.ucan.app.R;
import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.db.IMessageSqlManager;
import com.ucan.app.common.helper.IMChattingHelper;
import com.ucan.app.base.core.UCContentObservers;
import com.ucan.app.ui.dialog.UCAlertDialog;
import com.ucan.app.ui.dialog.UCProgressDialog;
import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.common.helper.CrashHelper;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.ToastUtil;
import com.ucan.app.common.helper.NotificationHelper;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.common.utils.VeryUtils;
import com.ucan.app.common.helper.SDKCoreHelper;
import com.ucan.app.ui.base.BaseActivity;
import com.ucan.app.ui.fragment.FindFragment;
import com.ucan.app.ui.fragment.ChatRoomFragment;
import com.ucan.app.ui.fragment.GroupFragment;
import com.ucan.app.ui.fragment.SettingFragment;
import com.umeng.analytics.MobclickAgent;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

public class MainActivity extends BaseActivity {
	public static final String TAG = "UCAN.MainActivity";
	public static MainActivity mLauncherUI;
	private final static int TAB_INDEX_CHATROOM = 0;
	private final static int TAB_INDEX_PRACTISE = 1;
	private final static int TAB_INDEX_GROUP = 2;
	private final static int TAB_INDEX_SETTING = 3;
	private int tabBtnIndex;
	private int cTabIndex;
	private Button mTabBtn[];
	/*
	 * private OverflowAdapter.OverflowItem[] mItems; private OverflowHelper
	 * mOverflowHelper;
	 */
	private UCProgressDialog mPostingdialog;
	private boolean mInitActionFlag;
	private InternalReceiver internalReceiver;
	private Fragment[] fragments;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (mLauncherUI != null) {
			LogUtil.i(LogUtil.getLogUtilsTag(MainActivity.class),
					"finish last LauncherUI");
			mLauncherUI.finish();
		}
		super.onCreate(savedInstanceState);
		mLauncherUI = this;

		intRes();
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent.setDebugMode(true);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		UCContentObservers.getInstance().initContentObserver();
	}

	private void intRes() {
		setContentView(R.layout.activity_main);
		setSatutsBarTint(this, R.color.top_bar);
		fragments = new Fragment[] { new ChatRoomFragment(),
				new FindFragment(), new GroupFragment(), new SettingFragment() };
		getFragmentManager().beginTransaction()
				.add(R.id.fragment_contain, fragments[0])
				.add(R.id.fragment_contain, fragments[1])
				.add(R.id.fragment_contain, fragments[2])
				.add(R.id.fragment_contain, fragments[3]).hide(fragments[1])
				.hide(fragments[2]).hide(fragments[3]).commit();
		mTabBtn = new Button[4];
		mTabBtn[0] = (Button) findViewById(R.id.tab_btn_chatroom);
		mTabBtn[1] = (Button) findViewById(R.id.tab_btn_find);
		mTabBtn[2] = (Button) findViewById(R.id.tab_btn_group);
		mTabBtn[3] = (Button) findViewById(R.id.tab_btn_setting);
		mTabBtn[0].setSelected(true);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		LogUtil.d(LogUtil.getLogUtilsTag(MainActivity.class), " onKeyDown");
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				&& event.getAction() == KeyEvent.ACTION_UP) {
			// dismiss PlusSubMenuHelper
			/*
			 * if (mOverflowHelper != null &&
			 * mOverflowHelper.isOverflowShowing()) { mOverflowHelper.dismiss();
			 * return true; }
			 */
		}

		// 这里可以进行设置全局性的menu菜单的判断
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			doTaskToBackEvent();
		}

		try {

			return super.dispatchKeyEvent(event);
		} catch (Exception e) {
			LogUtil.e(LogUtil.getLogUtilsTag(MainActivity.class),
					"dispatch key event catch exception " + e.getMessage());
		}

		return false;
	}

	public void doTaskToBackEvent() {
		moveTaskToBack(true);
	}

	public void onTabBtnClick(View view) {
		switch (view.getId()) {
		case R.id.tab_btn_chatroom:
			tabBtnIndex = TAB_INDEX_CHATROOM;
			break;
		case R.id.tab_btn_find:
			tabBtnIndex = TAB_INDEX_PRACTISE;
			break;
		case R.id.tab_btn_group:
			tabBtnIndex = TAB_INDEX_GROUP;
			break;
		case R.id.tab_btn_setting:
			tabBtnIndex = TAB_INDEX_SETTING;
			break;
		}
		if (cTabIndex != tabBtnIndex) {
			getFragmentManager().beginTransaction().hide(fragments[cTabIndex])
					.show(fragments[tabBtnIndex]).commit();
		}
		mTabBtn[cTabIndex].setSelected(false);
		// 把当前btn设为选中状态
		mTabBtn[tabBtnIndex].setSelected(true);
		cTabIndex = tabBtnIndex;
	}

	private final void registerReceiver(String[] actionArray) {
		if (actionArray == null) {
			return;
		}
		IntentFilter intentfilter = new IntentFilter();
		for (String action : actionArray) {
			intentfilter.addAction(action);
		}
		if (internalReceiver == null) {
			internalReceiver = new InternalReceiver();
		}
		registerReceiver(internalReceiver, intentfilter);
	}

	private void reTryConnect() {
		ECDevice.ECConnectState connectState = SDKCoreHelper.getConnectState();
		if (connectState == null
				|| connectState == ECDevice.ECConnectState.CONNECT_FAILED) {

			if (!TextUtils.isEmpty(getAutoRegistAccount())) {
				SDKCoreHelper.init(this);
			}
		}
	}

	public void handlerKickOff(String kickoffText) {
		if (isFinishing()) {
			return;
		}
		UCAlertDialog buildAlert = UCAlertDialog.buildAlert(this, kickoffText,
				getString(R.string.dialog_btn_confim),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						NotificationHelper.getInstance()
								.forceCancelNotification();
						restartAPP();
					}
				});
		buildAlert.setTitle("异地登陆");
		buildAlert.setCanceledOnTouchOutside(false);
		buildAlert.setCancelable(false);
		buildAlert.show();
	}

	private class InternalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || TextUtils.isEmpty(intent.getAction())) {
				return;
			}
				// 改变背景或者 处理网络的全局变量
				LogUtil.d(TAG, "[onReceive] action:" + intent.getAction());
				if (SDKCoreHelper.ACTION_SDK_CONNECT.equals(intent.getAction())) {
					doInitAction();
					updateConnectState();
				} else if (SDKCoreHelper.ACTION_KICK_OFF.equals(intent
						.getAction())) {
					String kickoffText = intent.getStringExtra("kickoffText");
					handlerKickOff(kickoffText);
				}
			}

		}
	

	public void updateConnectState() {
		ECDevice.ECConnectState connect = SDKCoreHelper.getConnectState();
		if (connect == ECDevice.ECConnectState.CONNECTING) {
		} else if (connect == ECDevice.ECConnectState.CONNECT_FAILED) {
			ToastUtil.showMessage("连接失败");
			reTryConnect();
		} else if (connect == ECDevice.ECConnectState.CONNECT_SUCCESS) {
			ToastUtil.showMessage("连接成功");
		}
	}

	/**
	 * 处理一些初始化操作
	 */
	private void doInitAction() {
		if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS
				&& !mInitActionFlag) {

			// 检测当前的版本
			SDKCoreHelper.SoftUpdate mSoftUpdate = SDKCoreHelper.mSoftUpdate;
			if (mSoftUpdate != null) {
				if (VeryUtils.checkUpdater(mSoftUpdate.version)) {
					boolean force = mSoftUpdate.mode == 2;
					showUpdaterTips(force);
					if (force) {
						return;
					}
				}
			}
			// 检测离线消息
			checkOffineMessage();
			mInitActionFlag = true;
		}
	}

	void showProcessDialog() {
		mPostingdialog = new UCProgressDialog(MainActivity.this,
				R.string.login_posting_submit);
		mPostingdialog.show();
	}

	public void onNetWorkNotify(ECDevice.ECConnectState connect) {
		updateConnectState();

	}

	private void checkOffineMessage() {
		if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS) {
			return;
		}
		ECHandlerHelper handlerHelper = new ECHandlerHelper();
		handlerHelper.postDelayedRunnOnThead(new Runnable() {
			@Override
			public void run() {
				boolean result = IMChattingHelper.isSyncOffline();
				if (!result) {
					ECHandlerHelper.postRunnOnUI(new Runnable() {
						@Override
						public void run() {
							disPostingLoading();
						}
					});
					IMChattingHelper.checkDownFailMsg();
				}
			}
		}, 1000);
	}

	UCAlertDialog showUpdaterTipsDialog = null;

	private void showUpdaterTips(final boolean force) {
		if (showUpdaterTipsDialog != null) {
			return;
		}
		String negativeText = getString(force ? R.string.settings_logout
				: R.string.update_next);
		String msg = getString(R.string.new_update_version);
		showUpdaterTipsDialog = UCAlertDialog.buildAlert(this, msg,
				negativeText, getString(R.string.app_update),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showUpdaterTipsDialog = null;
						if (force) {
							try {
								UCPreferences
										.savePreference(
												PreferenceSettings.SETTINGS_FULLY_EXIT,
												true, true);
							} catch (InvalidClassException e) {
								e.printStackTrace();
							}
							restartAPP();
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						AppManager.startUpdater(MainActivity.this);
						// restartAPP();
						showUpdaterTipsDialog = null;
					}
				});

		showUpdaterTipsDialog.setTitle(R.string.app_tip);
		showUpdaterTipsDialog.setDismissFalse();
		showUpdaterTipsDialog.setCanceledOnTouchOutside(false);
		showUpdaterTipsDialog.setCancelable(false);
		showUpdaterTipsDialog.show();
	}

	private void disPostingLoading() {
		if (mPostingdialog != null && mPostingdialog.isShowing()) {
			mPostingdialog.dismiss();
		}
	}

	public void restartAPP() {
		ECDevice.unInitial();
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (internalReceiver != null) {
			unregisterReceiver(internalReceiver);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		CrashHelper.getInstance().setContext(this);
		// 统计时长
		MobclickAgent.onResume(this);
		registerReceiver(new String[] {
				IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE,
				SDKCoreHelper.ACTION_SDK_CONNECT });
		Intent intent = getIntent();
		if (intent != null && intent.getIntExtra("launch_from", 1) == 0x06) {
			// 从Login过来,注册SDK,SDK登陆
			SDKCoreHelper.init(this);
			LogUtil.e(TAG, String.valueOf(intent.getIntExtra("launch_from", 1)));
		}
		OnUpdateMsgUnreadCounts();
	}

	public void OnUpdateMsgUnreadCounts() {
		int unreadCount = IMessageSqlManager.qureyAllSessionUnreadCount();
		int notifyUnreadCount = IMessageSqlManager.getUnNotifyUnreadCount();
		int count = unreadCount;
		if (unreadCount >= notifyUnreadCount) {
			count = unreadCount - notifyUnreadCount;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
