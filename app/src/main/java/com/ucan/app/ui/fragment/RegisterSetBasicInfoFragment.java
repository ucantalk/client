package com.ucan.app.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.gc.materialdesign.views.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.ucan.app.R;
import com.ucan.app.common.utils.FileAccessor;
import com.ucan.app.common.utils.LogUtil;
import com.ucan.app.common.utils.VeryUtils;
import com.ucan.app.ui.callbacks.OnSyncDataListener;
import com.ucan.app.ui.view.CircularImage;
import com.ucan.app.ui.view.WheelView;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RegisterSetBasicInfoFragment extends Fragment implements
        View.OnClickListener, TextWatcher {
    private View view;
    private OnSyncDataListener mListener;
    private static int PHOTO_REQUEST_CODE = 1; //选择或者拍摄照片
    private static int CROP_REQUEST_CODE = 2;//照片裁剪
    private MaterialEditText mSetHomeTownEtv, mSetNickNameEtv;
    private ButtonFlat mFinishBtn, mPreBtn;
    private RadioGroup mRadioGroup;
    private CircularImage setAvatar;
    private List<String> mProvince;
    private String mGender, mAvatarFilePath;
    private int[] cities = {R.array.ah_city, R.array.bj_city, R.array.cq_city,
            R.array.fj_city, R.array.gs_city, R.array.gd_city, R.array.gx_city, R.array.gz_city,
            R.array.hb_city, R.array.hn_city, R.array.hlj_city,
            R.array.hub_city, R.array.hun_city, R.array.hain_city,
            R.array.jl_city, R.array.jx_city, R.array.js_city, R.array.ln_city,
            R.array.nx_city, R.array.nmg_city, R.array.qh_city,
            R.array.sh_city, R.array.sx_city, R.array.sd_city,
            R.array.ssx_city, R.array.sc_city, R.array.tj_city,
            R.array.xj_city, R.array.xz_city, R.array.yn_city, R.array.zj_city,
            R.array.xg_city, R.array.am_city, R.array.tw_city,
            R.array.countries};

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
        view = inflater.inflate(R.layout.fragment_register_setbasicinfo,
                container, false);
        mSetNickNameEtv = (MaterialEditText) view.findViewById(R.id.setNickNameEtv);
        mSetHomeTownEtv = (MaterialEditText) view.findViewById(R.id.setHomeTownEtv);

        mRadioGroup = (RadioGroup) view.findViewById(R.id.setGenderRG);
        mGender="男";
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mGender = checkedId == R.id.setMaleRb ? "男" : "女";
                LogUtil.e(mGender);
            }
        });
        mFinishBtn = (ButtonFlat) view.findViewById(R.id.finishBtn);
        mPreBtn = (ButtonFlat) view.findViewById(R.id.preBtn);


        setAvatar = (CircularImage) view.findViewById(R.id.setAvatarIm);
        setAvatar.setBorderWidth(2);
        setAvatar.setBorderColor(ContextCompat.getColor(getActivity(), R.color.colorBaseBorder));
        setAvatar.setImageResource(R.drawable.bg_trans);

        setAvatar.setOnClickListener(this);
        mSetNickNameEtv.setOnClickListener(this);
        mSetHomeTownEtv.setOnClickListener(this);
        mFinishBtn.setOnClickListener(this);
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
            case R.id.setAvatarIm:
                showChoosePhotoDialog();
                break;
            case R.id.setHomeTownEtv:
                showCityPickerDialog();
                break;
            case R.id.finishBtn:
                mListener.OnPushData(new HashMap<String, String>() {{
                    put("nickName", mSetNickNameEtv.getText().toString().trim());
                    put("homeTown", mSetHomeTownEtv.getText().toString().trim());
                    put("gender", mGender);
                }});
                break;
            case R.id.preBtn:
                mListener.OnPullData();
                break;
        }
    }

    private void showCityPickerDialog() {
        View outerView = LayoutInflater.from(getActivity()).inflate(R.layout.wheel_view, null);
        final WheelView mWvCol1 = (WheelView) outerView.findViewById(R.id.wheel_col1);
        final WheelView mWvCol2 = (WheelView) outerView.findViewById(R.id.wheel_col2);
        mProvince = Arrays.asList(getActivity().getResources().getStringArray(
                R.array.province));
        mWvCol1.setOffset(1);
        mWvCol1.setItems(mProvince);
        mWvCol1.setSeletion(0);
        mWvCol2.setOffset(1);
        mWvCol2.setItems(Arrays.asList(getActivity().getResources().getStringArray(cities[0])));
        mWvCol2.setSeletion(0);
        mWvCol1.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                mWvCol2.setOffset(1);
                mWvCol2.setItems(Arrays.asList(getActivity().getResources().getStringArray(cities[selectedIndex - 1])));
                mWvCol2.setSeletion(0);
            }
        });
        new AlertDialog.Builder(getActivity())
                .setTitle("请选择")
                .setView(outerView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSetHomeTownEtv.setText(mWvCol1.getSeletedItem() + " " + mWvCol2.getSeletedItem());
                    }

                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE) {
            if (data == null) {
                return;
            }
            String pathString = VeryUtils.resolvePhotoFromIntent(getActivity(),
                    data, FileAccessor.getAvatarPathName() + "/");
            File file = new File(pathString);
            Uri fileUri = Uri.fromFile(file);
            startImageZoom(fileUri);
        } else if (requestCode == CROP_REQUEST_CODE) {
            if (data == null) {
                return;
            }
            Bundle extras = data.getExtras();
            Bitmap bm = extras.getParcelable("data");
            mAvatarFilePath = VeryUtils.saveBitmapToLocal(FileAccessor.getAvatarPathName() + "/" + VeryUtils.md5("avatar"), bm);
            setAvatar.setImageURI(Uri.fromFile(new File(mAvatarFilePath)));

        }
    }

    private void startImageZoom(Uri uri) {
        LogUtil.e(uri.getPath());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("scale", false);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }


    private void showChoosePhotoDialog() {
        CharSequence[] items = {"相册", "相机"};
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("选择图片来源")
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            Intent intent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PHOTO_REQUEST_CODE);
                        } else {
                            Intent intent = new Intent(
                                    Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");
                            startActivityForResult(intent, PHOTO_REQUEST_CODE);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
