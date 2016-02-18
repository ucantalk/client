package com.ucan.app.base.db;

public class MessageObservable extends Observable<OnMessageChange> {

	/**
	 * 分发数据库改变通知
	 * @param session
	 */
	 public void notifyChanged(String session) {
	        synchronized(mObservers) {
	            for (int i = mObservers.size() - 1; i >= 0; i--) {
	            	
	            	if(mObservers.get(i)!=null){
	                mObservers.get(i).onChanged(session);
	            	}
	            }
	        }
	    }
}
