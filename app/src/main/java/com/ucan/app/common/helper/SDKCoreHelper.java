package com.ucan.app.common.helper;

import java.io.InvalidClassException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ucan.app.UCApplication;
import com.ucan.app.base.core.AppManager;
import com.ucan.app.chat.chatroom.MeetingMsgReceiver;
import com.ucan.app.chat.chatroom.VideoconferenceBaseActivity;
import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.ui.activities.MainActivity;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDeskManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECNotifyOptions;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;

public class SDKCoreHelper implements ECDevice.InitListener,
		ECDevice.OnECDeviceConnectListener, ECDevice.OnLogoutListener {

	public static final String TAG = "UCAN.SDKCoreHelper";
	public static final String ACTION_LOGOUT = "com.ucan.app_logout";
	public static final String ACTION_SDK_CONNECT = "com.ucan.app.intent_ACTION_SDK_CONNECT";
	public static final String ACTION_KICK_OFF = "com.ucan.app.intent_ACTION_KICK_OFF";
	private static SDKCoreHelper sInstance;
	private Context mContext;
	private ECDevice.ECConnectState mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
	private ECInitParams mInitParams;
	/** 初始化错误 */
	public static final int ERROR_CODE_INIT = -3;

	public static final int WHAT_SHOW_PROGRESS = 0x101A;
	public static final int WHAT_CLOSE_PROGRESS = 0x101B;
	private boolean mKickOff = false;
	private ECNotifyOptions mOptions;
	public static SoftUpdate mSoftUpdate;

	private Handler handler;

	private SDKCoreHelper() {
		initOptions();
	}

	private void initOptions() {
		mOptions = new ECNotifyOptions();
		mOptions.enable = true;
		mOptions.mNewMsgShake = UCPreferences.getSharedPreferences()
				.getBoolean(
						PreferenceSettings.SETTINGS_NEW_MSG_SHAKE.getId(),
						true);
		mOptions.mNewMsgSound = UCPreferences.getSharedPreferences()
				.getBoolean(
						PreferenceSettings.SETTINGS_NEW_MSG_SOUND.getId(),
						true);
		mOptions.clazz = MainActivity.class;
	}

	public static SDKCoreHelper getInstance() {
		if (sInstance == null) {
			sInstance = new SDKCoreHelper();
		}
		return sInstance;
	}

	public synchronized void setHandler(final Handler handler) {
		this.handler = handler;
	}

	public static boolean isKickOff() {
		return getInstance().mKickOff;
	}

	public static void init(Context ctx) {
		getInstance().mKickOff = false;
		LogUtil.d(TAG, "[init] start regist..");
		ctx = UCApplication.getInstance().getApplicationContext();
		getInstance().mContext = ctx;
		// 判断SDK是否已经初始化，没有初始化则先初始化SDK
		if (!ECDevice.isInitialized()) {
			getInstance().mConnect = ECDevice.ECConnectState.CONNECTING;
			ECDevice.initial(ctx, getInstance());
			postConnectNotify();
			return;
		}
		LogUtil.d(TAG, " SDK has inited , then regist..");
		// 已经初始化成功，直接进行注册
		getInstance().onInitialized();
	}

	public static void setSoftUpdate(String version, int mode) {
		getInstance().mSoftUpdate = new SoftUpdate(version, mode);
	}

	@Override
	public void onInitialized() {
		LogUtil.d(TAG, "ECSDK is ready");
		UserInfo UserInfo = AppManager.getUserInfo();
		if (mInitParams == null || mInitParams.getInitParams() == null
				|| mInitParams.getInitParams().isEmpty()) {
			mInitParams = ECInitParams.createParams();
		}
		mInitParams.reset();
		mInitParams.setUserid(UserInfo.getAccount());
		// appkey
		mInitParams.setAppKey(FileAccessor.getAppKey());
		// appToken
		mInitParams.setToken(FileAccessor.getAppToken());
		mInitParams.setMode(ECInitParams.LoginMode.FORCE_LOGIN);
		mInitParams.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
		if (!mInitParams.validate()) {
			Intent intent = new Intent(ACTION_SDK_CONNECT);
			intent.putExtra("error", -1);
			mContext.sendBroadcast(intent);
			return;
		}
		// 定义了全局消息接收回调
		mInitParams.setOnChatReceiveListener(IMChattingHelper.getInstance());

		// 设置SDK注册结果回调通知，当第一次初始化注册成功或者失败会通过该引用回调
		// 通知应用SDK注册状态
		// 当网络断开导致SDK断开连接或者重连成功也会通过该设置回调
		mInitParams.setOnDeviceConnectListener(this);
		if (ECDevice.getECMeetingManager() != null) {
			ECDevice.getECMeetingManager().setOnMeetingListener(
					MeetingMsgReceiver.getInstance());
		}
		ECDevice.login(mInitParams);

	}

	@Override
	public void onConnect() {
		// Deprecated
	}

	@Override
	public void onDisconnect(ECError error) {
		// SDK与云通讯平台断开连接
		// Deprecated
	}

	@Override
	public void onConnectState(ECDevice.ECConnectState state, ECError error) {
		if (state == ECDevice.ECConnectState.CONNECT_FAILED
				&& error.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
			try {
				UCPreferences.savePreference(
						PreferenceSettings.SETTINGS_REGIST_AUTO, "", true);
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
			mKickOff = true;
			// 失败，账号异地登陆
			Intent intent = new Intent(ACTION_KICK_OFF);
			intent.putExtra("kickoffText", error.errorMsg);
			mContext.sendBroadcast(intent);
			MainActivity.mLauncherUI.handlerKickOff(error.errorMsg);
			NotificationHelper.getInstance().showKickoffNotification(
					mContext, error.errorMsg);
		}
		getInstance().mConnect = state;
		Intent intent = new Intent(ACTION_SDK_CONNECT);
		intent.putExtra("error", error.errorCode);
		mContext.sendBroadcast(intent);
		postConnectNotify();
	}

	/**
	 * 当前SDK注册状态
	 * 
	 * @return
	 */
	public static ECDevice.ECConnectState getConnectState() {
		return getInstance().mConnect;
	}

	@Override
	public void onLogout() {
		getInstance().mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
		if (mInitParams != null && mInitParams.getInitParams() != null) {
			mInitParams.getInitParams().clear();
		}
		mInitParams = null;
		mContext.sendBroadcast(new Intent(ACTION_LOGOUT));
	}

	@Override
	public void onError(Exception exception) {
		LogUtil.e(TAG,
				"ECSDK couldn't start: " + exception.getLocalizedMessage());
		Intent intent = new Intent(ACTION_SDK_CONNECT);
		intent.putExtra("error", ERROR_CODE_INIT);
		mContext.sendBroadcast(intent);
		ECDevice.unInitial();
	}

	/**
	 * 状态通知
	 */
	private static void postConnectNotify() {

		if (getInstance().mContext instanceof MainActivity) {
			((MainActivity) getInstance().mContext)
					.onNetWorkNotify(getConnectState());
		}

	}

	public static void logout() {
		ECDevice.logout(getInstance());
		release();
	}

	public static void release() {
		getInstance().mKickOff = false;
		/*
		 * IMChattingHelper.getInstance().destroy(); ContactSqlManager.reset();
		 * ConversationSqlManager.reset(); GroupMemberSqlManager.reset();
		 * GroupNoticeSqlManager.reset(); GroupSqlManager.reset();
		 * IMessageSqlManager.reset(); ImgInfoSqlManager.reset();
		 */
	}

	/**
	 * IM聊天功能接口
	 * 
	 * @return
	 */
	public static ECChatManager getECChatManager() {
		ECChatManager ecChatManager = ECDevice.getECChatManager();
		LogUtil.d(TAG, "ecChatManager :" + ecChatManager);
		return ecChatManager;
	}

	/**
	 * 群组聊天接口
	 * 
	 * @return
	 */
	public static ECGroupManager getUCGroupManager() {
		return ECDevice.getECGroupManager();
	}

	public static ECDeskManager getECDeskManager() {
		return ECDevice.getECDeskManager();
	}

	/**
	 * VoIP呼叫接口
	 * 
	 * @return
	 */
	public static ECVoIPCallManager getVoIPCallManager() {
		return ECDevice.getECVoIPCallManager();
	}

	public static ECVoIPSetupManager getVoIPSetManager() {
		return ECDevice.getECVoIPSetupManager();
	}

	public static class SoftUpdate {
		public String version;
		public int mode;

		public SoftUpdate(String version, int mode) {
			this.version = version;
			this.mode = mode;
		}
	}

	/**
	 * 
	 * @return返回底层so库 是否支持voip及会议功能 true 表示支持 false表示不支持 请在sdk初始化完成之后调用
	 */
	public boolean isSupportMedia() {

		return ECDevice.isSupportMedia();
	}

	public static boolean hasFullSize(String inStr) {
		if (inStr.getBytes().length != inStr.length()) {
			return true;
		}
		return false;
	}

	public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {

		LogUtil.e(TAG,
				"[onReceivevideomsg ] Receive video phone message  , id :"
						+ msg.getMeetingNo() + ",type=" + msg.getMsgType());
		Bundle b = new Bundle();
		b.putParcelable("VideoConferenceMsg", msg);

		sendTarget(VideoconferenceBaseActivity.KEY_VIDEO_RECEIVE_MESSAGE, b);

	}

	long t = 0;

	public void sendTarget(int what, Object obj) {
		t = System.currentTimeMillis();
		while (handler == null && (System.currentTimeMillis() - t < 3500)) {

			try {
				Thread.sleep(80L);
			} catch (InterruptedException e) {
			}
		}

		if (handler == null) {
			LogUtil.w(TAG,
					"[RLVoiceHelper] handler is null, activity maybe destory, wait...");
			return;
		}

		Message msg = Message.obtain(handler);
		msg.what = what;
		msg.obj = obj;
		msg.sendToTarget();
	}

	/**
	 * 判断服务是否自动重启
	 * 
	 * @return 是否自动重启
	 */
	public static boolean isUIShowing() {
		return ECDevice.isInitialized();
	}

}
