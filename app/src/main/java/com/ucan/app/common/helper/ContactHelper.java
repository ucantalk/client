package com.ucan.app.common.helper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.ucan.app.R;
import com.ucan.app.UCApplication;
import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.db.ContactSqlManager;
import com.ucan.app.base.db.GroupMemberSqlManager;
import com.ucan.app.common.model.Contact;
import com.ucan.app.common.utils.BitmapUtil;
import com.ucan.app.common.utils.DialNumberMap;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.ResourceHelper;
import com.ucan.app.common.utils.UCPropertiesUtil;
import com.ucan.app.common.utils.VeryUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 联系人逻辑处理
 */
public class ContactHelper {


    public static final String ALPHA_ACCOUNT = "izhangjy@163.com";
    public static final String CUSTOM_SERVICE = "KF4008818600668603";
    private static HashMap<String, Bitmap> photoCache = new HashMap<String, Bitmap>(20);
    public static final String[] CONVER_NAME = {"张三","李四","王五","赵六","钱七"};
    public static final String[] CONVER_PHONTO = {"select_account_photo_one.png"
            ,"select_account_photo_two.png"
            ,"select_account_photo_three.png"
            ,"select_account_photo_four.png"
            ,"select_account_photo_five.png"
    };

    private static String[] projection_getSettingList = {
            ContactsContract.Settings.ACCOUNT_TYPE,
            ContactsContract.Settings.ACCOUNT_NAME };

    private static String[] projection_getContractList = {
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2, ContactsContract.Data.DATA3,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.Data._ID, ContactsContract.Data.TIMES_CONTACTED,
            ContactsContract.Data.DATA5, ContactsContract.Data.DATA6,};

    public static Bitmap mDefaultBitmap = null;


    static {
        try {
            if(mDefaultBitmap == null) {
                mDefaultBitmap = VeryUtils.decodeStream(AppManager.getContext().getAssets().open("avatar/personal_center_default_avatar.png"), ResourceHelper.getDensity(null));
            }
        } catch (IOException e) {
        }
    }

    private static ContactHelper sInstance;
    public static ContactHelper getInstance() {
        if (sInstance == null) {
            sInstance = new ContactHelper();
        }
        return sInstance;
    }

    /**
     * 查找头像
     * @param username
     * @return
     */
    public static Bitmap getPhoto(String username) {

        if(TextUtils.isEmpty(username)) {
            return mDefaultBitmap;
        }
        try {
            if (photoCache.containsKey(username)) {
                return photoCache.get(username);
            }
            Bitmap bitmap ;
            if(username.startsWith("mobilePhoto://")) {
                bitmap = BitmapFactory.decodeFile(new File(FileAccessor.getAvatarPathName() , username.substring("mobilePhoto://".length())).getAbsolutePath());
            } else {

                bitmap =  VeryUtils.decodeStream(AppManager.getContext()
                                .getAssets().open("avatar/" + username),
                        ResourceHelper.getDensity(null));
            }
            photoCache.put(username, bitmap);
            return bitmap;
        } catch (IOException e) {
        }
        return mDefaultBitmap;
    }

    /**
     * 随即设置用户昵称
     * @param beas
     * @return
     */
    public static ArrayList<Contact> converContacts(ArrayList<Contact> beas) {

        if(beas == null || beas.isEmpty()) {
            return null;
        }
        Collections.sort(beas, new Comparator<Contact>() {

            @Override
            public int compare(Contact lhs, Contact rhs) {

                return lhs.getContactid().compareTo(rhs.getContactid());
            }

        });

        for(int i = 0 ; i < beas.size() ; i ++ ) {
            Contact accountBean = beas.get(i);
            if (i < 5) {
                accountBean.setRemark(ContactHelper.CONVER_PHONTO[i]);
            } else {
                accountBean.setRemark("personal_center_default_avatar.png");
            }
        }
        return beas;
    }

    public static ArrayList<Contact> initContacts() {
        ArrayList<Contact> list = new ArrayList<Contact>();
        Contact contacts = new Contact("KF4008818600668603");
        contacts.setNickname(AppManager.getContext().getString(R.string.main_plus_mcmessage));
        contacts.setRemark(CONVER_PHONTO[0]);
        list.add(contacts);
        return list;
    }

    /**
     * 是否在线客服
     * @param contact
     * @return
     */
    public static boolean isCustomService(String contact) {
        return CUSTOM_SERVICE.equals(contact);
    }

