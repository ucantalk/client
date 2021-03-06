
package com.ucan.app.common.helper;

import java.util.ArrayList;
import java.util.List;

import com.ucan.app.R;
import com.ucan.app.base.db.GroupMemberSqlManager;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.ToastUtil;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.im.ESpeakStatus;


/**
 * 群组成员同步接口
 */
public class GroupMemberHelper {

    public static final String TAG = "UCAN.GroupMemberHelper";
    private static GroupMemberHelper sInstence;
    public static GroupMemberHelper getInstance() {
        if(sInstence == null) {
            sInstence = new GroupMemberHelper();
        }
        return sInstence;
    }

    /**SDK 访问接口*/
    private ECGroupManager mGroupManager;
    /**群组成员同步完成回调*/
    private OnSynsGroupMemberListener mGroupMemberListener;
    private GroupMemberHelper() {
        mGroupManager = SDKCoreHelper.getUCGroupManager();
    }

    public static void synsGroupMember(final String groupId) {
        getGroupManager();
        if(getInstance().mGroupManager == null) {
            return ;
        }
        ECGroupManager groupManager = getInstance().mGroupManager;
        groupManager.queryGroupMembers(groupId, new ECGroupManager.OnQueryGroupMembersListener() {

            @Override
            public void onQueryGroupMembersComplete(ECError error,
                                                    List<ECGroupMember> members) {
                if(getInstance().isSuccess(error)) {
                    if(members == null || members.isEmpty()) {
                        GroupMemberSqlManager.delAllMember(groupId);
                    } else {

                        LogUtil.d(TAG , "[synsGroupMember] members size :" +members.size());
                        ArrayList<String> accounts = GroupMemberSqlManager.getGroupMemberAccounts(groupId);
                        ArrayList<String> ids = new ArrayList<String>();
                        for(ECGroupMember member :members) {
                            ids.add(member.getVoipAccount());
                        }

                        // 查找不是群组成员
                        if(accounts != null && !accounts.isEmpty()) {
                            for(String id : accounts) {
                                if(ids.contains(id)) {
                                    continue;
                                }
                                // 不是群组成员、从数据库删除
                                GroupMemberSqlManager.delMember(groupId, id);
                            }
                        }
                        GroupMemberSqlManager.insertGroupMembers(members);
                    }

                    getInstance().notify(groupId);
                }
            }

        });
    }

    /**
     * @param groupId
     */
    private void notify(final String groupId) {
        if(getInstance().mGroupMemberListener != null) {
            getInstance().mGroupMemberListener.onSynsGroupMember(groupId);
        }
    }

    /**
     * 邀请成员加入群组
     * @param groupId 群组ID
     * @param reason 邀请原因
     * @param confirm 是否需要对方确认
     * @param members 邀请的成员
     */
    public static void inviteMembers(String groupId ,String reason ,final ECGroupManager.InvitationMode confirm , String[] members) {
        getGroupManager();
        inviteMembers(groupId, reason, confirm, members, new ECGroupManager.OnInviteJoinGroupListener() {

            @Override
            public void onInviteJoinGroupComplete(ECError error, String groupId,
                                                  String[] members) {
                if (getInstance().isSuccess(error)) {
                    if (confirm == ECGroupManager.InvitationMode.FORCE_PULL) {
                        GroupMemberSqlManager.insertGroupMembers(groupId, members);
                    } else {
                        ToastUtil.showMessage(R.string.invite_wating_replay);
                    }
                } else {
                    ToastUtil.showMessage("邀请成员失败[" + error.errorCode + "]");
                }
                getInstance().notify(groupId);

            }
        });

   }

    public static void inviteMembers(String groupId ,String reason ,final ECGroupManager.InvitationMode confirm , String[] members , ECGroupManager.OnInviteJoinGroupListener l) {
        getGroupManager();
        getInstance().mGroupManager.inviteJoinGroup(groupId, reason, members, confirm, l);
    }

    /**
     * 将成员移除出群组
     * @param groupid 群组ID
     * @param member 移除出的群组成员
     */
    public static void removerMember(String groupid , String member) {
        getGroupManager();
        getInstance().mGroupManager.deleteGroupMember(groupid, member, new ECGroupManager.OnDeleteGroupMembersListener() {

            @Override
            public void onDeleteGroupMembersComplete(ECError error, String groupId, String members) {
                if (getInstance().isSuccess(error)) {
                    GroupMemberSqlManager.delMember(groupId, members);
                } else {
                    ToastUtil.showMessage("移除成员失败[" + error.errorCode + "]");
                }
                getInstance().notify(groupId);
            }
        });

    }

    /**
     * 设置群组成员禁言状态
     * @param groupId
     * @param member
     * @param enabled
     */
    public static void forbidMemberSpeakStatus(final String groupId , final String member ,final boolean enabled ) {
        getGroupManager();
        ESpeakStatus speakStatus = new ESpeakStatus();
        speakStatus.setOperation(enabled ? 2 : 1);
        getInstance().mGroupManager.forbidMemberSpeakStatus(groupId, member, speakStatus, new ECGroupManager.OnForbidMemberSpeakStatusListener() {
            @Override
            public void onForbidMemberSpeakStatusComplete(ECError error, String groupId, String member) {
                if (getInstance().isSuccess(error)) {
                    GroupMemberSqlManager.updateMemberSpeakState(groupId, member, enabled);
                } else {
                    ToastUtil.showMessage("设置失败[" + error.errorCode + "]");
                }
//                getInstance().notify(groupId);
            }

        });
    }


    public static void queryGroupMemberCard(final String groupId , final String member) {
        getGroupManager();
        getInstance().mGroupManager.queryMemberCard(member, groupId, new ECGroupManager.OnQueryMemberCardListener() {
            @Override
            public void onQueryMemberCardComplete(ECError error, ECGroupMember member) {
                if (getInstance().isSuccess(error)) {
                    if (member != null) {
                        LogUtil.d(TAG, "groupmember  " + member.toString());
                    }
                    return;
                }
                LogUtil.e(TAG, "query group member card fail " +
                        ", errorCode=" + error.errorCode);
            }
        });
    }

    public static void modifyGroupMemberCard(final String groupId , final String member) {
        getGroupManager();
        ECGroupMember groupMember = new ECGroupMember();
        groupMember.setBelong(groupId);
        groupMember.setVoipAccount(member);
        groupMember.setDisplayName("贾2");
        groupMember.setSex(2);
        groupMember.setRemark("詹季春改了贾政阳的名片");
        groupMember.setTel("18813192117");
        getInstance().mGroupManager.modifyMemberCard(groupMember, new ECGroupManager.OnModifyMemberCardListener() {
            @Override
            public void onModifyMemberCardComplete(ECError error, ECGroupMember member) {
                if (getInstance().isSuccess(error)) {
                    if (member != null) {
                        LogUtil.d(TAG, "groupmember  " + member.toString());
                    }
                    return;
                }
                LogUtil.e(TAG, "modify group member card fail " +
                        ", errorCode=" + error.errorCode);
            }

        });
    }


    public static void setGroupMessageOption(String groupid) {
        getGroupManager();

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

    public static void getGroupManager() {
        getInstance().mGroupManager = SDKCoreHelper.getUCGroupManager();
    }


    /**
     * 注入SDK群组成员同步回调
     * @param l
     */
    public static void addListener(OnSynsGroupMemberListener l) {
        getInstance().mGroupMemberListener = l;
    }

    public interface OnSynsGroupMemberListener{
        void onSynsGroupMember(String groupId);
    }
}
 