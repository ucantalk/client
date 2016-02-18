package com.ucan.app.ui.callbacks;

import java.io.IOException;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by soso on 2015/12/28.
 */
public abstract class ResponseCallBack<T extends HashMap<String, String>> implements Callback<T> {
    @Override
    public void onResponse(Response<T> response, Retrofit retrofit) {
        try {
            if (response.isSuccess()) {
                HashMap<String, String> body = response.body();
                if (body.containsKey("msg") && body.containsKey("data")) {
                    onSuccess(response.code(),body.get("msg"), body.get("data"));
                }
            } else {
                onError(new Throwable(response.errorBody().string()));
            }
        } catch (IOException e) {
            onError(e);
        } catch (NullPointerException e) {
            onError(e);
        }
    }


    @Override
    public void onFailure(Throwable t) {
        onError(t);
    }

    public abstract void onSuccess(int code,String msg, String data);

    public abstract void onError(Throwable t);
}
