package com.ucan.app.common.enums;

public enum PreferenceSettings {
    /**
     * Whether is the first use of the application
     */
    SETTINGS_FIRST_USE("com.ucan.app_first_use", Boolean.TRUE),

    /**
     * 保存账号
     */

    SETTINGS_UCAN_ACCOUNT(
            "com.ucan.app_ucan_account", ""),
    /**
     * 检查是否需要自动登录
     */
    SETTINGS_REGIST_AUTO("com.ucan.app_account", ""),
    /**
     * 是否使用回车键发送消息
     */
    SETTINGS_ENABLE_ENTER_KEY("com.ucan.app_sendmessage_by_enterkey",
            Boolean.TRUE),
    /**
     * 聊天键盘的高度
     */
    SETTINGS_KEYBORD_HEIGHT("com.ucan.app_keybord_height", 0),
    /**
     * 新消息声音
     */
    SETTINGS_NEW_MSG_SOUND("com.ucan.app_new_msg_sound", true),
    /**
     * 新消息震动
     */
    SETTINGS_NEW_MSG_SHAKE("com.ucan.app_new_msg_shake", true),


    SETTING_CHATTING_CONTACTID("com.ucan.app_chatting_contactid", ""),
    /**
     * 图片缓存路径
     */
    SETTINGS_CROPIMAGE_OUTPUTPATH("com.ucan.app_CropImage_OutputPath", ""),


    /* 云通讯appkey */
    SETTINGS_APPKEY("com.ucan.app_appkey", "20150314000000110000000000000010"),


    /* 云通讯token */
    SETTINGS_TOKEN("com.ucan.app_token", "17E24E5AFDB6D0C1EF32F3533494502B"),
    //SETTINGS_APPKEY("com.ucan.app_appkey","8a48b55150655bee015065981823010f"),
    //SETTINGS_TOKEN("com.ucan.app_token","b50c629e87c20f33ff119e75c80cf716"),


    /**
     *  登出程序
     */
    SETTINGS_ABSOLUTELY_EXIT("com.ucan.app_absolutely_exit", Boolean.FALSE),

    /**
     *登出账号
     */

    SETTINGS_FULLY_EXIT("com.ucan.app_fully_exit", Boolean.FALSE),

    SETTINGS_PREVIEW_SELECTED("com.ucan.app_preview_selected", Boolean.FALSE),
    SETTINGS_OFFLINE_MESSAGE_VERSION("com.ucan.app_offline_version", 0),
    /**
     * 设置是否是匿名聊天
     */
    SETTINGS_SHOW_CHATTING_NAME("com.ucan.app_show_chat_name", false),
    SETTINGS_CUSTOM_APPKEY("com.ucan.app_custom_appkey", ""),
    SETTINGS_CUSTOM_TOKEN("com.ucan.app_custom_token", ""),
    SETTINGS_SERVER_CUSTOM("com.ucan.app_setserver", false);

    private final String mId;
    private final Object mDefaultValue;

    /**
     * Constructor of <code>PreferenceSettings</code>.
     *
     * @param id           The unique identifier of the setting
     * @param defaultValue The default value of the setting
     */
    private PreferenceSettings(String id, Object defaultValue) {
        this.mId = id;
        this.mDefaultValue = defaultValue;
    }

    /**
     * Method that returns the unique identifier of the setting.
     *
     * @return the mId
     */
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns the default value of the setting.
     *
     * @return Object The default value of the setting
     */
    public Object getDefaultValue() {
        return this.mDefaultValue;
    }

    /**
     * Method that returns an instance of
     * {@link PreferenceSettings} from its. unique
     * identifier
     *
     * @param id The unique identifier
     * @return CCPPreferenceSettings The navigation sort mode
     */
    public static PreferenceSettings fromId(String id) {
        PreferenceSettings[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId == id) {
                return values[i];
            }
        }
        return null;
    }
}
