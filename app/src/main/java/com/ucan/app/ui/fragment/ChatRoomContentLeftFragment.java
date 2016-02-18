package com.ucan.app.ui.fragment;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ucan.app.R;
import com.ucan.app.common.helper.MeetingHelper;
import com.ucan.app.ui.view.CircularImage;
import com.ucan.app.ui.ptr.PtrClassicFrameLayout;
import com.ucan.app.ui.ptr.PtrDefaultHandler;
import com.ucan.app.ui.ptr.PtrFrameLayout;
import com.ucan.app.ui.ptr.PtrHandler;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECMeetingManager;
import com.yuntongxun.ecsdk.meeting.ECMeeting;
import com.yuntongxun.ecsdk.meeting.ECMeetingMember;

public class ChatRoomContentLeftFragment extends Fragment {
	private View view;
	private ListView mListView;
	private MeetingAdapter mAdapter;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		MeetingHelper.addInterPhoneCallback(OnMeetingCallback);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_chatroom_content_left,
				container, false);
		final PtrClassicFrameLayout ptrFrame = (PtrClassicFrameLayout) view
				.findViewById(R.id.fragment_rotate_header_with_view_group_frame);
		ptrFrame.setPtrHandler(new PtrHandler() {
			@Override
			public void onRefreshBegin(PtrFrameLayout frame) {
				frame.postDelayed(new Runnable() {
					@Override
					public void run() {
						ptrFrame.refreshComplete();
					}
				}, 1800);
				MeetingHelper
				.queryMeetings(ECMeetingManager.ECMeetingType.MEETING_MULTI_VOICE);
			}

			@Override
			public boolean checkCanDoRefresh(PtrFrameLayout frame,
					View content, View header) {
				return PtrDefaultHandler.checkContentCanBePulledDown(frame,
						mListView, header);
			}
		});
		ptrFrame.setLastUpdateTimeRelateObject(this);
		// the following are default settings
		ptrFrame.setResistance(1.7f);
		ptrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
		ptrFrame.setDurationToClose(200);
		ptrFrame.setDurationToCloseHeader(1000);
		// default is false
		ptrFrame.setPullToRefresh(false);
		// default is true
		ptrFrame.setKeepHeaderWhenRefresh(true);

		// scroll then refresh
		// comment in base fragment
		ptrFrame.postDelayed(new Runnable() {
			@Override
			public void run() {
				 ptrFrame.autoRefresh();
			}
		}, 150);
		mListView = (ListView) ptrFrame.findViewById(R.id.meeting_lv);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.getBoolean("isConflict", false))
			return;
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

	
	
	public class MeetingAdapter extends ArrayAdapter<ECMeeting> {

		public MeetingAdapter(Context context, List<ECMeeting> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view;
			MeetingHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				view = getActivity().getLayoutInflater().inflate(
						R.layout.voice_chatroom_item, null);
				holder = new MeetingHolder();
				view.setTag(holder);
				holder.cover_user_photo = (CircularImage) view
						.findViewById(R.id.cover_user_photo);
				
				holder.roomName = (TextView) view
						.findViewById(R.id.chatroom_name);
				holder.tips = (TextView) view.findViewById(R.id.chatroom_tips);
				/*
				 * holder.hostName = (TextView)
				 * view.findViewById(R.id.host_name);
				 */
				//holder.lock = (ImageView) view.findViewById(R.id.lock);
			} else {
				view = convertView;
				holder = (MeetingHolder) convertView.getTag();
			}

			ECMeeting meeting = getItem(position);
			if (meeting != null) {
				holder.roomName.setText(meeting.getMeetingName());
				/* holder.hostName.setText(meeting.getCreator()); */
				boolean meetingFill = (meeting.getJoined() == meeting
						.getSquare());
				holder.tips.setText(getString(
						R.string.str_chatroom_list_join_number,
						meeting.getJoined(), "5"));
				/*holder.lock.setVisibility(meeting.isValidate() ? View.VISIBLE
						: View.GONE);*/
				holder.cover_user_photo.setImageResource(R.drawable.user_photo);
			}
			return view;
		}

		class MeetingHolder {
			TextView roomName;
			TextView tips;
			TextView hostName;
			ImageView lock;
			CircularImage cover_user_photo;
		}
	}
	MeetingHelper.OnMeetingCallback OnMeetingCallback = new MeetingHelper.OnMeetingCallback() {
		@Override
		public void onMeetings(List<ECMeeting> list) {
			if (list == null) {
				mListView.setAdapter(null);
				return;
			}
			mAdapter = new MeetingAdapter(getActivity(), list);
			mListView.setAdapter(mAdapter);			
		}
		
		@Override
		public void onMeetingStart(String meetingNo) {
			
		}
		
		@Override
		public void onMeetingMembers(List<? extends ECMeetingMember> members) {
			
		}
		
		@Override
		public void onMeetingDismiss(String meetingNo) {
			
		}
		
		@Override
		public void onError(int type, ECError e) {
			
		}
	};
}
