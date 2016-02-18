package com.ucan.app.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.gc.materialdesign.views.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.ucan.app.R;
import com.ucan.app.base.core.AppManager;
import com.ucan.app.base.service.UserInfoService;
import com.ucan.app.ui.callbacks.ResponseCallBack;
import com.ucan.app.ui.dialog.UCProgressDialog;
import com.ucan.app.common.enums.PreferenceSettings;
import com.ucan.app.common.helper.HttpHelper;
import com.ucan.app.base.domain.UserInfo;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.ResourceHelper;
import com.ucan.app.common.utils.ToastUtil;
import com.ucan.app.common.utils.UCPreferences;
import com.ucan.app.common.utils.VeryUtils;
import com.ucan.app.ui.base.BaseActivity;
import com.ucan.app.ui.view.CircularImage;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.HashMap;

public class LoginActivity extends BaseActivity implements TextWatcher,
        View.OnClickListener {
    private CircularImage avatarIm;
    private MaterialEditText accountEtv, passwordEtv;
    private ButtonFlat signInBtn, signUpBtn;
    private String mAccount, mPassword;
    private UCProgressDialog mPostingdialog;
    private Context ctx;
    private Bitmap mAvatarDefault;
    private File mAvatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ctx = this;
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        setSatutsBarTint(this, R.color.colorPrimaryDark);
        avatarIm = (CircularImage) findViewById(R.id.avatarIm);
        avatarIm.setBorderWidth(10);
        avatarIm.setBorderColor(ContextCompat.getColor(this, R.color.white));
        try {
            mAvatarDefault = VeryUtils.decodeStream(AppManager.getContext().getAssets().open("avatar/personal_center_default_avatar.png"), ResourceHelper.getDensity(null));
            mAvatarFile = new File(FileAccessor.getAvatarPathName() + "/" + VeryUtils.md5("avatar"));
            if (mAvatarFile != null && mAvatarFile.exists()) {
                avatarIm.setImageURI(Uri.fromFile(mAvatarFile));
            } else {
                avatarIm.setImageBitmap(mAvatarDefault);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        accountEtv = (MaterialEditText) findViewById(R.id.accountEtv);
        accountEtv.addTextChangedListener(this);
        passwordEtv = (MaterialEditText) findViewById(R.id.passwordEtv);
        passwordEtv.addTextChangedListener(this);

        signInBtn = (ButtonFlat) findViewById(R.id.signinBtn);
        signInBtn.setOnClickListener(this);
        signInBtn.setEnabled(false);
        signUpBtn = (ButtonFlat) findViewById(R.id.signupBtn);
        signUpBtn.setOnClickListener(this);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!TextUtils.isEmpty(accountEtv.getText())
                && !TextUtils.isEmpty(passwordEtv.getText())) {
            signInBtn.setEnabled(true);
        } else {
            signInBtn.setEnabled(false);
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signinBtn:
                hideSoftKeyboard();
                mAccount = accountEtv.getText().toString().trim();
                mPassword = passwordEtv.getText().toString().trim();
                mPostingdialog = new UCProgressDialog(ctx, R.string.login_posting);
                mPostingdialog.show();

                HttpHelper.getInstance().httpClient(UserInfoService.class).isCorrectUser(mAccount, mPassword).enqueue(new ResponseCallBack<HashMap<String, String>>() {
                    @Override
                    public void onError(Throwable t) {
                        mPostingdialog.dismiss();
                        ToastUtil.showMessage(R.string.http_error_1);
                        t.printStackTrace();
                    }

                    @Override
                    public void onSuccess(String status, String msg, String data) {
                        if ("error".equals(status)) {
                            ToastUtil.showMessage(msg);
                            return;
                        }
                    }
                });
                break;

            case R.id.signupBtn:
                Intent intent = new Intent(ctx, RegisterActivity.class);
                startActivityForResult(intent, 0x2a);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0x2a) {

        }
    }

    private void saveAccount() throws InvalidClassException {
        UserInfo user = new UserInfo(mAccount);
        AppManager.setUserInfo(user);
        UCPreferences.savePreference(PreferenceSettings.SETTINGS_REGIST_AUTO,
                user.toString(), true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
