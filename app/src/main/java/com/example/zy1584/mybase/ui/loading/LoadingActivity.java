package com.example.zy1584.mybase.ui.loading;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseActivity;
import com.example.zy1584.mybase.ui.loading.mvp.LoadingActContract;
import com.example.zy1584.mybase.ui.loading.mvp.LoadingPresenter;
import com.example.zy1584.mybase.ui.main.BrowserActivity;
import com.example.zy1584.mybase.utils.LocationUtils;
import com.example.zy1584.mybase.utils.SPUtils;

import okhttp3.ResponseBody;

import static com.example.zy1584.mybase.utils.GlobalParams.IS_NET_ALLOW;

public class LoadingActivity extends BaseActivity<LoadingPresenter> implements LoadingActContract.View{

    @Override
    protected int getLayoutId() {
        return R.layout.activity_loading;
    }

    @Override
    protected LoadingPresenter loadPresenter() {
        return new LoadingPresenter();
    }


    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        showNetAllow();
    }

    private void showNetAllow(){
        final boolean isNetAllow = (boolean) SPUtils.get(IS_NET_ALLOW, false);
        if (!isNetAllow){
            new AlertDialog.Builder(this)
                    .setTitle("网络允许")
                    .setMessage("是否允许使用网络")
                    .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SPUtils.put(IS_NET_ALLOW, true);
                            requestServerAddress();
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SPUtils.put(IS_NET_ALLOW, false);
                            finish();
                        }
                    }).show();
        }else{
            requestServerAddress();
        }
    }

    private void requestServerAddress() {
        String latitude = (String) SPUtils.get(LocationUtils.LATITUDE, "22.53");
        String longitude = (String) SPUtils.get(LocationUtils.LONGITUDE, "114.03");
//        mPresenter.requestServerAddress(longitude, latitude);
    }

    @Override
    public void onReceiveServerAddress(ResponseBody body) {
        gotoActivity(BrowserActivity.class, true);
    }

    @Override
    public void onrequestServerAddressError() {

    }
}
