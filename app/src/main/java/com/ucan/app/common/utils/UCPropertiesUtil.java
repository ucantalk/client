package com.ucan.app.common.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;

/**
 * 读取头像合成所需要的坐标体系
 */
public class UCPropertiesUtil {
	/**
	 * 根据Key 读取Value
	 * 
	 * @param key
	 * @return
	 */
	public static String readData(Context mContext, String key, int resId) {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(mContext.getResources()
					.openRawResource(resId));
			props.load(in);
			in.close();
			String value = props.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
