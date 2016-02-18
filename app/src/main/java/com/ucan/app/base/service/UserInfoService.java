package com.ucan.app.base.service;

import com.squareup.okhttp.RequestBody;

import java.util.HashMap;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by soso on 2015/12/18.
 */
public interface UserInfoService {
    @GET("user/info/{account}/isExist")
    Call<HashMap<String, String>> isExistUser(@Path("account") String account);

    @GET("user/info/userLogin")
    Call<HashMap<String, String>> userLogin(@Query("account") String account, @Query("password") String password);

    @Multipart
    @POST("user/info/userRegister")
    Call<HashMap<String, String>> registerUserAccount(@PartMap HashMap<String, RequestBody> params);

    @Multipart
    @POST("user/info/saveAvatar")
    Call<HashMap<String, String>> uploadAvatarImage(@PartMap HashMap<String, RequestBody> params);
}
