package com.ucan.app.common.helper;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.db.ContactSqlManager;
import com.ucan.app.base.db.GroupMemberSqlManager;
import com.ucan.app.base.db.GroupSqlManager;
import com.ucan.app.base.db.IMessageSqlManager;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.ToastUtil;
import com.ucan.app.common.utils.VeryUtils;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.ECGroupManager.OnModifyMemberCardListener;
import com.yuntongxun.ecsdk.ECGroupManager.OnQueryMemberCardListener;
import com.yuntongxun.ecsdk.ECGroupManager.Target;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECAckType;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.im.ECGroupOption;

 
public class GroupHelper {
    private static final String TAG  = "UCAN.GroupHelper";

    public static final String ACTION_SYNC_GROUP = "com.ucan.app.intent.ACTION_SYNC_GROUP";
    public static final String PRICATE_CHATROOM = "@priategroup.com";
    private static GroupHelper sInstance;
    private ECGroupManager mGroupManager;
    private List<String> mGroupIds;
    private Callback mCallback;
    private boolean isSync = false;

    private GroupHelper() {
        mGroupManager = SDKCoreHelper.getUCGroupManager();
        countGroups();
    }

    private static GroupHelper getInstance() {
        if(sInstance == null) {
            sInstance = new GroupHelper();
        }
        return sInstance;
    }

    /**
     * 同步所有的群组
     */
    private void countGroups() {
        mGroupIds = GroupSqlManager.getAllGroupId();
    }

    /**
     * 开始网络同步群组列表信息
     */
    @SuppressWarnings("deprecation")
	public static void syncGroup(Callback callback) {
        getInstance().mGroupManager = SDKCoreHelper.getUCGroupManager();
        if(getInstance().mGroupManager == null || getInstance().isSync) {
            LogUtil.e(TAG, "SDK not ready or isSync " + getInstance().isSync);
            return ;
        }
        getInstance().isSync = true;
        getInstance().mCallback = callback;
        
        
        getInstance().mGroupManager.queryOwnGroups(Target.GROUP ,new ECGroupManager.OnQueryOwnGroupsListener() {

            @Override
            public void onQueryOwnGroupsComplete(ECError error, List<ECGroup> groups) {
                if(getInstance().isSuccess(error)) {
                    if (groups == null || groups.isEmpty()) {
                        GroupSqlManager.delALLGroup();
                    } else {
                        LogUtil.d(TAG , "[syncGroup] groups size :" +groups.size());
                        List<String> allGroupIdByJoin = GroupSqlManager.getAllGroupIdBy(true);
                        ArrayList<String> ids = new ArrayList<String>();
                        for (ECGroup group : groups) {
                            ids.add(group.getGroupId());
                        }

                        // 查找不是我的群组
                        if (!allGroupIdByJoin.isEmpty()) {
                            for (String id : allGroupIdByJoin) {
                                if (ids.contains(id)) {
                                    continue;
                                }
                                // 不是我的群组
                                GroupSqlManager.updateUNJoin(id);
                            }
                        }
                        GroupSqlManager.insertGroupInfos(groups, 1);
                    }
                    getInstance().isSync = false;
                    // 更新公共所有群组
                    // syncPublicGroups();
                    if (getInstance().mCallback != null) {
                        getInstance().mCallback.onSyncGroup();
                    }
                    if (AppManager.getContext() != null) {
                        AppManager.getContext().sendBroadcast(new Intent((ACTION_SYNC_GROUP)));
                    }
                    return ;
                }
                onErrorCallback(error.errorCode, "同步群组失败");
            }
        });
    }
    
    
    public static void syncDiscussionGroup(Callback callback) {
        getInstance().mGroupManager = SDKCoreHelper.getUCGroupManager();
        if(getInstance().mGroupManager == null ) {
            LogUtil.e(TAG, "SDK not ready or isSync " + getInstance().isSync);
            return ;
        }
//        getInstance().isSync = true;
        getInstance().mCallback = callback;
        
        
        getInstance().mGroupManager.queryOwnGroups(Target.DISCUSSION ,new ECGroupManager.OnQueryOwnGroupsListener() {

            @Override
            public void onQueryOwnGroupsComplete(ECError error, List<ECGroup> groups) {
                if(getInstance().isSuccess(error)) {
                    if (groups == null || groups.isEmpty()) {
                        GroupSqlManager.delALLDisGroup();
                    } else {
                        LogUtil.d(TAG , "[syncGroup] groups size :" +groups.size());
                        List<String> allGroupIdByJoin = GroupSqlManager.getAllDisGroupIdBy(true);
                        ArrayList<String> ids = new ArrayList<String>();
                        for (ECGroup group : groups) {
                            ids.add(group.getGroupId());
                        }

                        // 查找不是我的群组
                        if (!allGroupIdByJoin.isEmpty()) {
                            for (String id : allGroupIdByJoin) {
                                if (ids.contains(id)) {
                                    continue;
                                }
                                // 不是我的群组
                                GroupSqlManager.updateUNJoin(id);
                            }
                        }
                        GroupSqlManager.insertDisGroupInfos(groups, 1);
                    }
                    getInstance().isSync = false;
                    // 更新公共所有群组
                    // syncPublicGroups();
                    if (getInstance().mCallback != null) {
                        getInstance().mCallback.onSyncGroup();
                    }
                    if (AppManager.getContext() != null) {
                        AppManager.getContext().sendBroadcast(new Intent((ACTION_SYNC_GROUP)));
                    }
                    return ;
                }
                onErrorCallback(error.errorCode, "同步讨论组失败");
            }
        });
    }
    


