package com.ucan.app.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gc.materialdesign.views.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.ucan.app.R;
import com.ucan.app.ui.callbacks.OnSyncDataListener;
import com.ucan.app.common.utils.ToastUtil;
import com.ucan.app.common.utils.VeryUtils;

import java.util.HashMap;

public class RegisterSetPasswordFragment extends Fragment implements
        View.OnClickListener, TextWatcher {
    private OnSyncDataListener mListener;
    private View view;
    private ButtonFlat mNextBtn, mPreBtn;
    private MaterialEditText mPwdEtv;
    private String mPassword;

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
        view = inflater.inflate(R.layout.fragment_register_setpassword,
                container, false);
        mPwdEtv = (MaterialEditText) view.findViewById(R.id.setPasswordEtv);
        mPwdEtv.addTextChangedListener(this);
        mNextBtn = (ButtonFlat) view.findViewById(R.id.nextBtn);
        mNextBtn.setEnabled(false);
        mNextBtn.setOnClickListener(this);
        mPreBtn = (ButtonFlat) view.findViewById(R.id.preBtn);
        mPreBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null
                && savedInstanceState.getBoolean("isConflict", false))
            return;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        if (mPwdEtv != null) {
            mPwdEtv.setText("");
        }

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
            case R.id.nextBtn:
                mPassword = mPwdEtv.getText().toString();
                if (!VeryUtils.validPassword(mPassword)) {
                    ToastUtil.showMessage("请输入由6至12位数字和字母组成的密码");
                    return;
                }
                mListener.OnPushData(new HashMap<String, String>() {{
                    put("password", VeryUtils.md5(mPassword));
                }});
                break;
            case R.id.preBtn:
                mListener.OnPullData();
                break;
        }

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(mPwdEtv.getText())) {
            mNextBtn.setEnabled(false);
            return;
        }
        mNextBtn.setEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
