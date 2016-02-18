package com.ucan.app.base.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;

import com.ucan.app.common.model.Image;
import com.ucan.app.common.model.ViewImage;
import com.ucan.app.common.utils.DateUtil;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.FileUtil;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.VeryUtils;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECFileMessageBody;
import com.yuntongxun.ecsdk.im.ECImageMessageBody;


/**
 * 图片保存
 */
public class ImgInfoSqlManager extends AbstractSQLManager {

	public HashMap<String, Bitmap> imgThumbCache = new HashMap<String, Bitmap>(20);
	private static int column_index = 1;
	
	public static ImgInfoSqlManager mInstance;
	public static ImgInfoSqlManager getInstance() {
		if(mInstance == null) {
			mInstance = new ImgInfoSqlManager();
		}
		return mInstance;
	}
	
	static final String TABLES_NAME_IMGINFO = "imginfo";


	public List<ViewImage> getViewImageInfos(List<String> msgids) {
		StringBuilder where = new StringBuilder();
		if(msgids != null && !msgids.isEmpty()) {
			where.append(" where " + ImgInfoColumn.MSG_LOCAL_ID  + " IN (");
			for(int i = 0; i < msgids.size() ; i++) {
				if(msgids.get(i) == null) {
					continue ;
				}
				String id = msgids.get(i);
				where.append("'" + id + "'");
				if(i != msgids.size() - 1) {
					where.append(",");
				}
			}
			where.append(") ");
		}
		String sql = "select id , msglocalid ,bigImgPath , thumbImgPath from " + TABLES_NAME_IMGINFO + where.toString() + " ORDER BY id ,msglocalid ASC";
		Cursor cursor = sqliteDB().rawQuery(sql , null);
		List<ViewImage> urls = null;
		if(cursor !=null && cursor.getCount() > 0) {
			urls = new ArrayList<ViewImage>();
			while(cursor.moveToNext()) {
				urls.add(new ViewImage(cursor));
			}
		}
		return urls;
	}

	public String getAllmsgid() {
		return null;
	}

	public class ImgInfoColumn extends BaseColumn{
		
		public static final String MSGSVR_ID = "msgSvrId";
		public static final String OFFSET = "offset";
		public static final String TOTALLEN ="totalLen";
		public static final String BIG_IMGPATH = "bigImgPath";
		public static final String THUMBIMG_PATH = "thumbImgPath";
		public static final String CREATE_TIME = "createtime";
		public static final String STATUS = "status";
		public static final String MSG_LOCAL_ID = "msglocalid";
		public static final String NET_TIMES = "nettimes";

	}
	
