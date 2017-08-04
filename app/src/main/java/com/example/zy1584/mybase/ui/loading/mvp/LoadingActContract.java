package com.example.zy1584.mybase.ui.loading.mvp;

import okhttp3.ResponseBody;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface LoadingActContract {

    interface View{
        void onReceiveServerAddress(ResponseBody body);

        void onrequestServerAddressError();
    }

    interface Presenter{

        void requestServerAddress();

    }
}