    // These are the Contacts rows that we will retrieve.
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_ID,
    };



    /**
     * 获取联系人配置
     *
     * @return
     */
    public List<String[]> getSettingList() {
        List<String[]> cl = null;
        Cursor cursor = null;
        try {
            cursor = AppManager.getContext().getContentResolver().query(ContactsContract.Settings.CONTENT_URI,
                    projection_getSettingList, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cl = new ArrayList<String[]>();
                while (cursor.moveToNext()) {
                    String[] s = new String[cursor.getColumnCount()];
                    for (int i = 0; i < s.length; i++) {
                        s[i] = cursor.getString(i);
                    }
                    cl.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return cl;
    }

    private static HanyuPinyinOutputFormat format = null;
    public static void pyFormat(Contact contact) {
        try {
            String name = contact.getNickname();
            if (name == null || name.trim().length() == 0) {
                return;
            }
            name = name.trim();
            // 拼音转换设置
            if(format == null) {
                format = new HanyuPinyinOutputFormat();// 定义转换格式
                format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不要声调
                format.setVCharType(HanyuPinyinVCharType.WITH_V);// 设置 女 nv
            }

            String qpName = ""; // 用于分隔全拼数组
            StringBuilder qpBuilder = new StringBuilder();

            String qpNameStr = ""; // 完整的全拼Str
            StringBuilder qpStrBuilder = new StringBuilder();

            String qpNumber = ""; // 全拼对应的拨号盘数字
            StringBuilder qpNumberBuilder = new StringBuilder();

            String jpName = ""; // 简拼
            StringBuilder jpNameBuilder = new StringBuilder();

            String jpNumber = ""; // 简拼对应的拨号盘数字
            StringBuilder jpNumberBuilder = new StringBuilder();
//			LogUtil.v(name);
            int length = 0;
            char c = 0;
            // 处理英文名

            if (name.getBytes().length == name.length()) {
                qpName = name;
                String[] splitName = name.split(" ");
                for (String s : splitName) {
                    length = s.length();
                    for (int i = 0; i < length; i++) {
                        qpNumberBuilder.append(DialNumberMap.numberMap.get(s.charAt(i)) == null ? String.valueOf(s.charAt(i)) : DialNumberMap.numberMap.get(s.charAt(i)));
                    }
                    c = s.charAt(0);
                    qpNumberBuilder.append(" ");
                    jpNumberBuilder.append(DialNumberMap.numberMap.get(c) == null ? String.valueOf(c) : DialNumberMap.numberMap.get(c));
                    jpNameBuilder.append(String.valueOf(c));
                    qpStrBuilder.append(String.valueOf(c).toUpperCase()).append(s.subSequence(1, s.length()));
                }
                length = splitName.length;
                for (int i = 0; i < length; i++) {
                    splitName[i] = splitName[i].toLowerCase();
                }
                // jpName = jpNameBuilder.toString();
            } else { // 含有中文
                int namelength = name.length();
                for (int i = 0; i < namelength; i++) {
                    try {
                        String[] pyArray = PinyinHelper.toHanyuPinyinStringArray(name.charAt(i), format);
                        if (pyArray == null) {
//							char c = name.charAt(i);
                            c = name.charAt(i);
                            if (' ' == c) {
                                continue;
                            }
                            qpStrBuilder.append(c);
                            Integer num = DialNumberMap.numberMap.get(c);
                            qpNumberBuilder.append(num == null ? String.valueOf(c) : num).append(" ");
                            jpNumberBuilder.append(num == null ? String.valueOf(c) : num);
                            qpBuilder.append(String.valueOf(c).toLowerCase()).append(" ");
                            jpNameBuilder.append(String.valueOf(c).toLowerCase());
                            continue;
                        } else {
                            String py = pyArray[0];
                            length = py.length();
                            for (int j = 0; j < length; j++) {
                                qpNumberBuilder.append(DialNumberMap.numberMap.get(py.charAt(j)));
                            }
                            c = py.charAt(0);
                            qpNumberBuilder.append(" ");
                            jpNameBuilder.append(c);
                            jpNumberBuilder.append(DialNumberMap.numberMap.get(c));
                            qpBuilder.append(py).append(" ");
                            qpStrBuilder.append(String.valueOf(c).toUpperCase()).append(py.subSequence(1, py.length()));// 将拼音第一个字母转成大写后拼接在一起。
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
                qpName = qpBuilder.toString();
            }
            jpName = jpNameBuilder.toString();
            jpNumber = jpNumberBuilder.toString();
            qpNumber = qpNumberBuilder.toString();
            qpNameStr = qpStrBuilder.toString();

            if (qpName.length() > 0) {
                contact.setQpName(qpName.trim().split(" "));
                contact.setQpNumber(qpNumber.trim().split(" "));
                contact.setJpNumber(jpNumber.trim());
                contact.setJpName(jpName);
                contact.setQpNameStr(qpNameStr.trim());
                contact.setQpNumber(qpNumber.trim().split(" "));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回讨论组的头像
     * @return
     */
    public static Bitmap getChatroomPhoto(final String groupid) {
        try {
            if (photoCache.containsKey(groupid)) {
                return photoCache.get(groupid);
            }
            new Thread(new Runnable() {

                @Override
                public void run() {
                    processChatroomPhoto(groupid);
                }
            });
            processChatroomPhoto(groupid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(groupid.toUpperCase().startsWith("G")) {
            return BitmapFactory.decodeResource(AppManager.getContext().getResources() , R.drawable.group_head);
        }
        return mDefaultBitmap;
    }

    /**
     * @param groupid
     */
    private static void processChatroomPhoto(String groupid) {
        ArrayList<String> groupMembers = GroupMemberSqlManager.getGroupMemberID(groupid);
        if(groupMembers != null) {
            ArrayList<String> contactName = ContactSqlManager.getContactRemark(groupMembers.toArray(new String[]{}));
            if(contactName != null) {
                Bitmap[] bitmaps = new Bitmap[contactName.size()];
                if(bitmaps.length > 9) {
                    bitmaps = new Bitmap[9];
                }
                List<BitmapUtil.InnerBitmapEntity> bitmapEntitys = getBitmapEntitys(bitmaps.length);
                for(int i = 0; i < bitmaps.length; i ++ ) {
                    Bitmap photo = getPhoto(contactName.get(i));
                    photo = ThumbnailUtils.extractThumbnail(photo, (int) bitmapEntitys.get(0).width, (int) bitmapEntitys.get(0).width);
                    bitmaps[i] = photo;
                }
                Bitmap combineBitmap = BitmapUtil.getCombineBitmaps(bitmapEntitys,bitmaps);
                if(combineBitmap != null) {
                    photoCache.put(groupid, combineBitmap);
                    BitmapUtil.saveBitmapToLocal(groupid, combineBitmap);
                }
            }
        }
    }

    private static List<BitmapUtil.InnerBitmapEntity> getBitmapEntitys(int count) {
        List<BitmapUtil.InnerBitmapEntity> mList = new LinkedList<BitmapUtil.InnerBitmapEntity>();
        String value = UCPropertiesUtil.readData(AppManager.getContext(), String.valueOf(count), R.raw.nine_rect);
        LogUtil.d("value=>" + value);
        String[] arr1 = value.split(";");
        int length = arr1.length;
        for (int i = 0; i < length; i++) {
            String content = arr1[i];
            String[] arr2 = content.split(",");
            BitmapUtil.InnerBitmapEntity entity = null;
            for (int j = 0; j < arr2.length; j++) {
                entity = new BitmapUtil.InnerBitmapEntity();
                entity.x = Float.valueOf(arr2[0]);
                entity.y = Float.valueOf(arr2[1]);
                entity.width = Float.valueOf(arr2[2]);
                entity.height = Float.valueOf(arr2[3]);
            }
            mList.add(entity);
        }
        return mList;
    }


    public static void getMobileContactPhoto(List<Contact> list) {
        if(list == null || list.isEmpty()) {
            return ;
        }
        for(Contact contact : list) {
            if(contact.getPhotoId() > 0) {
                getMobileContactPhoto(contact);
            }
        }
    }

    public static void getMobileContactPhoto(Contact contact) {
        try {
            Bitmap bitmap = getContactPhoto(contact);
            if(bitmap == null) {
                return ;
            }
            contact.setRemark("mobilePhoto://" + contact.getContactid());
            VeryUtils.saveBitmapToLocal(new File(FileAccessor.getAvatarPathName(), contact.getContactid()), bitmap);
            ContactSqlManager.updateContactPhoto(contact);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getContactPhoto(Contact contact){
        long photoId = contact.getPhotoId();
        if (photoId != 0) {
            Cursor cursor = null;
            ContentResolver contentResolver = UCApplication.getInstance().getApplicationContext().getContentResolver();
            try {
                cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, new String[] { ContactsContract.CommonDataKinds.Photo._ID, ContactsContract.CommonDataKinds.Photo.PHOTO }, ContactsContract.CommonDataKinds.Photo._ID + " = " + photoId, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    byte[] photo = cursor.getBlob(1);
                    if (photo != null) {
                        return BitmapFactory.decodeByteArray(photo, 0, photo.length);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(cursor != null){
                    cursor.close();
                    cursor = null;
                }
            }
        }
        return null;
    }

}