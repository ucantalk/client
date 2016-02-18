package com.ucan.app.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.ucan.app.R;
import com.ucan.app.common.adapter.OverflowAdapter;
import com.ucan.app.common.adapter.OverflowAdapter.OverflowItem;
import com.ucan.app.common.helper.SDKCoreHelper;
import com.ucan.app.ui.activities.SearchActivity;
import com.ucan.app.common.helper.OverflowHelper;

public class ChatRoomFragment extends Fragment {
	private View view;
	private OverflowAdapter.OverflowItem[] mItems;
	private OverflowHelper mOverflowHelper;
	private int topBtnIndex, cTopIndex;
	private Button mTopBtn[];
	private Fragment[] contentFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_chatroom, container, false);
		mOverflowHelper = new OverflowHelper(getActivity());
		initOverflowItems();

		contentFragment = new Fragment[] { new ChatRoomContentLeftFragment(),
				new ChatRoomContentRightFragment() };
		getChildFragmentManager().beginTransaction()
				.add(R.id.fragment_content, contentFragment[0])
				.add(R.id.fragment_content, contentFragment[1])
				.hide(contentFragment[1]).commit();
		//view.findViewById(R.id.btn_menu).setOnClickListener(btnClickListener);
		return view;
	}

	View.OnClickListener btnClickListener = new View.OnClickListener() {
		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			/*case R.id.btn_menu:
				controlPlusSubMenu();
				break;*/
			}
		}
	};

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

	/**
	 * 获取所有会话
	 * 
	 * @param context
	 * @return +
	 */

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
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

	private void controlPlusSubMenu() {
		if (mOverflowHelper == null) {
			return;
		}

		if (mOverflowHelper.isOverflowShowing()) {
			mOverflowHelper.dismiss();
			return;
		}

		mOverflowHelper.setOverflowItems(mItems);
		mOverflowHelper
				.setOnOverflowItemClickListener(mOverflowItemCliclListener);
		//mOverflowHelper.showAsDropDown(view.findViewById(R.id.btn_menu));
	}

	private final AdapterView.OnItemClickListener mOverflowItemCliclListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			controlPlusSubMenu();

			OverflowItem overflowItem = mItems[position];
			String title = overflowItem.getTitle();

			if (getString(R.string.main_plus_meeting_voice).equals(title)) {
				// 语音房间

			} else if (getString(R.string.main_plus_meeting_video)
					.equals(title)) {

				// 视频房间

			} else if (getString(R.string.main_plus_search).equals(title)) {

				startActivity(new Intent(getActivity(), SearchActivity.class));
				// 搜索
			}
		}

	};

	void initOverflowItems() {
		if (mItems == null) {
			if (SDKCoreHelper.getInstance().isSupportMedia()) {
				mItems = new OverflowAdapter.OverflowItem[3];
				mItems[0] = new OverflowAdapter.OverflowItem(
						getString(R.string.main_plus_meeting_voice));
				mItems[0].setIcon(R.drawable.headset);
				mItems[1] = new OverflowAdapter.OverflowItem(
						getString(R.string.main_plus_meeting_video));
				mItems[1].setIcon(R.drawable.camera);
				mItems[2] = new OverflowAdapter.OverflowItem(
						getString(R.string.main_plus_search));
				mItems[2].setIcon(R.drawable.search);

			} else {
				mItems = new OverflowAdapter.OverflowItem[1];
				mItems[0] = new OverflowAdapter.OverflowItem(
						getString(R.string.main_plus_search));
				mItems[0].setIcon(R.drawable.search);
			}
		}

	}

}
