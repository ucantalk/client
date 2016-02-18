package com.ucan.app.common.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;


/**
 * 图像处理
 */
public class BitmapUtil {
	private static final String TAG = "UCAN.BitmapUtil";
	public static Bitmap getCombineBitmaps(List<InnerBitmapEntity> mEntityList,
			Bitmap... bitmaps) {
		LogUtil.d(TAG , "count=" + mEntityList.size());
		Bitmap newBitmap = Bitmap.createBitmap(200, 200, Config.ARGB_8888);
		LogUtil.d(TAG , "newBitmap=" + newBitmap.getWidth() + ","
				+ newBitmap.getHeight());
		for (int i = 0; i < mEntityList.size(); i++) {
			newBitmap = mixtureBitmap(newBitmap, bitmaps[i], new PointF(
					mEntityList.get(i).x, mEntityList.get(i).y));
		}
		return newBitmap;
	}
	
	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
			PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
				first.getHeight(), Config.ARGB_8888);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, 0, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();
		return newBitmap;
	}

	public static String saveBitmapToLocal(String outPath , Bitmap bitmap) {
		try {
			String imagePath = FileAccessor.getAvatarPathName() + "/" + VeryUtils.md5(outPath);
			File file = new File(imagePath);
			if(!file.exists()) {
				file.createNewFile();
			}
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, bufferedOutputStream);
			bufferedOutputStream.close();
			LogUtil.d(TAG, "photo image from data, path:" + imagePath);
			return imagePath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public static class InnerBitmapEntity {
		public float x;
		public float y;
		public float width;
		public float height;
		public static int devide = 1;
		public int index = -1;

		@Override
		public String toString() {
			return "InnerBitmapEntity [x=" + x + ", y=" + y + ", width=" + width
					+ ", height=" + height + ", devide=" + devide + ", index="
					+ index + "]";
		}
	}
}
