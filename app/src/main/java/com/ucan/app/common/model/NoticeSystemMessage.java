package com.ucan.app.common.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;

import com.ucan.app.common.utils.VeryUtils;
import com.yuntongxun.ecsdk.im.group.ECGroupNoticeMessage;

public class NoticeSystemMessage extends ECGroupNoticeMessage {
    public static final Creator<NoticeSystemMessage> CREATOR = new Creator() {
        public final NoticeSystemMessage createFromParcel(Parcel var1) {
            return new NoticeSystemMessage(var1);
        }

        public final NoticeSystemMessage[] newArray(int var1) {
            return new NoticeSystemMessage[var1];
        }
    };
    /**
     * 消息ID
     */
    private String id;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 群组I的
     */
    private String groupId;
    /**
     * 管理员
     */
    private String admin;
    /**
     * 验证成员
     */
    private String member;
    private String groupName;
    private int isRead;
    /**
     * 是否需要确认
     */
    private int confirm;
    /**
     * 消息时间
     */
    private long dateCreated;

    public NoticeSystemMessage(ECGroupNoticeMessage.ECGroupMessageType type) {
        super(type);
    }

    public NoticeSystemMessage(Parcel var1) {
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the admin
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * @param admin the admin to set
     */
    public void setAdmin(String admin) {
        this.admin = admin;
    }

    /**
     * @return the member
     */
    public String getMember() {
        return member;
    }

    /**
     * @param member the member to set
     */
    public void setMember(String member) {
        this.member = member;
    }

    /**
     * @return the isRead
     */
    public int getIsRead() {
        return isRead;
    }

    /**
     * @param isRead the isRead to set
     */
    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    /**
     * @return the confirm
     */
    public int getConfirm() {
        return confirm;
    }

    /**
     * @param confirm the confirm to set
     */
    public void setConfirm(int confirm) {
        this.confirm = confirm;
    }

    /**
     * @return the dateCreated
     */
    public long getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setCursor(Cursor cursor) {
        this.id = cursor.getString(0);
        this.content = cursor.getString(1);
        this.admin = cursor.getString(2);
        this.confirm = cursor.getInt(3);
        this.groupId = cursor.getString(4);
        this.member = cursor.getString(5);
        this.dateCreated = cursor.getLong(6);
        this.groupName = cursor.getString(7);
    }

    public ContentValues buildContentValues() {
        ContentValues values = new ContentValues();
        values.put("declared", content);
        values.put("notice_id", VeryUtils.md5(System.currentTimeMillis() + ""));
        values.put("groupId", groupId);
        values.put("admin", admin);
        values.put("member", member);
        values.put("isRead", isRead);
        values.put("confirm", confirm);
        values.put("dateCreated", dateCreated);
        values.put("type", getType().ordinal());

        return values;
    }
}
