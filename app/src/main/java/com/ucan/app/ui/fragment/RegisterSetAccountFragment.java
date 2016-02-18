package com.ucan.app.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gc.materialdesign.views.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.ucan.app.R;
import com.ucan.app.base.service.UserInfoService;
import com.ucan.app.ui.callbacks.OnSyncDataListener;
import com.ucan.app.common.helper.HttpHelper;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.ToastUtil;
import com.ucan.app.common.utils.VeryUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.my7g.qjlink.sdk.QJLinkManager;
import cn.my7g.qjlink.sdk.http.OnLoadDataListener;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class RegisterSetAccountFragment extends Fragment implements
        View.OnClickListener, TextWatcher {

    private TimeCount mTime;
    private Button mGetVerifycodeBtn;
    private ButtonFlat mNextBtn,mBackBtn;
    private String mAccount, mVerifycode;
    private MaterialEditText mSetAccountEtv, mSetVerifycodeEtv;
    private View view;
    private OnSyncDataListener mListener;


    /**
     * 如果设备api小于23,则onAttach(Context context)不会被调用
     * 为了兼容更多设备，这里依然采用onAttach(Activity activity)
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSyncDataListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnPushDataListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register_setaccount,
                container, false);

        mSetAccountEtv = (MaterialEditText) view.findViewById(R.id.setAccountEtv);
        mSetVerifycodeEtv = (MaterialEditText) view.findViewById(R.id.setVerifycodeEtv);
        mSetAccountEtv.addTextChangedListener(this);
        mSetVerifycodeEtv.addTextChangedListener(this);

        mGetVerifycodeBtn = (Button) view.findViewById(R.id.getVerifycodeBtn);
        mGetVerifycodeBtn.setOnClickListener(this);

        mNextBtn = (ButtonFlat) view.findViewById(R.id.nextBtn);
        mBackBtn = (ButtonFlat) view.findViewById(R.id.backBtn);
        mNextBtn.setEnabled(false);
        mNextBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);

        mTime = new TimeCount(60000, 1000);
        return view;
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        if (mSetVerifycodeEtv != null)
            mSetVerifycodeEtv.setText("");

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getVerifycodeBtn:
                mAccount = mSetAccountEtv.getText().toString().trim();
                if (!VeryUtils.validPhoneNumber(mAccount)) {
                    ToastUtil.showMessage("请输入正确的手机号码");
                    return;
                }
                try {
                    HttpHelper.getInstance().httpClient(UserInfoService.class).isExistUser(mAccount).enqueue(new Callback<HashMap<String, String>>() {
                        @Override
                        public void onResponse(Response<HashMap<String, String>> response, Retrofit retrofit) {
                            if (response.isSuccess()) {
                                if ("success".equals(response.body().get("status"))) {
                                    ToastUtil.showMessage(response.body().get("msg"));
                                    return;
                                }
                                requestVerifyCode(mAccount);
                                return;
                            }
                            ToastUtil.showMessage(R.string.http_error_2);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            ToastUtil.showMessage(R.string.http_error_1);
                            t.printStackTrace();
                        }
                    });
                } catch (Exception e) {

                    e.printStackTrace();
                }

                break;
            case R.id.nextBtn:
                mAccount = mSetAccountEtv.getText().toString().trim();
                mVerifycode = mSetVerifycodeEtv.getText().toString().trim();
                requestLogin();
                break;
            case R.id.backBtn:
                mListener.OnPullData();
                break;
        }
    }

    private void requestVerifyCode(String mAccount) {
        QJLinkManager.getInstance(getActivity().getApplicationContext())
                .requestPassword(mAccount, new OnLoadDataListener() {
                    @Override
                    public void onError(String result) {
                        LogUtil.e(result);
                        ToastUtil.showMessage(R.string.http_error_2);
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtil.e(result);
                        try {
                            JSONObject rs = new JSONObject(result);
                            if ((Integer) rs.get("code") != 0) {
                                ToastUtil.showMessage(new JSONObject(result)
                                        .get("msg").toString());
                                return;
                            }
                            mTime.start();
                        } catch (JSONException e) {
                            ToastUtil.showMessage(R.string.http_error_2);
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void requestLogin() {
        QJLinkManager.getInstance(getActivity()).requestLogin(mAccount, mVerifycode,
                new OnLoadDataListener() {
                    @Override
                    public void onError(String result) {
                        LogUtil.e(result);
                        ToastUtil.showMessage(R.string.http_error_2);
                    }

                    @Override
                    public void onSuccess(String result) {
                        LogUtil.e(result);
                        try {
                            JSONObject rs = new JSONObject(result);
                            if ((Integer) rs.get("code") != 0) {
                                ToastUtil.showMessage(new JSONObject(result)
                                        .get("msg").toString());
                                return;
                            }
                            mListener.OnPushData(new HashMap<String, String>() {
                                {
                                    put("account", mAccount);
                                }
                            });

                        } catch (JSONException e) {
                            ToastUtil.showMessage(R.string.http_error_2);
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!TextUtils.isEmpty(mSetAccountEtv.getText().toString())
                && !TextUtils.isEmpty(mSetVerifycodeEtv.getText().toString())) {
            mNextBtn.setEnabled(true);
        } else {
            mNextBtn.setEnabled(false);
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            mGetVerifycodeBtn.setClickable(true);
            mGetVerifycodeBtn.setText("重新验证");

        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mGetVerifycodeBtn.setClickable(false);
            mGetVerifycodeBtn.setText(millisUntilFinished / 1000 + "秒");
        }
    }

}
