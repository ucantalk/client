package com.ucan.app.common.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;


import com.ucan.app.base.db.AbstractSQLManager;
import com.ucan.app.base.db.ContactSqlManager;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.helper.ContactHelper;

import java.util.regex.Pattern;

/**
 * 联系人信息

 */
public class Contact implements Parcelable {

    public static final Parcelable.Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }

        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }
    };

    private long id;
    /**联系人账号*/
    private String contactid;
    /**联系人昵称*/
    private String nickname;
    /**联系人类型*/
    private int type;
    /**联系人账号Token*/
    private String token;
    /**联系人子账号*/
    private String subAccount;
    /**联系人子账号Token*/
    private String subToken;
    /**备注*/
    private String remark;
    // Other
    private String[] qpName;
    private String jpNumber; //简拼对应的拨号键盘的数字
    private String jpName;
    private String qpNameStr;
    private String[] qpNumber; //保存拼音对应的拨号键盘的数字
    private long photoId;
    /**
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }
    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }
    /**
     * @param remark the remark to set
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
    /**
     * @return the contactid
     */
    public String getContactid() {
        return contactid;
    }
    /**
     * @param contactid the contactid to set
     */
    public void setContactid(String contactid) {
        this.contactid = contactid;
    }
    /**
     * @return the type
     */
    public int getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
    private Contact(Parcel in) {
        this.id  = in.readLong();
        this.contactid = in.readString();
        this.type = in.readInt();
        this.nickname = in.readString();
        this.subAccount= in.readString();
        this.subToken= in.readString();
        this.token= in.readString();
        this.remark= in.readString();
    }

    public String getSortKey() {
        if(jpName == null || jpName.trim().length() <= 0) {
            return "#";
        }
        String c = jpName.substring(0, 1);
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c).matches()) {
            return c.toUpperCase();
        } else {
            return "#";
        }
    }



    public ContentValues buildContentValues() {
        ContentValues values = new ContentValues();
        values.put(AbstractSQLManager.ContactsColumn.CONTACT_ID, this.contactid);
        values.put(AbstractSQLManager.ContactsColumn.type, this.type);
        values.put(AbstractSQLManager.ContactsColumn.USERNAME, this.nickname );
        values.put(AbstractSQLManager.ContactsColumn.SUBACCOUNT, this.subAccount );
        values.put(AbstractSQLManager.ContactsColumn.SUBTOKEN, this.subToken );
        values.put(AbstractSQLManager.ContactsColumn.TOKEN, this.token );
        values.put(AbstractSQLManager.ContactsColumn.REMARK, this.remark);
        return values;
    }

    public Contact() {

    }

    public void setClientUser(UserInfo user) {
        setContactid(user.getAccount());
        setNickname(user.getNickName());
        setRemark(ContactHelper.CONVER_PHONTO[ContactSqlManager.getIntRandom(4, 0)]);
    }

    public Contact(String contactId) {
        this.contactid = contactId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(this.contactid);
        dest.writeInt(this.type);
        dest.writeString(this.nickname);
        dest.writeString(this.subAccount);
        dest.writeString(this.subToken);
        dest.writeString(this.token);
        dest.writeString(this.remark);
    }






    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public String[] getQpName() {
        return qpName;
    }

    public void setQpName(String[] qpName) {
        this.qpName = qpName;
    }

    public void setQpNumber(String[] qpNumber) {
        this.qpNumber = qpNumber;
    }


    public void setJpNumber(String jpNumber) {
        this.jpNumber = jpNumber;
    }

    public void setJpName(String jpName) {
        this.jpName = jpName;
    }

    public String getQpNameStr() {
        return qpNameStr;
    }

    public void setQpNameStr(String qpNameStr) {
        this.qpNameStr = qpNameStr;
    }
}