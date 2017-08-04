package com.example.zy1584.mybase.ui.loading.mvp;


import com.example.zy1584.mybase.base.BaseModel;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;


/**
 * Created by zy1584 on 2017-3-30.
 */

public class LoadingBiz extends BaseModel {

    public Call<ResponseBody> requestServerAddress(Map<String, String> params){
        return httpService.requestServerAddress(params);
    }
}
