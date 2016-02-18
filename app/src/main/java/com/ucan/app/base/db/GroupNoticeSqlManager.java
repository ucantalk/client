package com.ucan.app.base.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.ucan.app.common.model.GroupNotice;
import com.ucan.app.common.helper.GroupNoticeHelper;
import com.ucan.app.common.model.NoticeSystemMessage;
import com.ucan.app.common.utils.LogUtil;
import com.yuntongxun.ecsdk.ECMessage;


/**
 * 群组通知消息数据库
 */
public class GroupNoticeSqlManager extends AbstractSQLManager {

    public static final int NOTICE_MSG_TYPE = 1000;
    public static final String CONTACT_ID = "10089";

    private static GroupNoticeSqlManager instance;

    private GroupNoticeSqlManager() {
        super();
    }

    private static GroupNoticeSqlManager getInstance() {
        if (instance == null) {
            instance = new GroupNoticeSqlManager();
        }
        return instance;
    }

    /**
     * 更新群组通知消息
     * @param notice
     * @return
     */
    public static long insertNoticeMsg(NoticeSystemMessage notice) {
        long ownThreadId = -1;
        if(notice != null) {
            // values.put("sid", "ec_group@yuntongxun.com");
            ContentValues buildContentValues = notice.buildContentValues();
            ownThreadId = ConversationSqlManager.querySessionIdForBySessionId(CONTACT_ID);
            if (ownThreadId == 0) {
                try {
                    ECMessage message = ECMessage.createECMessage(ECMessage.Type.NONE);
                    message.setForm(CONTACT_ID);
                    message.setSessionId(CONTACT_ID);
                    ownThreadId = ConversationSqlManager.insertSessionRecord(message);

                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e(TAG + " " + e.toString());
                }
            }
            if(ownThreadId > 0) {
                buildContentValues.put(SystemNoticeColumn.OWN_THREAD_ID, ownThreadId);
                long row = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, null, buildContentValues);
                if(row != -1) {
                    getInstance().notifyChanged("ec_group@yuntongxun.com");
                }
                return row;
            }
        }
        return -1;
    }


    /**
     * 更新群组通知消息
     * @param notice
     * @return
     */
    public static long insertNoticeMsg(GroupNotice notice) {
        long ownThreadId = -1;
        if(notice != null) {
            // values.put("sid", "ec_group@yuntongxun.com");
            ContentValues buildContentValues = notice.buildContentValues();
            ownThreadId = ConversationSqlManager.querySessionIdForBySessionId(notice.getSender());
            if (ownThreadId == 0) {
                try {
                    IMessageSqlManager.checkContact(notice.getSender());
                    ECMessage message = ECMessage.createECMessage(ECMessage.Type.NONE);
                    message.setForm(notice.getSender());
                    message.setSessionId(notice.getSender());
                    ownThreadId = ConversationSqlManager.insertSessionRecord(message);

                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.e(TAG + " " + e.toString());
                }
            }
            if(ownThreadId > 0) {
                buildContentValues.put(SystemNoticeColumn.OWN_THREAD_ID, ownThreadId);
                long row = -1;
                if(!getInstance().hasNotice(notice.getId())) {
                    row = getInstance().sqliteDB().insert(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, null, buildContentValues);
                } else {
                    buildContentValues.remove("notice_id");
                    buildContentValues.remove("isRead");
                    row = getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE , buildContentValues , "notice_id='" + notice.getId() + "'", null);
                }
                if(row != -1) {
                    getInstance().notifyChanged(notice.getSender());
                }
                return row;
            }
        }
        return -1;
    }

    public boolean hasNotice(String noticeid) {
        String sql = "select notice_id from " + DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE + " where notice_id='" + noticeid + "'";
        Cursor cursor = getInstance().sqliteDB().rawQuery(sql, null);
        if(cursor != null && cursor.getCount() > 0) {
            cursor.close();
            cursor = null;
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    public static int getMaxVersion() {
        String sql = "select max(version) as maxVersion from " + DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE ;
        Cursor cursor = getInstance().sqliteDB().rawQuery(sql , null);
        if(cursor != null && cursor.getCount() > 0) {
            if(cursor.moveToFirst()) {
                int maxVersion = cursor.getInt(cursor.getColumnIndex("maxVersion"));
                cursor.close();;
                return maxVersion;
            }
        }
        return 0;
    }

    /**
     * 查询通知
     * @return
     */
    public static Cursor getCursor() {
        String sql = "select notice_id , verifymsg , admin  , confirm , groupId , member ,dateCreated , groupName ,nickName ,type ,declared from system_notice order by dateCreated desc";
        return getInstance().sqliteDB().rawQuery(sql, null);
    }

    /**
     * 设置会话已读
     */
    public static void setAllSessionRead() {
        ContentValues values = new ContentValues();
        values.put(SystemNoticeColumn.NOTICE_READ_STATUS, IMessageSqlManager.IMESSENGER_TYPE_READ);
        String where = SystemNoticeColumn.NOTICE_READ_STATUS + " != " + IMessageSqlManager.IMESSENGER_TYPE_READ;
        getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, values, where, null);
    }

    /**
     * 情况群组通知消息
     */
    public static void delSessions() {
        getInstance().sqliteDB().delete(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE, null, null);
    }

    public static void setSessionRead() {

    }

    public static void registerMsgObserver(OnMessageChange observer) {
        getInstance().registerObserver(observer);
    }

    public static void unregisterMsgObserver(OnMessageChange observer) {
        getInstance().unregisterObserver(observer);
    }

    public static void notifyMsgChanged(String session) {
        getInstance().notifyChanged(session);
    }

    public static void reset() {
        getInstance().release();
    }

    @Override
    protected void release() {
        super.release();
        instance = null;
    }

    public static long updateNoticeOperation(String id, boolean isAccept) {
        ContentValues values = new ContentValues();
        values.put("confirm" , isAccept? GroupNoticeHelper.SYSTEM_MESSAGE_THROUGH:GroupNoticeHelper.SYSTEM_MESSAGE_REFUSE);
        return getInstance().sqliteDB().update(DatabaseHelper.TABLES_NAME_SYSTEM_NOTICE , values ,"notice_id='" + id +"'", null);
    }
}
