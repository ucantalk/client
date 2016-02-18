package com.ucan.app.common.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.ucan.app.base.db.ImgInfoSqlManager;

public class ViewImage implements Parcelable {

	public static final Parcelable.Creator<ViewImage> CREATOR = new Parcelable.Creator<ViewImage>() {
		public ViewImage createFromParcel(Parcel in) {
			return new ViewImage(in);
		}

		public ViewImage[] newArray(int size) {
			return new ViewImage[size];
		}
	};

	private int index;
	private String msgLocalId;
	private String thumbnailurl;
	private String picurl;
	private boolean isDownload = false;
	private boolean isGif = false;

	public ViewImage(int index, String thumb, String url) {
		this.index = index;
		this.thumbnailurl = thumb;
		this.picurl = url;
		checkGif();
	}

	private ViewImage(Parcel in) {
		this.index = in.readInt();
		this.msgLocalId = in.readString();
		this.thumbnailurl = in.readString();
		this.picurl = in.readString();
		this.isDownload = (in.readByte() != 0);
		this.isGif = (in.readByte() != 0);

	}

	public ViewImage(String thumb, String url) {
		this(0, thumb, url);
	}

	public ViewImage(Cursor cursor) {
		setCursor(cursor);
	}

	public int getIndex() {
		return index;
	}

	public String getThumbnailurl() {
		return thumbnailurl;
	}

	public void setThumbnailurl(String thumbnailurl) {
		this.thumbnailurl = thumbnailurl;
		checkGif();
	}

	public String getPicurl() {
		return picurl;
	}

	public void setPicurl(String picurl) {
		this.picurl = picurl;
		checkGif();
	}

	private void checkGif() {
		if (!isGif && this.thumbnailurl != null) {
			isGif = this.thumbnailurl.endsWith(".gif");
		}
		if (!isGif && this.picurl != null) {
			isGif = this.picurl.endsWith(".gif");
		}
	}

	public String getMsgLocalId() {
		return msgLocalId;
	}

	public void setMsgLocalId(String msgLocalId) {
		this.msgLocalId = msgLocalId;
	}

	public void setCursor(Cursor cursor) {
		this.index = cursor.getInt(cursor
				.getColumnIndex(ImgInfoSqlManager.ImgInfoColumn.ID));
		this.picurl = cursor.getString(cursor
				.getColumnIndex(ImgInfoSqlManager.ImgInfoColumn.BIG_IMGPATH));
		this.msgLocalId = cursor.getString(cursor
				.getColumnIndex(ImgInfoSqlManager.ImgInfoColumn.MSG_LOCAL_ID));
		this.thumbnailurl = cursor.getString(cursor
				.getColumnIndex(ImgInfoSqlManager.ImgInfoColumn.THUMBIMG_PATH));
	}

	public boolean isDownload() {
		return isDownload;
	}

	public void setIsDownload(boolean isDownload) {
		this.isDownload = isDownload;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.index);
		dest.writeString(this.msgLocalId);
		dest.writeString(this.thumbnailurl);
		dest.writeString(this.picurl);
		dest.writeByte((byte) (this.isDownload ? 1 : 0));
		dest.writeByte((byte) (this.isGif ? 1 : 0));
	}

	public boolean isGif() {
		return isGif;
	}

	public void setIsGif(boolean isGif) {
		this.isGif = isGif;
	}
}