	private ImgInfoSqlManager() {
		Cursor cursor = sqliteDB().query(TABLES_NAME_IMGINFO, null, null, null, null, null, ImgInfoColumn.ID + " ASC ");
		if ((cursor.getCount() > 0) && (cursor.moveToLast())) {
			column_index = 1 + cursor.getInt(cursor.getColumnIndex(ImgInfoColumn.ID));
		}
		cursor.close();
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "loading new img id:" + column_index);
	}
	
	public long insertImageInfo(Image imageModel) {
		if(imageModel == null) {
			return -1;
		}
		ContentValues buildContentValues = imageModel.buildContentValues();
		if(buildContentValues.size() == 0) {
			return -1;
		}
		try {
			return sqliteDB().insert(TABLES_NAME_IMGINFO, null, buildContentValues);
		} catch (Exception e) {
			LogUtil.e(TAG, "insert imgInfo error = " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * 
	 * @param imageModel
	 * @return
	 */
	public long updateImageInfo(Image imageModel) {
		if(imageModel == null) {
			return -1;
		}
		ContentValues buildContentValues = imageModel.buildContentValues();
		if(buildContentValues.size() == 0) {
			return -1;
		}
		try {
			String where = ImgInfoColumn.ID + " = " + imageModel.getId();
			return sqliteDB().update(TABLES_NAME_IMGINFO, buildContentValues, where, null);
		} catch (Exception e) {
			LogUtil.e(TAG, "insert imgInfo error = " + e.getMessage());
		}
		return -1;
	}
	
	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public Image createImgInfo(String filePath) {
		
		if(!FileUtil.checkFile(filePath) || FileAccessor.getImagePathName() == null) {
			return null;
		}
		
		int bitmapDegrees = VeryUtils.getBitmapDegrees(filePath);
		String fileNameMD5 = VeryUtils.md5(System.currentTimeMillis() + filePath);
		String bigFileFullName = fileNameMD5 + ".jpg";
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "original img path = " + filePath);
		
		Options bitmapOptions = VeryUtils.getBitmapOptions(filePath);
		String authorityDir = FileAccessor.getImagePathName().getAbsolutePath();
		if((FileUtil.decodeFileLength(filePath) > 204800)
				|| (bitmapOptions != null && (((bitmapOptions.outHeight > 960) || (bitmapOptions.outWidth > 960))))) {
			File file = new File(authorityDir);
			if(!file.exists()) {
				file.mkdirs();
			}
			
			if(!VeryUtils.createThumbnailFromOrig(filePath, 960, 960, Bitmap.CompressFormat.JPEG, 70, FileAccessor.getImagePathName().getAbsolutePath(), fileNameMD5)) {
				return null;
			}
			FileAccessor.renameTo(authorityDir+File.separator, fileNameMD5, bigFileFullName);
		} else {
			// file size small.
			FileUtil.copyFile(authorityDir, fileNameMD5, ".jpg", FileUtil.readFlieToByte(filePath, 0, FileUtil.decodeFileLength(filePath)));
		}
		if(bitmapDegrees != 0 && !VeryUtils.rotateCreateBitmap(authorityDir +File.separator+ bigFileFullName, bitmapDegrees, Bitmap.CompressFormat.JPEG, authorityDir, bigFileFullName)) {
			return null;
		}
		LogUtil.d(TAG, "insert: compressed bigImgPath = " + bigFileFullName);
		String thumbName = VeryUtils.md5(fileNameMD5 + System.currentTimeMillis());
		File file = new File(authorityDir);
		if(!file.exists()) {
			file.mkdirs();
		}
		if(!VeryUtils.createThumbnailFromOrig(authorityDir +File.separator+ bigFileFullName, 100, 100, Bitmap.CompressFormat.JPEG, 60, authorityDir, thumbName)) {
			return null;
		}
		LogUtil.d(TAG, "insert: thumbName = " + thumbName);
		Image imageModel = new Image();
		column_index += 1;
		imageModel.setId(column_index);
		imageModel.setBigImgPath(bigFileFullName);
		imageModel.setThumbImgPath(thumbName);
		imageModel.setCreatetime((int) DateUtil.getCurrentTime());
		imageModel.setTotalLen(FileUtil.decodeFileLength(filePath));
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "insert: compress img size = " + imageModel.getTotalLen());
		return imageModel;
	}

	/**
	 *
	 * @param filePath
	 * @return
	 */
	public Image createGIFImgInfo(String filePath) {

		if(!FileUtil.checkFile(filePath) || FileAccessor.getImagePathName() == null) {
			return null;
		}
		String fileNameMD5 = VeryUtils.md5(System.currentTimeMillis() + filePath);
		String bigFileFullName = fileNameMD5 + ".gif";
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "original img path = " + filePath);
		String authorityDir = FileAccessor.getImagePathName().getAbsolutePath();
		FileUtil.copyFile(authorityDir, fileNameMD5, ".gif", FileUtil.readFlieToByte(filePath, 0, FileUtil.decodeFileLength(filePath)));
		LogUtil.d(TAG, "insert: compressed bigImgPath = " + bigFileFullName);
		String thumbName = VeryUtils.md5(fileNameMD5 + System.currentTimeMillis());
		File file = new File(authorityDir);
		if(!file.exists()) {
			file.mkdirs();
		}
		if(!VeryUtils.createThumbnailFromOrig(authorityDir +File.separator+ bigFileFullName, 100, 100, Bitmap.CompressFormat.JPEG, 60, authorityDir, thumbName)) {
			return null;
		}
		LogUtil.d(TAG, "insert: thumbName = " + thumbName);
		Image imageModel = new Image();
		column_index += 1;
		imageModel.setId(column_index);
		imageModel.setBigImgPath(bigFileFullName);
		imageModel.setThumbImgPath(thumbName);
		imageModel.setCreatetime((int) DateUtil.getCurrentTime());
		imageModel.setTotalLen(FileUtil.decodeFileLength(filePath));
		imageModel.isGif = true;
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "insert: compress img size = " + imageModel.getTotalLen());
		return imageModel;
	}

    /**
     * 接收图片生成缩略图
     * @param msg
     * @return
     */
	public Image getThumbImgInfo(ECMessage msg) {
		ECImageMessageBody body = (ECImageMessageBody) msg.getBody();
		if(TextUtils.isEmpty(body.getLocalUrl()) || !new File(body.getLocalUrl()).exists()) {
			return null;
		}
		LogUtil.d(TAG, "insert: thumbName = " + body.getFileName());
		Image imageModel = new Image();
		column_index += 1;
		imageModel.setId(column_index);
		if(!TextUtils.isEmpty(body.getThumbnailFileUrl())) {
			imageModel.setBigImgPath(body.getRemoteUrl());
			imageModel.setThumbImgPath(new File(body.getLocalUrl()).getName());
        } else {
			imageModel.setBigImgPath(new File(body.getLocalUrl()).getName());
            String filePath = body.getLocalUrl();
            String imageName = filePath.substring(filePath.lastIndexOf("/") + 1);
            String thumbName = VeryUtils.md5((imageName + System.currentTimeMillis()));
            String thumbNameDir = FileAccessor.getImagePathName().getAbsolutePath();
            if(!VeryUtils.createThumbnailFromOrig(filePath, 100, 100, Bitmap.CompressFormat.JPEG, 60, thumbNameDir, thumbName)) {
                return null;
            }
			imageModel.setThumbImgPath(thumbName);
        }
		/*if(body.getRemoteUrl().contains("_thumbnail")) {
			imgInfo.setBigImgPath(body.getRemoteUrl().replace("_thumbnail", ""));
		} else {
			imgInfo.setBigImgPath(null);
		}*/
		imageModel.isGif = body.getRemoteUrl().endsWith(".gif");
		imageModel.setMsglocalid(msg.getMsgId());
		imageModel.setCreatetime((int)DateUtil.getCurrentTime());
		imageModel.setTotalLen(FileUtil.decodeFileLength(body.getLocalUrl()));
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "insert: compress img size = " + imageModel.getTotalLen());
		return imageModel;
	}
    /**
     * 接收图片生成缩略图
     * @return
     */
    public Image getThumbImgInfo2(ECMessage msg) {

        ECFileMessageBody body = (ECFileMessageBody) msg.getBody();
        if(TextUtils.isEmpty(body.getLocalUrl()) || !new File(body.getLocalUrl()).exists()) {
            return null;
        }
        String filePath = body.getLocalUrl();
        String imageName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String thumbName = VeryUtils.md5((imageName + System.currentTimeMillis()));
        String thumbNameDir = FileUtil.getMD5FileDir(FileAccessor.IMESSAGE_IMAGE, thumbName);
        File file = new File(thumbNameDir);
        if(!file.exists()) {
            file.mkdirs();
        }
        if(!VeryUtils.createThumbnailFromOrig(filePath, 100, 100, Bitmap.CompressFormat.JPEG, 60, thumbNameDir, thumbName)) {
            return null;
        }
        LogUtil.d(TAG, "insert: thumbName = " + thumbName);
        Image imageModel = new Image();
        column_index += 1;
		imageModel.setId(column_index);
		imageModel.setBigImgPath(imageName);
		imageModel.setThumbImgPath(thumbName);
		imageModel.setMsglocalid(msg.getMsgId());
		imageModel.setCreatetime((int)DateUtil.getCurrentTime());
		imageModel.setTotalLen(FileUtil.decodeFileLength(body.getLocalUrl()));
        LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "insert: compress img size = " + imageModel.getTotalLen());
        return imageModel;
    }

    public String getThumbUrlAndDel(String fileName) {
        if(TextUtils.isEmpty(fileName)) {
            return null;
        }
        if(fileName.trim().startsWith("THUMBNAIL://")) {
            String fileId = fileName.substring("THUMBNAIL://".length());
            String imgName = getImgInfo(fileId).getThumbImgPath();
            if(imgName == null) {
                return null;
            }
            String fileUrlByFileName = FileAccessor.getImagePathName() + "/" + imgName;
            delImgInfo(fileId);
            return fileUrlByFileName;
        }
        return null;
    }
	
	public Bitmap getThumbBitmap(String fileName , float scale) {
		if(TextUtils.isEmpty(fileName)) {
			return null;
		}
		if(fileName.trim().startsWith("THUMBNAIL://")) {
			String fileId = fileName.substring("THUMBNAIL://".length());
			String imgName = getImgInfo(fileId).getThumbImgPath();
			if(imgName == null) {
				return null;
			}
			String fileUrlByFileName = FileAccessor.getImagePathName() + "/" + imgName;;
            //String fileUrlByFileName = FileAccessor.getFileUrlByFileName(imgName);
			Bitmap bitmap = imgThumbCache.get(fileUrlByFileName);
			if(bitmap == null || bitmap.isRecycled()) {
				Options options = new Options();
			    float density = 160.0F * scale;
			    options.inDensity = (int)density;
			    bitmap = BitmapFactory.decodeFile(fileUrlByFileName, options);
			    if (bitmap != null){
			    	bitmap.setDensity((int)density);
			    	bitmap = Bitmap.createScaledBitmap(bitmap, (int)(scale * bitmap.getWidth()), (int)(scale * bitmap.getHeight()), true);
			    	imgThumbCache.put(fileUrlByFileName, bitmap);
			    	LogUtil.d(TAG, "cached file " + fileName);
			    }
			}
			
			if(bitmap != null) {
				return VeryUtils.processBitmap(bitmap, /*bitmap.getWidth() / 15*/0);
			}
			
		}
		return null;
	}
	
	/**
	 * 
	 * @param msgId
	 * @return
	 */
	public Image getImgInfo(String msgId) {
		Image imageModel = new Image();
		String where = ImgInfoColumn.MSG_LOCAL_ID + "='" + msgId + "'";
		Cursor cursor = sqliteDB().query(TABLES_NAME_IMGINFO, null, where, null, null, null, null);
		if(cursor.getCount() != 0) {
			cursor.moveToFirst();
			imageModel.setCursor(cursor);
		}
		cursor.close();
		return imageModel;
	}

	public Image getImgInfo(int id) {
		Image imageModel = new Image();
		String where = ImgInfoColumn.ID + "=" + id ;
		Cursor cursor = sqliteDB().query(TABLES_NAME_IMGINFO, null, where, null, null, null, null);
		if(cursor.getCount() != 0) {
			cursor.moveToFirst();
			imageModel.setCursor(cursor);
		}
		cursor.close();
		return imageModel;
	}



    public long delImgInfo(String msgId) {
        String where = ImgInfoColumn.MSG_LOCAL_ID + "='" + msgId + "'";
       return getInstance().sqliteDB().delete(TABLES_NAME_IMGINFO ,where, null);
    }
	
	public static void reset() {
		getInstance().release();
	}
	
	@Override
	protected void release() {
		super.release();
		mInstance = null;
	}
}
