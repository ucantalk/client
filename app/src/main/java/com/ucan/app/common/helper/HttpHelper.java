package com.ucan.app.common.helper;

import com.ucan.app.base.core.AsyncServerConfig;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class HttpHelper {
    private static HttpHelper mInstance;

    private Retrofit mRetrofit;

    public static HttpHelper getInstance() {
        if (mInstance == null) {
            mInstance = new HttpHelper();
        }
        return mInstance;
    }

    private HttpHelper(){
        if(mRetrofit==null) {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(AsyncServerConfig.getBaseServerUrl()).addConverterFactory(GsonConverterFactory.create())
                    .build();

        }
    }
    public <T> T httpClient(final Class<T> t) {
        return mRetrofit.create(t);
    }

    public void downloadFile() {


    }

    public void uploadFile() {


    }

}
