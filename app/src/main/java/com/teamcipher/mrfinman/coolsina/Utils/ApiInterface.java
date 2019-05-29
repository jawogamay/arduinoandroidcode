package com.teamcipher.mrfinman.coolsina.Utils;

import com.teamcipher.mrfinman.coolsina.Model.MessageResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiInterface {
    @FormUrlEncoded
    @POST("sms/json")
    Call<MessageResponse> getMessageResponse(
            @Field("api_key") String apiKey,
            @Field("api_secret") String apiSecret,
            @Field("from") String from,
            @Field("to") String to,
            @Field("text") String text
    );
}
