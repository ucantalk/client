package com.ucan.app.ui.activities;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.ucan.app.R;
import com.ucan.app.base.service.UserInfoService;
import com.ucan.app.common.helper.HttpHelper;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.ToastUtil;
import com.ucan.app.common.utils.VeryUtils;
import com.ucan.app.ui.base.BaseActivity;
import com.ucan.app.ui.callbacks.OnSyncDataListener;
import com.ucan.app.ui.callbacks.ResponseCallBack;
import com.ucan.app.ui.dialog.UCProgressDialog;
import com.ucan.app.ui.fragment.RegisterSetAccountFragment;
import com.ucan.app.ui.fragment.RegisterSetBasicInfoFragment;
import com.ucan.app.ui.fragment.RegisterSetPasswordFragment;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RegisterActivity extends BaseActivity implements
        OnSyncDataListener {
    private UCProgressDialog mPostingdialog;
    private Context ctx;
    private Fragment[] fragments;
    private int titleText[] = {R.string.register_set_account,
            R.string.register_set_password, R.string.register_set_basic_info};
    HashMap<String, String> params = new HashMap<String, String>();
    private int currentIndex;
    private UserInfo userInfo;
    private Toolbar mToolbar;
    private File mAvatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_register);
        setSatutsBarTint(this, R.color.colorPrimaryDark);
        initData();
        initView();
    }

    private void doRegister() {
        mPostingdialog = new UCProgressDialog(this, R.string.register_posting);
        mPostingdialog.show();
        mAvatarFile = new File(FileAccessor.getAvatarPathName() + "/" + VeryUtils.md5("avatar"));
        HashMap<String, RequestBody> map = new HashMap<>();

        RequestBody accountRequst = RequestBody.create(MediaType.parse("text/plain"), params.get("account"));
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), mAvatarFile);
        map.put("account", accountRequst);
        map.put("image\"; filename=\"" + mAvatarFile.getName() + "", fileBody);
        HttpHelper.getInstance().httpClient(UserInfoService.class).uploadAvatarImage(map).enqueue(new ResponseCallBack<HashMap<String, String>>() {
            @Override
            public void onSuccess(int code, String msg, String data) {

            }

            @Override
            public void onError(Throwable t) {
                mPostingdialog.dismiss();
                ToastUtil.showMessage(R.string.http_error_1);
                t.printStackTrace();
            }
        });
//        userInfo.setAccount(params.get("account"));
//        userInfo.setPassword(params.get("password"));
//        userInfo.setGender(params.get("gender"));
//        userInfo.setNickName(params.get("nickName"));
//        userInfo.setBirthDay(params.get("birthDay"));
//        userInfo.setHomeTown(params.get("homeTown"));
//        userInfo.setAvatarUrl(params.get("avatarUrl"));
//        mPostingdialog = new UCProgressDialog(this, R.string.register_posting);
//        mPostingdialog.show();
    }

    private void initData() {
        userInfo = new UserInfo();
        currentIndex = 0;
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolBar);
        mToolbar.setTitle(titleText[currentIndex]);
        setSupportActionBar(mToolbar);
        fragments = new Fragment[]{new RegisterSetAccountFragment(),
                new RegisterSetPasswordFragment(),
                new RegisterSetBasicInfoFragment()};
//        fragments = new Fragment[]{new RegisterSetBasicInfoFragment(),
//                new RegisterSetPasswordFragment(),
//                new RegisterSetAccountFragment()};
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_contain, fragments[0])
                .add(R.id.fragment_contain, fragments[1])
                .add(R.id.fragment_contain, fragments[2]).hide(fragments[1])
                .hide(fragments[2]).commit();


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            OnPullData();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void OnPullData() {
        if (currentIndex <= 0) {
            finish();
            return;
        }
        currentIndex--;

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_left_in,
                        R.anim.fragment_slide_right_out)
                .hide(fragments[currentIndex + 1])
                .show(fragments[currentIndex]).commit();

    }

    @Override
    public void OnPushData(HashMap v) {
        Iterator<Map.Entry<String, String>> iter = v.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter
                    .next();
            String key = entry.getKey();
            String val = entry.getValue();
            params.put(key, val);
        }
        if (currentIndex >= 2) {
            doRegister();
            return;
        }
        currentIndex++;

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_slide_right_in,
                        R.anim.fragment_slide_left_out)
                .hide(fragments[currentIndex - 1])
                .show(fragments[currentIndex]).commit();

    }

}