    /**
     * 同步群组信息
     * @param groupId
     */
    public static void syncGroupInfo(final String groupId) {
        ECGroupManager groupManager = SDKCoreHelper.getUCGroupManager();
        if(groupManager == null) {
            return ;
        }
        groupManager.getGroupDetail(groupId, new ECGroupManager.OnGetGroupDetailListener() {

            @Override
            public void onGetGroupDetailComplete(ECError error, ECGroup group) {
                if (getInstance().isSuccess(error)) {
                    if (group == null) {
                        return;
                    }

                    GroupSqlManager.updateGroup(group);
                    if (getInstance().mCallback != null) {
                        getInstance().mCallback.onSyncGroupInfo(groupId);
                    }
                    return;
                }
                onErrorCallback(error.errorCode, "同步群组信息失败");
            }
        });
    }

    /**
     * 解散群组
     * @param groupId
     */
    public static void disGroup(String groupId) {
        getGroupManager();
        getInstance().mGroupManager.deleteGroup(groupId, new ECGroupManager.OnDeleteGroupListener() {

            @Override
            public void onDeleteGroupComplete(ECError error, String groupId) {
                if (getInstance().isSuccess(error)) {
                    GroupMemberSqlManager.delAllMember(groupId);
                    IMessageSqlManager.deleteChattingMessage(groupId);
                    GroupSqlManager.delGroup(groupId);
                    if (getInstance().mCallback != null) {
                        getInstance().mCallback.onGroupDel(groupId);
                    }
                    return;
                }
                getInstance().mCallback.onError(error);
                onErrorCallback(error.errorCode, "解散群组失败");
            }
        });
    }

    /**
     * 退出群组
     * @param groupId
     */
    public static void quitGroup(String groupId) {
        getGroupManager();
        getInstance().mGroupManager.quitGroup(groupId, new ECGroupManager.OnQuitGroupListener() {

            @Override
            public void onQuitGroupComplete(ECError error, String groupId) {
                if (getInstance().isSuccess(error)) {
                    GroupMemberSqlManager.delAllMember(groupId);
                    //GroupSqlManager.updateUNJoin(groupId);
                    GroupSqlManager.delGroup(groupId);
                    IMessageSqlManager.deleteChattingMessage(groupId);
                    getInstance().mCallback.onGroupDel(groupId);
                    return;
                }
                getInstance().mCallback.onError(error);
                onErrorCallback(error.errorCode, "退出群组失败");
            }
        });

    }

    /**
     * 修改群组信息
     * @param group
     */
    public static void modifyGroup(ECGroup group) {
        getGroupManager();
        getInstance().mGroupManager.modifyGroup(group, new ECGroupManager.OnModifyGroupListener() {
            @Override
            public void onModifyGroupComplete(ECError error, ECGroup group) {
                if (getInstance().isSuccess(error)) {
                    GroupSqlManager.updateGroup(group);
                    if (getInstance().mCallback != null) {
                        getInstance().mCallback.onSyncGroupInfo(group.getGroupId());
                    }
                    return;
                }
                onErrorCallback(error.errorCode, "修改群组信息失败");
            }

        });
    }

