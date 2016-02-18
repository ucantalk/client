 
package com.ucan.app.common.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.db.ContactSqlManager;
import com.ucan.app.base.db.GroupNoticeSqlManager;
import com.ucan.app.base.db.GroupSqlManager;
import com.ucan.app.base.db.IMessageSqlManager;
import com.ucan.app.base.db.ImgInfoSqlManager;
import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.common.model.Contact;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.model.Image;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.common.utils.VeryUtils;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECMessage.Direction;
import com.yuntongxun.ecsdk.ECMessage.Type;
import com.yuntongxun.ecsdk.OnChatReceiveListener;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECFileMessageBody;
import com.yuntongxun.ecsdk.im.ECImageMessageBody;
import com.yuntongxun.ecsdk.im.ECMessageDeleteNotify;
import com.yuntongxun.ecsdk.im.ECMessageNotify;
import com.yuntongxun.ecsdk.im.ECMessageNotify.NotifyType;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.ECVideoMessageBody;
import com.yuntongxun.ecsdk.im.ECVoiceMessageBody;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;

 
public class IMChattingHelper implements OnChatReceiveListener,
		ECChatManager.OnDownloadMessageListener {

	private static final String TAG = "UCAN.IMChattingHelper";
	public static final String INTENT_ACTION_SYNC_MESSAGE = "com.ucan.app_sync_message";
	public static final String GROUP_PRIVATE_TAG = "@priategroup.com";
	private static HashMap<String, SyncMsgEntry> syncMessage = new HashMap<String, SyncMsgEntry>();
	private static IMChattingHelper sInstance;
	private boolean isSyncOffline = false;

	public static IMChattingHelper getInstance() {
		if (sInstance == null) {
			sInstance = new IMChattingHelper();
		}
		return sInstance;
	}

	/** 云通讯SDK聊天功能接口 */
	private ECChatManager mChatManager;
	/** 全局处理所有的IM消息发送回调 */
	private ChatManagerListener mListener;
	/** 是否是同步消息 */
	private boolean isFirstSync = false;

	private IMChattingHelper() {
		mChatManager = SDKCoreHelper.getECChatManager();
		mListener = new ChatManagerListener();
	}

	private void checkChatManager() {
		mChatManager = SDKCoreHelper.getECChatManager();
	}

	/**
	 * 消息发送报告
	 */
	private OnMessageReportCallback mOnMessageReportCallback;

	/**
	 * 发送ECMessage 消息
	 * 
	 * @param msg
	 */
	public static long sendECMessage(ECMessage msg) {
		getInstance().checkChatManager();
		// 获取一个聊天管理器
		ECChatManager manager = getInstance().mChatManager;
		if (manager != null) {
			// 调用接口发送IM消息
			boolean isShowChatName = UCPreferences.getSharedPreferences().getBoolean(PreferenceSettings.SETTINGS_SHOW_CHATTING_NAME.getId(), false);
			msg.setMsgTime(System.currentTimeMillis());
			manager.sendMessage(msg, getInstance().mListener);
			// 保存发送的消息到数据库
			if (msg.getType() == Type.FILE
					&& msg.getBody() instanceof ECFileMessageBody) {
				ECFileMessageBody fileMessageBody = (ECFileMessageBody) msg
						.getBody();
				msg.setUserData("fileName=" + fileMessageBody.getFileName());
			}
		} else {
			msg.setMsgStatus(ECMessage.MessageStatus.FAILED);
		}
		return IMessageSqlManager.insertIMessage(msg,
				ECMessage.Direction.SEND.ordinal());
	}

	public void destroy() {
		if (syncMessage != null) {
			syncMessage.clear();
		}
		mListener = null;
		mChatManager = null;
		isFirstSync = false;
		sInstance = null;
	}

	/**
	 * 消息重发
	 * 
	 * @param msg
	 * @return
	 */
	public static long reSendECMessage(ECMessage msg) {
		ECChatManager manager = getInstance().mChatManager;
		if (manager != null) {
			// 调用接口发送IM消息
			String oldMsgId = msg.getMsgId();
			
			if(msg.getType()==Type.IMAGE&&IMessageSqlManager.isFireMsg(oldMsgId)){
				msg.setUserData("fireMessage");
			}
			
			manager.sendMessage(msg, getInstance().mListener);
			if (msg.getType() == ECMessage.Type.IMAGE) {
				Image imgInfo = ImgInfoSqlManager.getInstance().getImgInfo(
						oldMsgId);
				if (imgInfo == null
						|| TextUtils.isEmpty(imgInfo.getBigImgPath())) {
					return -1;
				}
				String bigImagePath = new File(FileAccessor.getImagePathName(),
						imgInfo.getBigImgPath()).getAbsolutePath();
				imgInfo.setMsglocalid(msg.getMsgId());
				ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
				body.setLocalUrl(bigImagePath);
				BitmapFactory.Options options = VeryUtils
						.getBitmapOptions(new File(FileAccessor.IMESSAGE_IMAGE,
								imgInfo.getThumbImgPath()).getAbsolutePath());
				msg.setUserData("outWidth://" + options.outWidth
						+ ",outHeight://" + options.outHeight + ",THUMBNAIL://"
						+ msg.getMsgId());
				ImgInfoSqlManager.getInstance().updateImageInfo(imgInfo);
			}
			// 保存发送的消息到数据库
			return IMessageSqlManager.changeResendMsg(msg.getId(), msg);
		}
		return -1;
	}

	public static long sendImageMessage(Image imgInfo, ECMessage message) {
		ECChatManager manager = getInstance().mChatManager;
		if (manager != null) {
			// 调用接口发送IM消息
			manager.sendMessage(message, getInstance().mListener);

			if (TextUtils.isEmpty(message.getMsgId())) {
				return -1;
			}
			imgInfo.setMsglocalid(message.getMsgId());
			BitmapFactory.Options options = VeryUtils
					.getBitmapOptions(new File(FileAccessor.IMESSAGE_IMAGE,
							imgInfo.getThumbImgPath()).getAbsolutePath());
			message.setUserData("outWidth://" + options.outWidth
					+ ",outHeight://" + options.outHeight + ",THUMBNAIL://"
					+ message.getMsgId() + ",PICGIF://" + imgInfo.isGif);
			long row = IMessageSqlManager.insertIMessage(message,
					ECMessage.Direction.SEND.ordinal());
			
			if (row != -1) {
				return ImgInfoSqlManager.getInstance().insertImageInfo(imgInfo);
			}
		}
		return -1;

	}

	public void getUserInfo() {
		LogUtil.d(TAG, "[getUserInfo] currentVersion :");
		final UserInfo UserInfo = AppManager.getUserInfo();
		if (UserInfo == null) {
			return;
		}
	}

	private class ChatManagerListener implements
			ECChatManager.OnSendMessageListener {

		@Override
		public void onSendMessageComplete(ECError error, ECMessage message) {
			if (message == null) {
				return;
			}
			// 处理ECMessage的发送状态
			if (message != null) {
				if (message.getType() == ECMessage.Type.VOICE) {
					try {
						VeryUtils.playNotifycationMusic(
								AppManager.getContext(),
								"sound/voice_message_sent.mp3");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				IMessageSqlManager.setIMessageSendStatus(message.getMsgId(),
						message.getMsgStatus().ordinal());
				IMessageSqlManager.notifyMsgChanged(message.getSessionId());
				if (mOnMessageReportCallback != null) {
					mOnMessageReportCallback.onMessageReport(error, message);
				}
				return;
			}
		}

		@Override
		public void onProgress(String msgId, int total, int progress) {
			// 处理发送文件IM消息的时候进度回调
			LogUtil.d(TAG, "[IMChattingHelper - onProgress] msgId：" + msgId
					+ " ,total：" + total + " ,progress:" + progress);
		}

	}

	public static void setOnMessageReportCallback(
			OnMessageReportCallback callback) {
		getInstance().mOnMessageReportCallback = callback;
	}

	public interface OnMessageReportCallback {
		void onMessageReport(ECError error, ECMessage message);
		
		void onPushMessage(String sessionId, List<ECMessage> msgs);
	}

	private int getMaxVersion() {
		int maxVersion = IMessageSqlManager.getMaxVersion();
		int maxVersion1 = GroupNoticeSqlManager.getMaxVersion();
		return maxVersion > maxVersion1 ? maxVersion : maxVersion1;
	}

	/**
	 * 收到新的IM文本和附件消息
	 */
	@Override
	public void OnReceivedMessage(ECMessage msg) {
		LogUtil.d(TAG, "[OnReceivedMessage] show notice true");
		if (msg == null) {
			return;
		}
		postReceiveMessage(msg, true);
	}

	/**
	 * 处理接收消息
	 * 
	 * @param msg
	 * @param showNotice
	 */
	private synchronized void postReceiveMessage(ECMessage msg,
			boolean showNotice) {
		// 接收到的IM消息，根据IM消息类型做不同的处理
		// IM消息类型：ECMessage.Type
		if (msg.getType() != ECMessage.Type.TXT) {
			ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
			FileAccessor.initFileAccess();
			if (!TextUtils.isEmpty(body.getRemoteUrl())) {
				boolean thumbnail = false;
				String fileExt = VeryUtils
						.getExtensionName(body.getRemoteUrl());
				LogUtil.e(TAG , "fileSize " + body.getLength());
				if (msg.getType() == ECMessage.Type.VOICE) {
					body.setLocalUrl(new File(FileAccessor.getVoicePathName(),
							VeryUtils.md5(String.valueOf(System
									.currentTimeMillis())) + ".amr")
							.getAbsolutePath());
				} else if (msg.getType() == ECMessage.Type.IMAGE) {
					ECImageMessageBody imageBody = (ECImageMessageBody) body;
					thumbnail = !TextUtils.isEmpty(imageBody
							.getThumbnailFileUrl());
					imageBody.setLocalUrl(new File(FileAccessor
							.getImagePathName(), VeryUtils
							.md5(thumbnail ? imageBody.getThumbnailFileUrl()
									: imageBody.getRemoteUrl())
							+ "." + fileExt).getAbsolutePath());
				} else {

					if (msg.getBody() instanceof ECVideoMessageBody) {
						ECVideoMessageBody videoBody = (ECVideoMessageBody) body;
						
						thumbnail = !TextUtils.isEmpty(videoBody
								.getThumbnailUrl());
						StringBuilder builder = new StringBuilder(
								videoBody.getFileName());
						builder.append("_thum.png");
						body.setLocalUrl(new File(FileAccessor
								.getFilePathName(), builder.toString())
								.getAbsolutePath());

					} else {
						body.setLocalUrl(new File(FileAccessor
								.getFilePathName(), VeryUtils.md5(String
								.valueOf(System.currentTimeMillis()))
								+ "."
								+ fileExt).getAbsolutePath());
					}
				}
				if (syncMessage != null) {
					syncMessage.put(msg.getMsgId(), new SyncMsgEntry(
							showNotice, thumbnail, msg));
				}
				if (mChatManager != null) {
					if (thumbnail) {
						mChatManager.downloadThumbnailMessage(msg, this);
					} else {
						mChatManager.downloadMediaMessage(msg, this);
					}
				}
				if (TextUtils.isEmpty(body.getFileName())
						&& !TextUtils.isEmpty(body.getRemoteUrl())) {
					body.setFileName(FileAccessor.getFileName(body
							.getRemoteUrl()));
				}
				if(msg.getType()==Type.IMAGE&&msg.getDirection()==Direction.RECEIVE){
					msg.setUserData(msg.getUserData());
				}else {
					msg.setUserData("fileName=" + body.getFileName());
					
				}
				
				if (IMessageSqlManager.insertIMessage(msg, msg.getDirection()
						.ordinal()) > 0) {
					return;
				}
			} else {
				LogUtil.e(TAG, "ECMessage fileUrl: null");
			}
		}

		if (IMessageSqlManager
				.insertIMessage(msg, msg.getDirection().ordinal()) <= 0) {
			return;
		}

		if (mOnMessageReportCallback != null) {
			ArrayList<ECMessage> msgs = new ArrayList<ECMessage>();
			msgs.add(msg);
			mOnMessageReportCallback.onPushMessage(msg.getSessionId(), msgs);
		}

		// 是否状态栏提示
		if (showNotice)
			
			showNotification(msg);
	}

	private static void showNotification(ECMessage msg) {
		if (checkNeedNotification(msg.getSessionId())) {
			NotificationHelper.getInstance().forceCancelNotification();
			String lastMsg = "";
			if (msg.getType() == ECMessage.Type.TXT) {
				lastMsg = ((ECTextMessageBody) msg.getBody()).getMessage();
			}
			Contact contact = ContactSqlManager.getContact(Long.parseLong(msg.getForm()));
			if (contact == null) {
				return;
			}
			NotificationHelper.getInstance()
					.showCustomNewMessageNotification(
							AppManager.getContext(), lastMsg,
							contact.getNickname(), msg.getSessionId(),
							msg.getType().ordinal());
		}
	}

	public static void checkDownFailMsg() {
		getInstance().postCheckDownFailMsg();
	}

	private void postCheckDownFailMsg() {
		List<ECMessage> downdFailMsg = IMessageSqlManager.getDowndFailMsg();
		if (downdFailMsg == null || downdFailMsg.isEmpty()) {
			return;
		}
		for (ECMessage msg : downdFailMsg) {
			ECImageMessageBody body = (ECImageMessageBody) msg.getBody();
			body.setThumbnailFileUrl(body.getRemoteUrl() + "_thum");
			if (syncMessage != null) {
				syncMessage.put(msg.getMsgId(), new SyncMsgEntry(false, true,
						msg));
			}
			if (mChatManager != null) {
				mChatManager.downloadThumbnailMessage(msg, this);
			}
		}

	}

	/**
	 * 是否需要状态栏通知
	 * 
	 * @param contactId
	 */
	public static boolean checkNeedNotification(String contactId) {
		String currentChattingContactId = UCPreferences
				.getSharedPreferences()
				.getString(
						PreferenceSettings.SETTING_CHATTING_CONTACTID.getId(),
						(String) PreferenceSettings.SETTING_CHATTING_CONTACTID
								.getDefaultValue());
		if (contactId == null) {
			return true;
		}
		// 当前聊天
		if (contactId.equals(currentChattingContactId)) {
			return false;
		}
		// 群组免打扰
		if (contactId.toUpperCase().startsWith("G")) {
			return GroupSqlManager.isGroupNotify(contactId);
		}
		return true;
	}

	@Override
	public void OnReceiveGroupNoticeMessage(ECGroupNoticeMessage notice) {
		if (notice == null) {
			return;
		}

		// 接收到的群组消息，根据群组消息类型做不同处理
		// 群组消息类型：ECGroupMessageType
//		GroupNoticeHelper.insertNoticeMessage(notice,
//				new GroupNoticeHelper.OnPushGroupNoticeMessageListener() {
//
//					@Override
//					public void onPushGroupNoticeMessage(GroupNotice system) {
//						IMessageSqlManager
//								.notifyMsgChanged(GroupNoticeSqlManager.CONTACT_ID);
//
//						ECMessage msg = ECMessage
//								.createECMessage(ECMessage.Type.TXT);
//						msg.setSessionId(system.getSender());
//						msg.setForm(system.getSender());
//						ECTextMessageBody tx = new ECTextMessageBody(system
//								.getContent());
//						msg.setBody(tx);
//						// 是否状态栏提示
//						showNotification(msg);
//					}
//				});

	}

	private int mHistoryMsgCount = 0;

	@Override
	public void onOfflineMessageCount(int count) {
		mHistoryMsgCount = count;
	}

	@Override
	public int onGetOfflineMessage() {
		// 获取全部的离线历史消息
		return ECDevice.SYNC_OFFLINE_MSG_ALL;
	}

	private ECMessage mOfflineMsg = null;

	@Override
	public void onReceiveOfflineMessage(List<ECMessage> msgs) {
		// 离线消息的处理可以参考 void OnReceivedMessage(ECMessage msg)方法
		// 处理逻辑完全一样
		// 参考 IMChattingHelper.java
		LogUtil.d(TAG, "[onReceiveOfflineMessage] show notice false");
		if (msgs != null && !msgs.isEmpty() && !isFirstSync)
			isFirstSync = true;
		for (ECMessage msg : msgs) {
			mOfflineMsg = msg;
			postReceiveMessage(msg, false);
		}
	}

	@Override
	public void onReceiveOfflineMessageCompletion() {
		LogUtil.e("message",String.valueOf(mOfflineMsg == null));
		if (mOfflineMsg == null) {
			return;
		}
		// SDK离线消息拉取完成之后会通过该接口通知应用
		// 应用可以在此做类似于Loading框的关闭，Notification通知等等
		// 如果已经没有需要同步消息的请求时候，则状态栏开始提醒
		ECMessage lastECMessage = mOfflineMsg;
		try {
			if (lastECMessage != null && mHistoryMsgCount > 0 && isFirstSync) {
				showNotification(lastECMessage);
				// lastECMessage.setSessionId(lastECMessage.getTo().startsWith("G")?lastECMessage.getTo():lastECMessage.getForm());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isFirstSync = isSyncOffline = false;
		// 无需要同步的消息
		AppManager.getContext().sendBroadcast(
				new Intent(INTENT_ACTION_SYNC_MESSAGE));
		mOfflineMsg = null;
	}

	public int mServicePersonVersion = 0;

	@Override
	public void onServicePersonVersion(int version) {
		mServicePersonVersion = version;
	}

	/**
	 * 客服消息
	 * 
	 * @param msg
	 */
	@Override
	public void onReceiveDeskMessage(ECMessage msg) {
		LogUtil.d(TAG, "[onReceiveDeskMessage] show notice true");
		OnReceivedMessage(msg);
	}

	@Override
	public void onSoftVersion(String version, int sUpdateMode) {
		SDKCoreHelper.setSoftUpdate(version, sUpdateMode);
	}

	public static boolean isSyncOffline() {
		return getInstance().isSyncOffline;
	}

	/**
	 * 下载
	 */
	@Override
	public void onDownloadMessageComplete(ECError e, ECMessage message) {
		if (e.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
			if (message == null)
				return;
			// 处理发送文件IM消息的时候进度回调
			LogUtil.d(TAG,
					"[onDownloadMessageComplete] msgId：" + message.getMsgId());
			postDowloadMessageResult(message);

			if (message.getType() == Type.VIDEO
					&& mOnMessageReportCallback != null
					&& message.getDirection()==Direction.RECEIVE
					/*&& mOnMessageReportCallback instanceof ChattingFragment*/) {

				/*((ChattingFragment) mOnMessageReportCallback)
						.dismissPostingDialog();*/
			}

		} else {
			// 重试下载3次
			SyncMsgEntry remove = syncMessage.remove(message.getMsgId());
			if (remove == null) {
				return;
			}
			LogUtil.d(TAG,
					"[onDownloadMessageComplete] download fail , retry ："
							+ remove.retryCount);
			retryDownload(remove);
		}
	}

	@Override
	public void onProgress(String msgId, int totalByte, int progressByte) {
		// 处理发送文件IM消息的时候进度回调 //download
		LogUtil.d(TAG, "[IMChattingHelper - onProgress] msgId: " + msgId
				+ " , totalByte: " + totalByte + " , progressByte:"
				+ progressByte);

	}

	/**
	 * 重试下载3次
	 * 
	 * @param entry
	 */
	private void retryDownload(SyncMsgEntry entry) {
		if (entry == null || entry.msg == null || entry.isRetryLimit()) {
			return;
		}
		entry.increase();
		// download ..
		if (mChatManager != null) {
			if (entry.thumbnail) {
				mChatManager.downloadThumbnailMessage(entry.msg, this);
			} else {
				mChatManager.downloadMediaMessage(entry.msg, this);
			}
		}
		syncMessage.put(entry.msg.getMsgId(), entry);
	}

	private synchronized void postDowloadMessageResult(ECMessage message) {
		if (message == null) {
			return;
		}
		if (message.getType() == ECMessage.Type.VOICE) {
			ECVoiceMessageBody voiceBody = (ECVoiceMessageBody) message
					.getBody();
			voiceBody.setDuration(VeryUtils.calculateVoiceTime(voiceBody
					.getLocalUrl()));
		} else if (message.getType() == ECMessage.Type.IMAGE) {
			Image thumbImgInfo = ImgInfoSqlManager.getInstance()
					.getThumbImgInfo(message);
			if (thumbImgInfo == null) {
				return;
			}
			ImgInfoSqlManager.getInstance().insertImageInfo(thumbImgInfo);
			BitmapFactory.Options options = VeryUtils
					.getBitmapOptions(new File(FileAccessor.getImagePathName(),
							thumbImgInfo.getThumbImgPath()).getAbsolutePath());
			message.setUserData("outWidth://" + options.outWidth
					+ ",outHeight://" + options.outHeight + ",THUMBNAIL://"
					+ message.getMsgId() + ",PICGIF://" + thumbImgInfo.isGif);
		}
		if (IMessageSqlManager.updateIMessageDownload(message) <= 0) {
			return;
		}
		if (mOnMessageReportCallback != null) {
			mOnMessageReportCallback.onMessageReport(null, message);
		}
		boolean showNotice = true;
		SyncMsgEntry remove = syncMessage.remove(message.getMsgId());
		if (remove != null) {
			showNotice = remove.showNotice;
			if (mOnMessageReportCallback != null && remove.msg != null) {
				ArrayList<ECMessage> msgs = new ArrayList<ECMessage>();
				msgs.add(remove.msg);
				mOnMessageReportCallback.onPushMessage(
						remove.msg.getSessionId(), msgs);
			}
		}
		if (showNotice)
			showNotification(message);
	}

	public class SyncMsgEntry {
		// 是否是第一次初始化同步消息
		boolean showNotice = false;
		boolean thumbnail = false;

		// 重试下载次数
		private int retryCount = 1;
		ECMessage msg;

		public SyncMsgEntry(boolean showNotice, boolean thumbnail,
				ECMessage message) {
			this.showNotice = showNotice;
			this.msg = message;
			this.thumbnail = thumbnail;
		}

		public void increase() {
			retryCount++;
		}

		public boolean isRetryLimit() {
			return retryCount >= 3;
		}
	}

	@Override
	public void onReceiveMessageNotify(ECMessageNotify msg) {
		if(msg.getNotifyType()==NotifyType.DELETE){
			ECMessageDeleteNotify deleteMsg=(ECMessageDeleteNotify) msg;
			IMessageSqlManager.updateMsgReadStatus(msg.getMsgId(), true);
			IMessageSqlManager.deleteLocalFileAfterFire(msg.getMsgId());
			if (mOnMessageReportCallback != null) {
				mOnMessageReportCallback.onMessageReport(null, null);
			}	
		}
		
	}

}
