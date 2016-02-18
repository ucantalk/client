package com.ucan.app.base.domain;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {

    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
    /**
     * 用户唯一Id码<h1>32位uuid</h1>
     */
    private String uid;

    /**
     * 用户账号:<h1>mobile</h1>
     */
    private String account;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期 <h1>YYYY-MM-DD</h1>
     */
    private String birthday;

    /**
     * 国籍(籍贯)
     */

    private String hometown;

    /**
     * 用户头像地址
     */
    private String avatarurl;
    /**
     * 用户水平等级
     */
    private int level;


    /**
     * 用户类别：<br/>
     * 0-普通 <br/>
     * 1-付费<br/>
     */
    private int type;

    /**
     * 用户token
     */
    private String token;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getnickname() {
        return nickname;
    }

    public void setnickname(String nickname) {
        this.nickname = nickname;
    }

    public String getbirthday() {
        return birthday;
    }

    public void setbirthday(String birthday) {
        this.birthday = birthday;
    }

    public String gethometown() {
        return hometown;
    }

    public void sethometown(String hometown) {
        this.hometown = hometown;
    }

    public String getavatarurl() {
        return avatarurl;
    }

    public void setavatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo(String account) {
        this.account = account;
    }

    public UserInfo() {

    }

    private UserInfo(Parcel in) {
        this.uid = in.readString();
        this.account = in.readString();
        this.nickname = in.readString();
        this.gender = in.readString();
        this.birthday = in.readString();
        this.hometown = in.readString();
        this.avatarurl = in.readString();
        this.level = in.readInt();
        this.type = in.readInt();
        this.token = in.readString();
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", this.uid);
            jsonObject.put("account", this.account);
            jsonObject.put("nickname", this.nickname);
            jsonObject.put("gender", this.gender);
            jsonObject.put("birthday", this.birthday);
            jsonObject.put("hometown", this.hometown);
            jsonObject.put("level", this.level);
            jsonObject.put("type", this.type);
            jsonObject.put("token", this.token);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return jsonObject.toString();
        }
       /* StringBuilder sb = new StringBuilder();
        sb.append("\"UserInfo\":{");
        sb.append("\"uid\":\"").append(uid).append("\",");
        sb.append("\"account\":\"").append(account).append("\",");
        sb.append("\"password\":\"").append(password).append("\",");
        sb.append("\"nickname\":\"").append(nickname).append("\",");
        sb.append("\"gender\":\"").append(gender).append("\",");
        sb.append("\"birthday\":\"").append(birthday).append("\",");
        sb.append("\"hometown\":\"").append(hometown).append("\",");
        sb.append("\"avatarurl\":\"").append(avatarurl).append("\",");
        sb.append("\"level\":").append(level).append(",");
        sb.append("\"type\":").append(type).append(",");
        sb.append("\"token\":\"").append(token).append("\"");
        sb.append("}");*/

    }

    public UserInfo from(String input) {
        JSONObject object = null;
        try {
            object = new JSONObject(input);
            if (object.has("uid")) {
                this.uid = object.getString("uid");
            }
            if (object.has("account")) {
                this.account = object.getString("account");
            }
            if (object.has("nickname")) {
                this.nickname = object.getString("nickname");
            }
            if (object.has("gender")) {
                this.gender = object.getString("gender");
            }
            if (object.has("birthday")) {
                this.birthday = object.getString("birthday");
            }
            if (object.has("hometown")) {
                this.hometown = object.getString("hometown");
            }
            if (object.has("avatarurl")) {
                this.avatarurl = object.getString("avatarurl");
            }
            if (object.has("level")) {
                this.level = object.getInt("level");
            }
            if (object.has("type")) {
                this.type = object.getInt("type");
            }
            if (object.has("token")) {
                this.token = object.getString("token");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.account);
        dest.writeString(this.nickname);
        dest.writeString(this.gender);
        dest.writeString(this.birthday);
        dest.writeString(this.hometown);
        dest.writeString(this.avatarurl);
        dest.writeInt(this.level);
        dest.writeInt(this.type);
        dest.writeString(this.token);


    }

}