    private static void onErrorCallback(int code ,String msg) {
        if (getInstance().mCallback != null) {
            getInstance().mCallback.onError(new ECError(code , msg));
        }
        ToastUtil.showMessage(msg + "[" + code + "]");
    }

    /**
     * 申请加入群组
     */
    public static void applyGroup(String groupId , String declare , final OnApplyGroupCallbackListener l) {
        getGroupManager();
        getInstance().mGroupManager.joinGroup(groupId, declare, new ECGroupManager.OnJoinGroupListener() {

            @Override
            public void onJoinGroupComplete(ECError error, String groupId) {
                if (getInstance().isSuccess(error) ||
                        // 群组成员已经存在了
                        SdkErrorCode.MEMBER_ALREADY_EXIST == error.errorCode) {

                    if (l != null) {
                        l.onApplyGroup(true);
                    }
                    return;
                }
                if (SdkErrorCode.GROUP_NON_EXISTENT == error.errorCode) {
                    // 群组不存在
                    GroupSqlManager.delGroup(groupId);
                    IMessageSqlManager.deleteAllBySession(groupId);
                }
                if (l != null) {
                    if (error != null) {
                        ToastUtil.showMessage("申请加入群组失败[" + error.errorCode + "]");
                    }
                    l.onApplyGroup(false);
                }
            }
        });
    }


    /**
     * 创建私有群组
     * @param member
     */
    public static void doCreateGroup(final String[] member , final ECGroupManager.OnInviteJoinGroupListener l) {
        // 构建群组参数
        ECGroup group = new ECGroup();
        // 设置群组名称
        group.setName(AppManager.getUserId() + PRICATE_CHATROOM);
        // 设置群组公告
        group.setDeclare("");
        // 设置群组类型，如：临时群组（100人）
        group.setGroupType(0);
        // 设置群组验证权限，如：需要身份验证2
        group.setPermission(ECGroup.Permission.NEED_AUTH);
        // 设置群组创建者（可以不设置，服务器默认接口调用者为创建者）
        group.setOwner(AppManager.getUserId());

        getGroupManager();
        getInstance().mGroupManager.createGroup(group, new ECGroupManager.OnCreateGroupListener() {

            @Override
            public void onCreateGroupComplete(ECError error, ECGroup group) {
                if(getInstance().isSuccess(error)) {
                    if(group.getName() != null && group.getName().endsWith(PRICATE_CHATROOM)) {
                        ArrayList<String> contactName = ContactSqlManager.getContactName(member);
                        String chatroomName = VeryUtils.listToString(contactName, ",");
                        group.setName(chatroomName);
                    }
                    GroupSqlManager.insertGroup(group, true, false,false);
                    GroupMemberHelper.inviteMembers(group.getGroupId(), "", ECGroupManager.InvitationMode.FORCE_PULL, member, l);
                    return ;
                }
                onErrorCallback(error.errorCode , "创建群组失败");
            }
        });
    }
    
    
    
    public static void queryGroupCard(String userId,String groupId,final GroupCardCallBack l){
    	getGroupManager();
    	getInstance().mGroupManager.queryMemberCard(userId, groupId, new OnQueryMemberCardListener() {
			
			@Override
			public void onQueryMemberCardComplete(ECError error, ECGroupMember member) {
				// TODO Auto-generated method stub
				
				 if (getInstance().isSuccess(error)) {
	                    if (l != null) {
	                        l.onQueryGroupCardSuccess(member);
	                    }
	                    return;
	                }
				 
				 if(l!=null){
					 l.onQueryGroupCardFailed(error);
				 }
				 
			    }
		});
    }
    
    public static void modifyGroupCard(ECGroupMember member,final GroupCardCallBack l){
    	getGroupManager();
    	getInstance().mGroupManager.modifyMemberCard(member, new OnModifyMemberCardListener() {
			
			@Override
			public void onModifyMemberCardComplete(ECError error, ECGroupMember me) {

				if (getInstance().isSuccess(error)) {
                    if (l != null) {
                        l.onModifyGroupCardSuccess();
                    }
                    return;
                }
				
				 if(l!=null){
					 l.onModifyGroupCardFailed(error);
				 }
			 
		    
				
			}
		});
    }
    
    


