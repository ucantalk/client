
package com.ucan.app.base.db;

import java.util.ArrayList;
import java.util.Random;

import junit.framework.Assert;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.ucan.app.base.core.AppManager;
import com.ucan.app.common.helper.ContactHelper;
import com.ucan.app.common.model.Contact;
import com.ucan.app.base.domain.UserInfo;
import com.yuntongxun.ecsdk.im.ECGroupMember;

/**
 * 联系人数据库管理
 */
public class ContactSqlManager extends AbstractSQLManager {

    private static ContactSqlManager sInstance;
    private static ContactSqlManager getInstance() {
        if(sInstance == null) {
            sInstance = new ContactSqlManager();
        }
        return sInstance;
    }

    public static boolean hasContact(String contactId) {
        String sql = "select contact_id from contacts where contact_id = '" + contactId + "'";
        Cursor cursor = getInstance().sqliteDB().rawQuery(sql , null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * 插入联系人到数据库
     * @param contacts
     * @return
     */
//    public static ArrayList<Long> insertContacts(List<Contact> contacts) {
//
//        ArrayList<Long> rows = new ArrayList<Long>();
//        try {
//
//            getInstance().sqliteDB().beginTransaction();
//            for(Contact c : contacts) {
//                long rowId = insertContact(c);
//                if(rowId != -1L) {
//                    rows.add(rowId);
//                }
//            }
//
//            // 初始化系统联系人
//            insertSystemNoticeContact();
//            getInstance().sqliteDB().setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            getInstance().sqliteDB().endTransaction();
//        }
//        return rows;
//    }

    /**
     * 初始化联系人数据库
     * @return
     */
    private static long insertSystemNoticeContact() {
        Contact contacts = new Contact(GroupNoticeSqlManager.CONTACT_ID);
        contacts.setNickname("系统通知");
        contacts.setRemark("touxiang_notice.png");

        return insertContact(contacts);
    }

    public static long updateContactPhoto(Contact contact) {
        return insertContact(contact , 1 , true);
    }

    /**
     * 根据性别更新联系人信息（区分联系人头像）
     * @param contact
     * @param sex
     * @return
     */
    public static long insertContact(Contact contact , int sex) {
        return insertContact(contact , sex , false);
    }

    public static long insertContact(Contact contact , int sex , boolean hasPhoto) {
        if(contact == null || TextUtils.isEmpty(contact.getContactid())) {
            return -1;
        }
        try {
            ContentValues values = contact.buildContentValues();
            if(!hasPhoto ) {
                int index = getIntRandom(3, 0);
                if(sex == 2) {
                    index = 4;
                }
                String remark = ContactHelper.CONVER_PHONTO[index];
                contact.setRemark(remark);
            }
            values.put(AbstractSQLManager.ContactsColumn.REMARK, contact.getRemark());
            if(!hasContact(contact.getContactid())) {
                return getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_CONTACT, null, values);
            }
            getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_CONTACT , values , "contact_id = '" + contact.getContactid() + "'" , null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 插入联系人到数据库
     * @param contact
     * @return
     */
    public static long insertContact(Contact contact) {
        return insertContact(contact , 1);
    }


    /**
     * 查询联系人名称
     * @param contactId
     * @return
     */
    public static ArrayList<String> getContactName(String[] contactId) {
        ArrayList<String> contacts = null;
        try {
            String sql = "select username ,contact_id from contacts where contact_id in ";
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < contactId.length; i++) {
                sb.append("'").append(contactId[i]).append("'");
                if (i != contactId.length - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            Cursor cursor = getInstance().sqliteDB().rawQuery(
                    sql + sb.toString(), null);
            if (cursor != null && cursor.getCount() > 0) {
                contacts = new ArrayList<String>();
                // 过滤自己的联系人账号
                UserInfo UserInfo = AppManager.getUserInfo();
                while (cursor.moveToNext()) {
                    if (UserInfo != null
                            && UserInfo.getAccount().equals(
                            cursor.getString(0))) {
                        continue;
                    }
                    String displayName = cursor.getString(0);
                    String contact_id = cursor.getString(1);
                    if(TextUtils.isEmpty(displayName) || TextUtils.isEmpty(contact_id) || displayName.equals(contact_id)) {
                        continue;
                    }
                    contacts.add(displayName);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contacts;
    }

    /**
     * 查询联系人名称
     * @param contactId
     * @return
     */
    public static ArrayList<String> getContactRemark(String[] contactId) {
        ArrayList<String> contacts = null;
        try {
            String sql = "select remark from contacts where contact_id in ";
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < contactId.length; i++) {
                sb.append("'").append(contactId[i]).append("'");
                if (i != contactId.length - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            Cursor cursor = getInstance().sqliteDB().rawQuery(
                    sql + sb.toString(), null);
            if (cursor != null && cursor.getCount() > 0) {
                contacts = new ArrayList<String>();
                while (cursor.moveToNext()) {
                    contacts.add(cursor.getString(0));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contacts;
    }

    /**
     * 更新联系人名字
     * @param member
     */
    public static void updateContactName(ECGroupMember member) {
        ContentValues values = new ContentValues();
        values.put(AbstractSQLManager.ContactsColumn.USERNAME , member.getDisplayName());
        getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_CONTACT , values , "contact_id = '" + member.getVoipAccount() + "'" , null);
    }

    /**
     * 查询联系人
     * @return
     */
    public static ArrayList<Contact> getContacts() {
        ArrayList<Contact> contacts = null;
        try {
            Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT,
                    new String[]{
                            ContactsColumn.ID,
                            ContactsColumn.USERNAME ,
                            ContactsColumn.CONTACT_ID,
                            ContactsColumn.REMARK},
                    null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                contacts = new ArrayList<Contact>();
                // 过滤自己的联系人账号信息
                 UserInfo UserInfo = AppManager.getUserInfo();
                while (cursor.moveToNext()) {
                    if(GroupNoticeSqlManager.CONTACT_ID.equals(cursor.getString(2))) {
                        continue;
                    }
                    Contact c = new Contact(cursor.getString(2));
                    c.setNickname(cursor.getString(1));
                    c.setRemark(cursor.getString(3));
                    c.setId(cursor.getInt(0));
                    if(UserInfo != null && UserInfo.getAccount().equals(c.getContactid())) {
                        continue;
                    }
                    contacts.add(c);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contacts;

    }


    /**
     * 根据联系人ID查询联系人
     * @param rawId
     * @return
     */
    public static Contact getContact(long rawId) {
        if(rawId == -1) {
            return null;
        }
        try {
            Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
                    "id=?", new String[]{String.valueOf(rawId)},null , null, null, null);
            Contact c = null;
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    c = new Contact(cursor.getString(2));
                    c.setNickname(cursor.getString(1));
                    c.setRemark(cursor.getString(3));
                    c.setId(cursor.getInt(0));
                }
                cursor.close();
            }
            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param phoneNumber
     * @return
     */
    public static Contact getCacheContact(String phoneNumber) {
//        if (ContactsCache.getInstance().getContacts() != null) {
//            return ContactsCache.getInstance().getContacts()
//                    .getValueByPhone(phoneNumber);
//        }
        return null;
    }

    /**
     * 根据联系人账号查询
     * @param contactId
     * @return
     */
//    public static Contact  getContact(String contactId) {
//        if(TextUtils.isEmpty(contactId)) {
//            return null;
//        }
//        Contact c = new Contact(contactId);
//        c.setNickname(contactId);
//        try {
//            Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
//                    "contact_id=?", new String[]{contactId},null , null, null, null);
//            if (cursor != null && cursor.getCount() > 0) {
//                while (cursor.moveToNext()) {
//                    c = new Contact(cursor.getString(2));
//                    c.setNickname(cursor.getString(1));
//                    c.setRemark(cursor.getString(3));
//                    c.setId(cursor.getInt(0));
//                }
//                cursor.close();
//            }
//            return c;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return c;
//    }

    /**
     * 根据昵称查询信息
     * @param nikeName
     * @return
     */
    public static Contact getContactLikeUsername(String nikeName) {
        if(TextUtils.isEmpty(nikeName)) {
            return null;
        }
        try {
            Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
                    "username LIKE '" + nikeName + "'" , null,null , null, null, null);
            Contact c = null;
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    c = new Contact(cursor.getString(2));
                    c.setNickname(cursor.getString(1));
                    c.setRemark(cursor.getString(3));
                    c.setId(cursor.getInt(0));
                }
                cursor.close();
            }
            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Contact getContactLikeUserId(String nikeName) {
        if(TextUtils.isEmpty(nikeName)) {
            return null;
        }
        try {
            Cursor cursor = getInstance().sqliteDB().query(DatabaseHelper.TABLES_NAME_CONTACT, new String[]{ContactsColumn.ID,ContactsColumn.USERNAME ,ContactsColumn.CONTACT_ID ,ContactsColumn.REMARK},
                    "username LIKE '" + nikeName + "'" , null,null , null, null, null);
            Contact c = null;
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    c = new Contact(cursor.getString(2));
                    c.setNickname(cursor.getString(1));
                    c.setRemark(cursor.getString(3));
                    c.setId(cursor.getInt(0));
                }	
                cursor.close();
            }
            return c;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void reset() {
        getInstance().release();
        sInstance = null;
    }

    public static int getIntRandom(int max, int min) {
        Assert.assertTrue(max > min);
        return (new Random(System.currentTimeMillis()).nextInt(max - min + 1) + min);
    }
}
