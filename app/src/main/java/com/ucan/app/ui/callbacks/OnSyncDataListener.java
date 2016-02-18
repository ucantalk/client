package com.ucan.app.ui.callbacks;

import java.util.HashMap;

public interface OnSyncDataListener {
	  void OnPushData(HashMap v);
	  void OnPullData();
}