    public static void operationGroupApplyOrInvite(boolean inviteAck ,String groupId , String member , ECAckType ackType , final OnAckGroupServiceListener listener) {
        getGroupManager();
        if(!inviteAck) {
            getInstance().mGroupManager.ackJoinGroupRequest(groupId , member , ackType , new ECGroupManager.OnAckJoinGroupRequestListener() {
                @Override
                public void onAckJoinGroupRequestComplete(ECError error, String groupId, String member) {
                    if(getInstance().isSuccess(error)) {
                        if(listener != null) {
                            listener.onAckGroupService(true);
                        }
                        return ;
                    }
                    onErrorCallback(error.errorCode , "操作失败");
                }
            });
            return ;
        }

        
        getInstance().mGroupManager.ackInviteJoinGroupRequest(groupId, ackType,member, new ECGroupManager.OnAckInviteJoinGroupRequestListener() {
            @Override
            public void onAckInviteJoinGroupRequestComplete(ECError error, String groupId) {
                if(getInstance().isSuccess(error)) {

                    if(listener != null) {
                        listener.onAckGroupService(true);
                    }
                    return ;
                }
                onErrorCallback(error.errorCode , "操作失败");
            }

        });

    }

    public static void setGroupMessageOption(final ECGroupOption option) {
        getGroupManager();
        getInstance().mGroupManager.setGroupMessageOption(option, new ECGroupManager.OnSetGroupMessageOptionListener() {
            @Override
            public void onSetGroupMessageOptionComplete(ECError error, String groupId) {
                if (getInstance().isSuccess(error)) {
                    GroupSqlManager.updateGroupNofity(option.getRule().ordinal(), option.getGroupId());
                    if (getInstance().mCallback != null) {
                        getInstance().mCallback.onSyncGroupInfo(option.getGroupId());
                    }
                    return;
                }
                onErrorCallback(error.errorCode, "操作失败");
            }
        });
    }
    public static void setGroupIsAnonymity(final String groupId,final boolean isAnonymity) {
    	getGroupManager();
    	
    	
    }

    public static void setGroupMessageOption(final ECGroupOption option , final GroupOptionCallback listener) {
        
    	getGroupManager();
        getInstance().mGroupManager.setGroupMessageOption(option, new ECGroupManager.OnSetGroupMessageOptionListener() {
            @Override
            public void onSetGroupMessageOptionComplete(ECError error, String groupId) {
                if(getInstance().isSuccess(error)) {
                    GroupSqlManager.updateGroupNofity(option.getRule().ordinal() , option.getGroupId());
                    if(listener != null) {
                        listener.onComplete(option.getGroupId());
                    }
                    return ;
                }
                if (listener != null) {
                    listener.onError(error);
                }
                ToastUtil.showMessage("操作失败[" + error.errorCode + "]");
            }
        });
    }


    /**
     * 请求是否成功
     * @param error
     * @return
     */
    private boolean isSuccess(ECError error) {
        if(error.errorCode == SdkErrorCode.REQUEST_SUCCESS)  {
            return true;
        }
        return false;
    }

    private static void getGroupManager() {
        getInstance().mGroupManager = SDKCoreHelper.getUCGroupManager();
    }

    /**
     * @param callback
     */
    public static void addListener(Callback callback) {
        getInstance().mCallback = callback;
    }

    public interface Callback {
        void onSyncGroup();
        void onSyncGroupInfo(String groupId);
        void onGroupDel(String groupId);
        void onError(ECError error);
        
        void onUpdateGroupAnonymitySuccess(String groupId,boolean isAnonymity);
        
        
    }
    
    public interface GroupCardCallBack{

		void onQueryGroupCardSuccess(ECGroupMember member);
		void onQueryGroupCardFailed(ECError error);

		void onModifyGroupCardSuccess();
		void onModifyGroupCardFailed(ECError error);
    	
    	
    }

    public interface GroupOptionCallback {
        void onComplete(String groupId);
        void onError(ECError error);
    }

    public interface OnApplyGroupCallbackListener {
        void onApplyGroup(boolean success);
    }

    public interface OnAckGroupServiceListener {
        void onAckGroupService(boolean success);
    }
}
