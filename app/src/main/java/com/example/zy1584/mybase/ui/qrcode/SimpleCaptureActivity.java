package com.example.zy1584.mybase.ui.qrcode;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.zy1584.mybase.manager.TabsManager;

import io.github.xudaojie.qrcodelib.CaptureActivity;

/**
 * Created by xdj on 16/9/17.
 */

public class SimpleCaptureActivity extends CaptureActivity {
    protected Activity mActivity = this;
    private TabsManager mTabsManager;
    private AlertDialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivity = this;
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void handleResult(final String resultString) {
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(mActivity, io.github.xudaojie.qrcodelib.R.string.scan_failed, Toast.LENGTH_SHORT).show();
            restartPreview();
        } else {
            // TODO: 16/9/17 ...

            if (mDialog == null) {
                mDialog = new AlertDialog.Builder(mActivity)
                        .setMessage(resultString)
                        .setPositiveButton("打开",
                               new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
//                         Intent intent = new Intent();
//                         intent.setAction("android.intent.action.VIEW");
//                         Uri content_url = Uri.parse(resultString);
//                         intent.setData(content_url);
//                         startActivity(intent);
                         Intent resultIntent = new Intent();
                         Bundle bundle = new Bundle();
                         bundle.putString("result", resultString);
                         resultIntent.putExtras(bundle);
                         setResult(RESULT_OK, resultIntent);
                         finish();
                         }
                        }).setNegativeButton("复制", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           if (resultString!= null) {// TODO: 2017-8-7 是否处理文件路径
                               ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                               ClipData clip = ClipData.newPlainText("label", resultString);
                               clipboard.setPrimaryClip(clip);
                               Toast.makeText(SimpleCaptureActivity.this, "已复制到粘贴板",
                                         Toast.LENGTH_LONG).show();
                           }
                        }
                      })
                       .setNeutralButton("关闭", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    restartPreview();
                                }}).create();


            if (!mDialog.isShowing()) {
                mDialog.setMessage(resultString);
                mDialog.show();
            }
        }
    }
    }
}
